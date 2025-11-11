package com.datarecv.cloud.command;

import com.datarecv.cloud.session.Session;
import com.datarecv.cloud.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CommandObserverImpl implements CommandObserver {

    private static final Logger log = LoggerFactory.getLogger(CommandObserverImpl.class);

    /** id is 唯一 */
    private String id;
    /**
     * 反控的结果
     */
    private Object result;

    private SessionManager sessionManager;

    private Condition condition;

    private Lock lock;

    public CommandObserverImpl(SessionManager sessionManager, String id,Lock lock,Condition condition) {
        this.id = id;
        this.sessionManager = sessionManager;
        this.lock=lock;
        this.condition=condition;
    }

    @Override
    public void publish(Object message) {
        lock.lock();
        try{
            this.result=message;
            condition.signalAll();
            Session session = this.sessionManager.findByClientId(id);
            session.setCommand(false);
            session.setCommandSubject(null);
            session.setCommandDown(null);
            log.info("publish:{}", message);
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getResult() {
        return result;
    }

}
