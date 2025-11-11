package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.model.ContentMessageRequest;
import com.datarecv.cloud.model.ElementResult;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.Map;

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
