package org.cytobank.springboot3demo.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.cytobank.springboot3demo.constants.CaffeineCacheConstants;
import org.cytobank.springboot3demo.utils.FileHandler;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author jasonchen
 */
@RestController
@RequestMapping("/api/v1/cache")
@Slf4j
public class CaffeineCacheController {
    private final CacheManager cacheManager;
    private final ObjectMapper objectMapper;

    public CaffeineCacheController(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        this.objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    @GetMapping("/name")
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }

    @GetMapping("/entries/{cacheName}")
    public Map<String, Object> getEntriesForCache(@PathVariable(name = "cacheName") final String cacheName) throws JsonProcessingException {
        final Cache<String, Object> cache = (Cache<String, Object>) cacheManager.getCache(cacheName).getNativeCache();
        final ConcurrentMap<String, Object> data = cache.asMap();
        final String json = objectMapper.writeValueAsString(data);
        final TypeReference<HashMap<String,Object>> typeRef = new TypeReference<>() {};
        return objectMapper.readValue(json, typeRef);
    }

    @GetMapping("/stats")
    public CacheStats getCacheStats() {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CaffeineCacheConstants.CACHE_NAME);
        Cache nativeCoffeeCache = caffeineCache.getNativeCache();
        return nativeCoffeeCache.stats();
    }

    @GetMapping("/save")
    public void saveStripeFileToCache() {
        final Cache<String, Object> cache = (Cache<String, Object>) cacheManager.getCache(CaffeineCacheConstants.CACHE_NAME).getNativeCache();

        FileHandler fileHandler = new FileHandler();
        // 200M
        String path = "/Users/jasonchen/cytobank-data/cytobank/Temp/Cache/cytobank_development_experiments/834/experiment_834_cache/cytoflex_25m_tube4.fcs/channel_0";
        double[] events = fileHandler.loadChannelStripeFileToCache(path);

        cache.put("channel_0", events);
    }

    @GetMapping("/keys")
    public Set<Object> keys() {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CaffeineCacheConstants.CACHE_NAME);
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();

        return nativeCache.asMap().keySet();
    }

    @GetMapping("/keys/{key}")
    public Integer keys(@PathVariable("key") String key) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CaffeineCacheConstants.CACHE_NAME);
        Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
        ConcurrentMap<Object, Object> data = nativeCache.asMap();

        double[] events = (double[]) data.get(key);
        if(events != null){
            log.info(String.valueOf(events.length));
            return events.length;
        }
        return 0;
    }

}
