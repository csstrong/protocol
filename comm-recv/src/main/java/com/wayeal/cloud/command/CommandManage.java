package com.wayeal.cloud.command;

import com.wayeal.cloud.session.Session;
import com.wayeal.cloud.session.SessionManager;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommandManage {
    /** session */
    private SessionManager sessionManager;
    /** 下发的命令 message */
    private Object message;
    /** id */
    private String id;
    /** 最长等待的时间 */
    private static final long waitTime = 10;

    private final Lock lock;

    private Condition result;

    private static final Logger log = LoggerFactory.getLogger(CommandObserverImpl.class);

    public CommandManage(SessionManager sessionManager, String id, Object message) {
        this.sessionManager = sessionManager;
        lock = new ReentrantLock();
        result = lock.newCondition();
        this.id = id;
        this.message = message;
    }

    public Object command() {
        Session session = this.sessionManager.findByClientId(id);
        //当前反控没有结束，拒绝
        if (session.isCommand()){
            return null;
        }
        if (message instanceof String) {
            session.write((String) message);
        }
        if (message instanceof ByteBuf) {
            session.write((ByteBuf) message);
        }
        CommandObserver commandObserver = new CommandObserverImpl(sessionManager, id, lock, result);
        CommandSubject commandSubject = new CommandSubjectImpl(commandObserver);

        session.setCommand(true);
        session.setCommandSubject(commandSubject);
        session.setCommandDown(message);
        lock.lock();
        try {
            long current = System.currentTimeMillis();
            log.info("------------------current:{}", current);
            if (commandObserver.getResult() != null) {
                result.await(waitTime, TimeUnit.SECONDS);
            }
            long end = System.currentTimeMillis();
            log.info("------------------end:{}", end);
            if (commandObserver.getResult() != null) {
                return commandObserver.getResult();
            }
            log.info("------------------反控超时");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }
}
