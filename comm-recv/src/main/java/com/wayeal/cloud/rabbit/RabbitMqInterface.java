package com.wayeal.cloud.rabbit;

import com.wayeal.cloud.enums.ProtocolType;

public interface RabbitMqInterface {

    void sendMessage(String message, ProtocolType protocolType);
}
