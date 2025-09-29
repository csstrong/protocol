package com.wayeal.cloud.server.protocol;

import com.wayeal.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * @author jian
 * @version 2023-02-14 14:03
 */
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
