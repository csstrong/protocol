package com.wayeal.cloud.command;
/**
 * @des 命令发布
 * @author jian
 * @version 2023-02-27 9:58
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
