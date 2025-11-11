package com.datarecv.cloud.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandSubjectImpl implements CommandSubject{

    private static final Logger log = LoggerFactory.getLogger(CommandSubjectImpl.class);

    /**
     * 发布订阅维护
     */
    private CommandObserver commandObserver;

    public CommandSubjectImpl(CommandObserver commandObserver){
        this.commandObserver=commandObserver;
    }

    @Override
    public void notify(Object message) {
        commandObserver.publish(message);
    }

}
