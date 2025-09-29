package com.wayeal.cloud.server.protocol;

import com.wayeal.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * @author jian
 * @version 2023-02-14 10:57
 */
public interface ContentDecoderAbstract {

    void read(ByteBuf buf);

    List<ElementResult> getList();

    Map<String, String> getLastElement();

    void write();
}
