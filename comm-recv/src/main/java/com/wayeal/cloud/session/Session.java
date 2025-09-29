package com.wayeal.cloud.session;

import com.wayeal.cloud.command.CommandSubject;
import com.wayeal.cloud.enums.ProtocolType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);

    protected final Channel channel;

    private final SocketAddress remoteAddress;

    private final String remoteAddressStr;

    private final long creationTime;

    private final String id;

    private final String clintId;

    private Object data;
    /** 分包的数据缓存 */
    private Map<String, Object> subData;
    /** 分割符 */
    private final String delimiter;

    private ProtocolType protocolType;

    private long lastAccessedTime;
    /** 命令 */
    private Object commandDown;
    /** 是否存在命令 按理来说一次只能执行一条命令 */
    private boolean isCommand;
    /** 命令返回的值 */
    private CommandSubject commandSubject;

    public Session(
            Channel channel,
            SocketAddress remoteAddress,
            String delimiter,
            String clintId,
            ProtocolType protocolType,
            Object data) {
        this.channel = channel;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
        this.remoteAddress = remoteAddress;
        this.remoteAddressStr = remoteAddress.toString();
        this.clintId = clintId;
        this.id = channel.id().asLongText();
        this.delimiter = delimiter;
        this.protocolType = protocolType;
        this.subData = new ConcurrentHashMap<>();
        this.data = data;
    }

    public Mono<Void> write(String msg) {

        return Mono.create(
                sink ->
                        channel.writeAndFlush(msg)
                                .addListener(
                                        future -> {
                                            if (future.isSuccess()) {
                                                sink.success();
                                            } else {
                                                sink.error(future.cause());
                                            }
                                        }));
    }

    public Mono<Void> write(ByteBuf msg) {

        return Mono.create(
                sink ->
                        channel.writeAndFlush(msg)
                                .addListener(
                                        future -> {
                                            if (future.isSuccess()) {
                                                sink.success();
                                            } else {
                                                sink.error(future.cause());
                                            }
                                        }));
    }

    public void register() {}

    public String getId() {
        return id;
    }

    public String getClintId() {
        return clintId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getSubData() {
        return subData;
    }

    public void setSubData(Map<String, Object> subData) {
        this.subData = subData;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public Channel getChannel() {
        return channel;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public ProtocolType getProtocolType() {
        return protocolType;
    }

    public void setProtocolType(ProtocolType protocolType) {
        this.protocolType = protocolType;
    }

    public synchronized boolean isCommand() {
        return isCommand;
    }

    public synchronized void setCommand(boolean command) {
        isCommand = command;
    }

    public synchronized Object getCommandDown() {
        return commandDown;
    }

    public synchronized void setCommandDown(Object commandDown) {
        this.commandDown = commandDown;
    }

    public synchronized CommandSubject getCommandSubject() {
        return commandSubject;
    }

    public synchronized void setCommandSubject(CommandSubject commandSubject) {
        this.commandSubject = commandSubject;
    }
}
