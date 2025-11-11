package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

public interface ContentDecoderAbstract {

    void read(ByteBuf buf);

    List<ElementResult> getList();

    Map<String, String> getLastElement();

    void write();
}
