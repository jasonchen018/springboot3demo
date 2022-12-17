package org.cytobank.springboot3demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.extern.slf4j.Slf4j;
import org.cytobank.springboot3demo.constants.CaffeineCacheConstants;
import org.hibernate.graph.Graph;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.query.QueryLookupStrategy;

import java.util.concurrent.TimeUnit;

/**
 * @author jasonchen
 */
@Configuration
@Slf4j
public class CaffeineCacheConfig {
    /**
     * initialCapacity
     * maximumSize
     * maximumWeight
     * expireAfterAccess
     * expireAfterWrite
     * refreshAfterWrite
     * weakKeys
     * weakValues
     * softValues
     * recordStats
     */
    @Bean
    public CacheManager cacheManager() {
        String specAsString = "initialCapacity=100,maximumSize=500,expireAfterAccess=5m,recordStats";
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CaffeineCacheConstants.CACHE_NAME_CUSTOMERS,
                CaffeineCacheConstants.CACHE_NAME_EVENTS);
        // can happen if you get a value from a @Cachable that returns null
        cacheManager.setAllowNullValues(false);
        // cacheManager.setCacheSpecification(specAsString);
        // cacheManager.setCaffeineSpec(caffeineSpec());
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    CaffeineSpec caffeineSpec() {
        return CaffeineSpec.parse("initialCapacity=100,maximumSize=500,expireAfterAccess=5m,recordStats");
    }

    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .initialCapacity(2_000)
                .maximumSize(10_000)
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .weakKeys()
                .removalListener(new CustomRemovalListener())
                .recordStats();
    }

    // Evict based on the number of vertices in the cache
    // LoadingCache<QueryLookupStrategy.Key, Graph> graphs = Caffeine.newBuilder()
    //         .maximumWeight(10_000)
    //         .weigher((QueryLookupStrategy.Key key, Graph graph) -> graph.vertices().size())
    //         .build(key -> createExpensiveGraph(key));


    static class CustomRemovalListener implements RemovalListener<Object, Object> {
        @Override
        public void onRemoval(Object key, Object value, RemovalCause cause) {
            System.out.format("removal listener called with key [%s], cause [%s], evicted [%S]\n",
                    key, cause.toString(), cause.wasEvicted());
        }
    }

}
