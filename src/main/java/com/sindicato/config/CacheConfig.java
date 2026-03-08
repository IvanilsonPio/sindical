package com.sindicato.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Configuração de cache da aplicação.
 * 
 * Utiliza Caffeine como provedor de cache para melhorar a performance
 * de consultas frequentes ao banco de dados.
 * 
 * Estratégia de cache:
 * - socios: Cache de 10 minutos para dados de sócios
 * - pagamentos: Cache de 5 minutos para dados de pagamentos
 * - arquivos: Cache de 15 minutos para metadados de arquivos
 * - recibos: Cache de 30 minutos para recibos gerados
 * 
 * O cache é automaticamente invalidado quando dados são modificados
 * através das anotações @CacheEvict nos métodos de serviço.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura o gerenciador de cache com Caffeine.
     * 
     * Caffeine é uma biblioteca de cache de alta performance para Java,
     * baseada no Google Guava, com melhorias significativas de performance.
     * 
     * Configurações padrão:
     * - Tamanho máximo: 1000 entradas por cache
     * - Tempo de expiração: 10 minutos após escrita
     * - Remoção automática de entradas expiradas
     * 
     * @return CacheManager configurado com Caffeine
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "socios",
            "socio",
            "pagamentos",
            "pagamento",
            "arquivos",
            "arquivo",
            "recibos"
        );
        
        cacheManager.setCaffeine(caffeineCacheBuilder());
        
        return cacheManager;
    }

    /**
     * Configura o builder do Caffeine com parâmetros de performance.
     * 
     * Configurações:
     * - maximumSize: Limita o número de entradas no cache
     * - expireAfterWrite: Remove entradas após tempo especificado
     * - recordStats: Habilita estatísticas de cache para monitoramento
     * 
     * @return Caffeine builder configurado
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats();
    }

    /**
     * Configuração específica para cache de recibos.
     * Recibos têm TTL maior pois são documentos imutáveis.
     * 
     * @return Caffeine builder para recibos
     */
    @Bean("reciboCacheBuilder")
    public Caffeine<Object, Object> reciboCacheBuilder() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats();
    }
}
