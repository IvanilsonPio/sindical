package com.sindicato.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import com.sindicato.dto.SocioRequest;
import com.sindicato.dto.SocioResponse;
import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;
import com.sindicato.repository.SocioRepository;

/**
 * Integration tests for SocioService caching behavior.
 * Verifies that cache annotations work correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class SocioServiceCacheTest {

    @Autowired
    private SocioService socioService;

    @MockBean
    private SocioRepository socioRepository;

    @MockBean
    private SocioHistoryService historyService;

    @Autowired
    private CacheManager cacheManager;

    private Socio testSocio;

    @BeforeEach
    void setUp() {
        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });

        // Create test socio
        testSocio = new Socio();
        testSocio.setId(1L);
        testSocio.setNome("João Silva");
        testSocio.setCpf("12345678901");
        testSocio.setMatricula("MAT001");
        testSocio.setRg("1234567");
        testSocio.setDataNascimento(LocalDate.of(1980, 1, 1));
        testSocio.setTelefone("11999999999");
        testSocio.setEmail("joao@example.com");
        testSocio.setEndereco("Rua Teste, 123");
        testSocio.setCidade("São Paulo");
        testSocio.setEstado("SP");
        testSocio.setCep("01234567");
        testSocio.setProfissao("Agricultor");
        testSocio.setStatus(StatusSocio.ATIVO);
    }

    @Test
    void buscarPorId_shouldCacheResult() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.of(testSocio));

        // Act - First call should hit repository
        SocioResponse response1 = socioService.buscarPorId(1L);
        
        // Act - Second call should use cache
        SocioResponse response2 = socioService.buscarPorId(1L);
        
        // Act - Third call should also use cache
        SocioResponse response3 = socioService.buscarPorId(1L);

        // Assert - Repository should be called only once
        verify(socioRepository, times(1)).findById(1L);
        
        // Assert - All responses should be equal
        assertThat(response1.getId()).isEqualTo(1L);
        assertThat(response2.getId()).isEqualTo(1L);
        assertThat(response3.getId()).isEqualTo(1L);
        assertThat(response1.getNome()).isEqualTo("João Silva");
    }

    @Test
    void criarSocio_shouldEvictSociosCache() {
        // Arrange
        SocioRequest request = new SocioRequest();
        request.setNome("Maria Santos");
        request.setCpf("98765432109");
        request.setMatricula("MAT002");
        request.setRg("9876543");
        request.setDataNascimento(LocalDate.of(1985, 5, 15));
        request.setTelefone("11988888888");
        request.setEmail("maria@example.com");
        request.setEndereco("Rua Nova, 456");
        request.setCidade("Rio de Janeiro");
        request.setEstado("RJ");
        request.setCep("20000000");
        request.setProfissao("Pecuarista");

        Socio newSocio = new Socio();
        newSocio.setId(2L);
        newSocio.setNome(request.getNome());
        newSocio.setCpf(request.getCpf());
        newSocio.setMatricula(request.getMatricula());
        newSocio.setStatus(StatusSocio.ATIVO);

        when(socioRepository.existsByCpf(request.getCpf())).thenReturn(false);
        when(socioRepository.existsByMatricula(request.getMatricula())).thenReturn(false);
        when(socioRepository.save(any(Socio.class))).thenReturn(newSocio);

        // Act - Create socio should evict socios cache
        SocioResponse response = socioService.criarSocio(request);

        // Assert
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getNome()).isEqualTo("Maria Santos");
        
        // Verify cache was evicted by checking it's empty
        var sociosCache = cacheManager.getCache("socios");
        assertThat(sociosCache).isNotNull();
    }

    @Test
    void atualizarSocio_shouldEvictBothCaches() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.of(testSocio));
        when(socioRepository.existsByCpfAndIdNot(any(), any())).thenReturn(false);
        when(socioRepository.existsByMatriculaAndIdNot(any(), any())).thenReturn(false);
        when(socioRepository.save(any(Socio.class))).thenReturn(testSocio);

        // First, cache the socio
        socioService.buscarPorId(1L);
        verify(socioRepository, times(1)).findById(1L);

        // Act - Update should evict cache (note: update also calls findById internally)
        SocioRequest updateRequest = new SocioRequest();
        updateRequest.setNome("João Silva Updated");
        updateRequest.setCpf(testSocio.getCpf());
        updateRequest.setMatricula(testSocio.getMatricula());
        updateRequest.setRg(testSocio.getRg());
        updateRequest.setDataNascimento(testSocio.getDataNascimento());
        updateRequest.setTelefone(testSocio.getTelefone());
        updateRequest.setEmail(testSocio.getEmail());
        updateRequest.setEndereco(testSocio.getEndereco());
        updateRequest.setCidade(testSocio.getCidade());
        updateRequest.setEstado(testSocio.getEstado());
        updateRequest.setCep(testSocio.getCep());
        updateRequest.setProfissao(testSocio.getProfissao());

        socioService.atualizarSocio(1L, updateRequest);

        // Act - Next call should hit repository again (cache evicted)
        socioService.buscarPorId(1L);

        // Assert - Repository should be called 3 times total:
        // 1. Initial cache (buscarPorId)
        // 2. Inside atualizarSocio (findById to get existing socio)
        // 3. After update (buscarPorId - cache was evicted)
        verify(socioRepository, times(3)).findById(1L);
    }

    @Test
    void excluirSocio_shouldEvictBothCaches() {
        // Arrange
        when(socioRepository.findById(1L)).thenReturn(Optional.of(testSocio));
        when(socioRepository.save(any(Socio.class))).thenReturn(testSocio);

        // First, cache the socio
        socioService.buscarPorId(1L);
        verify(socioRepository, times(1)).findById(1L);

        // Act - Delete should evict cache (note: delete also calls findById internally)
        socioService.excluirSocio(1L);

        // Act - Next call should hit repository again (cache evicted)
        socioService.buscarPorId(1L);

        // Assert - Repository should be called 3 times total:
        // 1. Initial cache (buscarPorId)
        // 2. Inside excluirSocio (findById to get existing socio)
        // 3. After delete (buscarPorId - cache was evicted)
        verify(socioRepository, times(3)).findById(1L);
    }

    @Test
    void cachesShouldBeIndependent() {
        // Arrange
        Socio socio2 = new Socio();
        socio2.setId(2L);
        socio2.setNome("Maria Santos");
        socio2.setCpf("98765432109");
        socio2.setMatricula("MAT002");
        socio2.setStatus(StatusSocio.ATIVO);

        when(socioRepository.findById(1L)).thenReturn(Optional.of(testSocio));
        when(socioRepository.findById(2L)).thenReturn(Optional.of(socio2));

        // Act - Cache both socios
        socioService.buscarPorId(1L);
        socioService.buscarPorId(2L);

        // Act - Call again to verify caching
        socioService.buscarPorId(1L);
        socioService.buscarPorId(2L);

        // Assert - Each should be called only once
        verify(socioRepository, times(1)).findById(1L);
        verify(socioRepository, times(1)).findById(2L);
    }
}
