package com.datarecv.cloud.command;
/**
 * @des 命令发布
 */
public interface CommandObserver {

    /**
     * 向所有的客户端发布消息
     * @param message
     */
    void publish(Object message);

    String getId();

    Object getResult();
}
