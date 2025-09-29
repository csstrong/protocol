package com.wayeal.cloud.server.protocol;

import com.wayeal.cloud.model.ContentMessageRequest;
import com.wayeal.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

/**
 * @author jian
 * @version 2023-02-14 14:57
 */
public class ContentManage {

    private ContentDecoderAbstract contentDecoderAbstract;

    private ContentMessageRequest contentMessageRequest;

    public ContentManage(ContentDecoderAbstract contentDecoderAbstract ,ContentMessageRequest contentMessageRequest){
        this.contentDecoderAbstract=contentDecoderAbstract;
        this.contentMessageRequest=contentMessageRequest;
    }

    public ContentMessageRequest read(ByteBuf byteBuf){
        contentDecoderAbstract.read(byteBuf);
        List<ElementResult> list=contentDecoderAbstract.getList();
        Map<String,String> map= contentDecoderAbstract.getLastElement();
        contentMessageRequest.setElementResultS(list);
        contentMessageRequest.setCommonFactor(map);
        return this.contentMessageRequest;
    }
}
