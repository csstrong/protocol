package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

public class SL651_2F  implements ContentDecoderAbstract{
    @Override
    public void read(ByteBuf buf) {
    }

    @Override
    public List<ElementResult> getList() {
        return null;
    }

    @Override
    public Map<String, String> getLastElement() {
        return null;
    }

    @Override
    public void write() {

    }
}
