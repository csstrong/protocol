package com.wayeal.cloud.session;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author jian
 * @version 2023-02-08 10:54
 */
public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);


    private final Map<String,String>   relationMap;

    private final Cache<String, Object> offlineCache;

    private final Map<String, Map<String, Session>> cache;

    public SessionManager(){
        this.cache   = new ConcurrentHashMap<>();
        this.relationMap  = new ConcurrentHashMap<>();
        this.offlineCache = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();
    }


    public void put(String clientId, String id, Session session) {
        Map<String, Session> map;
        if (cache.containsKey(clientId)) {
            map = cache.get(clientId);
            if (map!=null &&!map.containsKey(id)) {
                map.put(id, session);
            }else {
                map = new LinkedHashMap<>();
                map.put(id, session);
                cache.put(clientId, map);
            }
        } else {
            map = new LinkedHashMap<>();
            map.put(id, session);
            cache.put(clientId, map);
        }
        relationMap.put(id,clientId);
    }

    /**
     * 默认返回第一个
     *
     * @return
     */
    public Session findByClientId(String clientId) {
        if (clientId == null) {
            return null;
        }
        Map<String, Session> sessionMap = cache.get(clientId);
        if (sessionMap!=null && sessionMap.size() > 0) {
            for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
                return entry.getValue();
            }
        }
        return null;
    }

    public Session findById(String id) {
        if (id == null) {
            return null;
        }
        String clientId = relationMap.get(id);
        return findByClientId(clientId);
    }

    public Session findByClientIdAndId(String clientId, String id){
        if (clientId == null) {
            return null;
        }
        Map<String, Session> sessionMap = cache.get(clientId);
        if (sessionMap!=null && sessionMap.size() > 0) {
            return sessionMap.get(id);
        }
        return null;
    }

    public void remove(String id) {
        if (id == null) {
            return;
        }
        Session session = findById(id);
        if (session != null) {
            String client = session.getClintId();
            Map<String, Session> map = cache.get(client);
            map.remove(id);
            if (map.size() == 0) {
                cache.remove(client);
            }
        }
        relationMap.remove(id);
    }
}
