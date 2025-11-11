package com.datarecv.cloud.command;

/**
 * @des 命令订阅
 */
public interface CommandSubject {

    //增加订阅者
   //  void add(CommandObserver observer);

    //删除订阅者
  //   void delete();

    //通知订阅者更新消息
     void notify(Object message);

  //  void notify(String id,Object message);
}
