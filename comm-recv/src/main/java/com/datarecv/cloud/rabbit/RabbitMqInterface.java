package com.datarecv.cloud.rabbit;

import com.datarecv.cloud.enums.ProtocolType;

public interface RabbitMqInterface {

    void sendMessage(String message, ProtocolType protocolType);
}
