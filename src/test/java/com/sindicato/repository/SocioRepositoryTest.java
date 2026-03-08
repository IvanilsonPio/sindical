package com.sindicato.repository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;

/**
 * Testes de integração para o SocioRepository.
 * Utiliza banco de dados em memória (H2) para testes.
 */
@DataJpaTest
class SocioRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private SocioRepository socioRepository;
    
    private Socio socioAtivo;
    private Socio socioInativo;
    
    @BeforeEach
    void setUp() {
        socioAtivo = new Socio("João Silva", "123.456.789-00", "MAT001");
        socioAtivo.setRg("12.345.678-9");
        socioAtivo.setDataNascimento(LocalDate.of(1980, 5, 15));
        socioAtivo.setTelefone("(11) 98765-4321");
        socioAtivo.setEmail("joao@email.com");
        socioAtivo.setEndereco("Rua das Flores, 123");
        socioAtivo.setCidade("São Paulo");
        socioAtivo.setEstado("SP");
        socioAtivo.setCep("01234-567");
        socioAtivo.setProfissao("Agricultor");
        socioAtivo.setStatus(StatusSocio.ATIVO);
        
        socioInativo = new Socio("Maria Santos", "987.654.321-00", "MAT002");
        socioInativo.setTelefone("(11) 91234-5678");
        socioInativo.setEmail("maria@email.com");
        socioInativo.setCidade("Campinas");
        socioInativo.setEstado("SP");
        socioInativo.setProfissao("Pecuarista");
        socioInativo.setStatus(StatusSocio.INATIVO);
        
        entityManager.persist(socioAtivo);
        entityManager.persist(socioInativo);
        entityManager.flush();
    }
    
    @Test
    void deveBuscarSocioPorCpf() {
        Optional<Socio> encontrado = socioRepository.findByCpf("123.456.789-00");
        
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("João Silva");
        assertThat(encontrado.get().getCpf()).isEqualTo("123.456.789-00");
    }
    
    @Test
    void deveRetornarVazioQuandoCpfNaoExiste() {
        Optional<Socio> encontrado = socioRepository.findByCpf("000.000.000-00");
        
        assertThat(encontrado).isEmpty();
    }
    
    @Test
    void deveBuscarSocioPorMatricula() {
        Optional<Socio> encontrado = socioRepository.findByMatricula("MAT001");
        
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNome()).isEqualTo("João Silva");
        assertThat(encontrado.get().getMatricula()).isEqualTo("MAT001");
    }
    
    @Test
    void deveRetornarVazioQuandoMatriculaNaoExiste() {
        Optional<Socio> encontrado = socioRepository.findByMatricula("MAT999");
        
        assertThat(encontrado).isEmpty();
    }
    
    @Test
    void deveVerificarSeExisteSocioPorCpf() {
        boolean existe = socioRepository.existsByCpf("123.456.789-00");
        boolean naoExiste = socioRepository.existsByCpf("000.000.000-00");
        
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }
    
    @Test
    void deveVerificarSeExisteSocioPorMatricula() {
        boolean existe = socioRepository.existsByMatricula("MAT001");
        boolean naoExiste = socioRepository.existsByMatricula("MAT999");
        
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }
    
    @Test
    void deveVerificarCpfDuplicadoExcluindoId() {
        Long idSocioAtivo = socioAtivo.getId();
        
        // Não deve encontrar duplicata quando é o mesmo sócio
        boolean duplicado = socioRepository.existsByCpfAndIdNot("123.456.789-00", idSocioAtivo);
        assertThat(duplicado).isFalse();
        
        // Deve encontrar duplicata quando é outro sócio
        duplicado = socioRepository.existsByCpfAndIdNot("123.456.789-00", 999L);
        assertThat(duplicado).isTrue();
    }
    
    @Test
    void deveVerificarMatriculaDuplicadaExcluindoId() {
        Long idSocioAtivo = socioAtivo.getId();
        
        // Não deve encontrar duplicata quando é o mesmo sócio
        boolean duplicado = socioRepository.existsByMatriculaAndIdNot("MAT001", idSocioAtivo);
        assertThat(duplicado).isFalse();
        
        // Deve encontrar duplicata quando é outro sócio
        duplicado = socioRepository.existsByMatriculaAndIdNot("MAT001", 999L);
        assertThat(duplicado).isTrue();
    }
    
    @Test
    void deveBuscarSociosPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Socio> resultado = socioRepository.findByNomeContainingIgnoreCase("joão", pageable);
        
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("João Silva");
    }
    
    @Test
    void deveBuscarSociosPorNomeCaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Socio> resultado = socioRepository.findByNomeContainingIgnoreCase("MARIA", pageable);
        
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("Maria Santos");
    }
    
    @Test
    void deveBuscarSociosPorStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Socio> ativos = socioRepository.findByStatus(StatusSocio.ATIVO, pageable);
        assertThat(ativos.getContent()).hasSize(1);
        assertThat(ativos.getContent().get(0).getNome()).isEqualTo("João Silva");
        
        Page<Socio> inativos = socioRepository.findByStatus(StatusSocio.INATIVO, pageable);
        assertThat(inativos.getContent()).hasSize(1);
        assertThat(inativos.getContent().get(0).getNome()).isEqualTo("Maria Santos");
    }
    
    @Test
    void deveBuscarSociosPorMultiplosCriterios() {
        Pageable pageable = PageRequest.of(0, 10);
        
        // Busca por nome
        Page<Socio> porNome = socioRepository.searchByMultipleCriteria("joão", pageable);
        assertThat(porNome.getContent()).hasSize(1);
        assertThat(porNome.getContent().get(0).getNome()).isEqualTo("João Silva");
        
        // Busca por CPF
        Page<Socio> porCpf = socioRepository.searchByMultipleCriteria("123.456", pageable);
        assertThat(porCpf.getContent()).hasSize(1);
        assertThat(porCpf.getContent().get(0).getCpf()).isEqualTo("123.456.789-00");
        
        // Busca por matrícula
        Page<Socio> porMatricula = socioRepository.searchByMultipleCriteria("MAT002", pageable);
        assertThat(porMatricula.getContent()).hasSize(1);
        assertThat(porMatricula.getContent().get(0).getMatricula()).isEqualTo("MAT002");
    }
    
    @Test
    void deveBuscarSociosPorNomeEStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Socio> resultado = socioRepository.findByNomeContainingIgnoreCaseAndStatus(
            "joão", StatusSocio.ATIVO, pageable);
        
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNome()).isEqualTo("João Silva");
        assertThat(resultado.getContent().get(0).getStatus()).isEqualTo(StatusSocio.ATIVO);
    }
    
    @Test
    void deveBuscarSociosPorCidade() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Socio> resultado = socioRepository.findByCidadeIgnoreCase("são paulo", pageable);
        
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getCidade()).isEqualTo("São Paulo");
    }
    
    @Test
    void deveBuscarSociosPorEstado() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Socio> resultado = socioRepository.findByEstadoIgnoreCase("sp", pageable);
        
        assertThat(resultado.getContent()).hasSize(2);
    }
    
    @Test
    void deveContarSociosPorStatus() {
        long ativos = socioRepository.countByStatus(StatusSocio.ATIVO);
        long inativos = socioRepository.countByStatus(StatusSocio.INATIVO);
        
        assertThat(ativos).isEqualTo(1);
        assertThat(inativos).isEqualTo(1);
    }
    
    @Test
    void deveSalvarNovoSocio() {
        Socio novoSocio = new Socio("Pedro Oliveira", "111.222.333-44", "MAT003");
        novoSocio.setEmail("pedro@email.com");
        novoSocio.setProfissao("Produtor Rural");
        
        Socio salvo = socioRepository.save(novoSocio);
        
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getNome()).isEqualTo("Pedro Oliveira");
        assertThat(salvo.getCriadoEm()).isNotNull();
        assertThat(salvo.getAtualizadoEm()).isNotNull();
        assertThat(salvo.getStatus()).isEqualTo(StatusSocio.ATIVO);
    }
    
    @Test
    void deveAtualizarSocioExistente() {
        Socio socio = socioRepository.findByCpf("123.456.789-00").orElseThrow();
        socio.setTelefone("(11) 99999-9999");
        socio.setEmail("joao.novo@email.com");
        
        Socio atualizado = socioRepository.save(socio);
        
        assertThat(atualizado.getTelefone()).isEqualTo("(11) 99999-9999");
        assertThat(atualizado.getEmail()).isEqualTo("joao.novo@email.com");
        assertThat(atualizado.getAtualizadoEm()).isNotNull();
    }
    
    @Test
    void deveExcluirSocio() {
        Long id = socioAtivo.getId();
        
        socioRepository.deleteById(id);
        entityManager.flush();
        
        Optional<Socio> encontrado = socioRepository.findById(id);
        assertThat(encontrado).isEmpty();
    }
}
