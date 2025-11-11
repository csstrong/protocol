package com.datarecv.cloud.server.protocol;

import com.datarecv.cloud.model.SubData;
import com.datarecv.cloud.enums.ProtocolType;
import com.datarecv.cloud.session.Session;
import com.datarecv.cloud.session.SessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface ProtocolAnalysisInterface {

    Object read(ByteBuf byteBuf);

    void write(Session data);

    SubData subpackage (Object data, SessionManager sessionManager, Channel channel, String delimiter, ProtocolType protocolType);

    void getCommand(Session session);

    String getDelimiter();

    void   setDelimiter(String delimiter);

    ProtocolType getProtocolType();

    void setProtocolType(ProtocolType protocolType);

}
