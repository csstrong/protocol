package com.wayeal.cloud.rabbit;

import com.wayeal.cloud.enums.ProtocolType;

/**
 * @author jian
 * @version 2023-02-16 11:30
 */
public interface RabbitMqInterface {

    void sendMessage(String message, ProtocolType protocolType);
}
