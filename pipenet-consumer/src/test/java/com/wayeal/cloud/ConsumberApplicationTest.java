package com.wayeal.cloud;

import com.wayeal.cloud.mongo.dao.HourMonitorDataRepository;
import com.wayeal.cloud.mongo.entity.MonitorData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Example;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@ComponentScan(basePackages = {"com.wayeal.cloud.mongo"})
public class ConsumberApplicationTest {

    @Resource
    private HourMonitorDataRepository hourMonitorDataRepository;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 1.count()统计总数
     */
    @Test
    void test01() {
        long count = hourMonitorDataRepository.count();
        System.out.println(count);
    }

    /**
     * 2.count（Example< T > example）条件统计总数
     */
    @Test
    void test02() {
        MonitorData monitorData = new MonitorData();
        monitorData.setMonitorTime("2022/08/04 15:00:00");
        Example<MonitorData> example = Example.of(monitorData);
        List<MonitorData> all = hourMonitorDataRepository.findAll(example);
        all.stream().forEach(System.out::println);
    }

    @Test
    void test03() {
        //Object ping = redisTemplate.opsForValue().get("PING");
        //System.out.println(ping);
        System.out.println("hello");
    }

    /**
     * 4.delete（T t）通过对象信息删除某条数据
     */
    @Test
    void test04() {

        //底层通过id删除数据
        hourMonitorDataRepository.delete(null);
    }

    /**
     * 5.delete（ID id）通过id删除某条数据
     */
    @Test
    void test05() {
        hourMonitorDataRepository.deleteById("111");
    }

    /**
     * 6.delete（Iterable<? extends Apple> iterable）批量删除某条数据
     */
    @Test
    void test06() {
        List<MonitorData> list = new ArrayList<>();
        hourMonitorDataRepository.deleteAll(list);
    }

    @Test
    public void processRedisDirtyKey() {
        Set<String> keys = redisTemplate.keys("*-union");
        System.out.println("size: " + keys.size());
        Map<Object, Object> map = new HashMap<>();

        List<String> list = new LinkedList<>();
        for (String key : keys) {
            Object value = redisTemplate.opsForValue().get(key);
            map.put(key, value);
            int index = key.indexOf("-union");
            String newKey = key.substring(0, index);
            list.add(newKey);
        }
        //list.stream().sorted().forEach(System.out::println);
        System.out.println("size: " + list.size());
        for (int i = 0; i < list.size(); i++) {
            String key = list.get(i);
            System.out.println(key);
            redisTemplate.opsForValue().set(key, 1, i + 5, TimeUnit.SECONDS);
        }
    }

}
