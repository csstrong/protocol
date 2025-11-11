package com.datarecv.cloud.redis;

import com.datarecv.cloud.config.RedisConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GetRedisStatusService {

	@Resource
	private RedisConfig redisConfig;

	public boolean judgeRedisConnectionStatus() {
		boolean status = false;
		RedisURI redisURI = RedisURI.builder()
			.withHost(redisConfig.getDbHost())
			.withPort(Integer.parseInt(redisConfig.getDbPort()))
			.withPassword(redisConfig.getPassword().toCharArray())
			.build();

		RedisClient redisClient = RedisClient.create(redisURI);
		ClientOptions options = ClientOptions.builder()
			.pingBeforeActivateConnection(true)
			.build();
		redisClient.setOptions(options);

		try {
			redisClient.connect().async();
			status = true;
		} catch (Exception e) {
			return false;
		} finally {
			redisClient.shutdown();
		}
		return status;
	}
}
