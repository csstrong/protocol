package com.wayeal.cloud.config;

import com.wayeal.cloud.server.protocol.DelimiterDto;
import com.wayeal.cloud.server.protocol.ProtocolDto;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "protocol")
public class ProtocolConfig {

    private String name;

    private List<DelimiterDto> delimiter;

    private List<ProtocolDto> protocol;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DelimiterDto> getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(List<DelimiterDto> delimiter) {
        this.delimiter = delimiter;
    }

    public List<ProtocolDto> getProtocol() {
        return protocol;
    }

    public void setProtocol(List<ProtocolDto> protocol) {
        this.protocol = protocol;
    }
}
