package com.sindicato.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for CacheConfig to ensure caching is properly configured.
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldLoadCacheManager() {
        assertThat(cacheManager).isNotNull();
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    void shouldHaveConfiguredCaches() {
        assertThat(cacheManager.getCacheNames())
            .contains("socios", "socio", "pagamentos", "pagamento", "arquivos", "arquivo", "recibos");
    }

    @Test
    void shouldCreateCacheOnDemand() {
        var cache = cacheManager.getCache("socios");
        assertThat(cache).isNotNull();
        
        // Test cache operations
        cache.put("test-key", "test-value");
        var cachedValue = cache.get("test-key", String.class);
        assertThat(cachedValue).isEqualTo("test-value");
        
        // Test cache eviction
        cache.evict("test-key");
        cachedValue = cache.get("test-key", String.class);
        assertThat(cachedValue).isNull();
    }

    @Test
    void shouldHandleMultipleCaches() {
        var sociosCache = cacheManager.getCache("socios");
        var pagamentosCache = cacheManager.getCache("pagamentos");
        
        assertThat(sociosCache).isNotNull();
        assertThat(pagamentosCache).isNotNull();
        assertThat(sociosCache).isNotSameAs(pagamentosCache);
        
        // Test that caches are independent
        sociosCache.put("key1", "value1");
        pagamentosCache.put("key1", "value2");
        
        assertThat(sociosCache.get("key1", String.class)).isEqualTo("value1");
        assertThat(pagamentosCache.get("key1", String.class)).isEqualTo("value2");
    }

    @Test
    void shouldClearAllEntriesInCache() {
        var cache = cacheManager.getCache("socios");
        assertThat(cache).isNotNull();
        
        // Add multiple entries
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");
        
        // Verify entries exist
        assertThat(cache.get("key1", String.class)).isEqualTo("value1");
        assertThat(cache.get("key2", String.class)).isEqualTo("value2");
        
        // Clear all entries
        cache.clear();
        
        // Verify all entries are cleared
        assertThat(cache.get("key1", String.class)).isNull();
        assertThat(cache.get("key2", String.class)).isNull();
        assertThat(cache.get("key3", String.class)).isNull();
    }
}
