package com.sindicato.repository;

import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para o UsuarioRepository.
 * Utiliza banco de dados em memória (H2) para testes.
 */
@DataJpaTest
class UsuarioRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private Usuario usuarioAtivo;
    private Usuario usuarioInativo;
    
    @BeforeEach
    void setUp() {
        usuarioAtivo = new Usuario("admin", "senha123", "Administrador");
        usuarioAtivo.setStatus(StatusUsuario.ATIVO);
        
        usuarioInativo = new Usuario("inativo", "senha456", "Usuário Inativo");
        usuarioInativo.setStatus(StatusUsuario.INATIVO);
        
        entityManager.persist(usuarioAtivo);
        entityManager.persist(usuarioInativo);
        entityManager.flush();
    }
    
    @Test
    void deveBuscarUsuarioPorUsername() {
        Optional<Usuario> encontrado = usuarioRepository.findByUsername("admin");
        
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("admin");
        assertThat(encontrado.get().getNome()).isEqualTo("Administrador");
    }
    
    @Test
    void deveRetornarVazioQuandoUsernameNaoExiste() {
        Optional<Usuario> encontrado = usuarioRepository.findByUsername("naoexiste");
        
        assertThat(encontrado).isEmpty();
    }
    
    @Test
    void deveBuscarUsuarioPorUsernameEStatus() {
        Optional<Usuario> encontrado = usuarioRepository.findByUsernameAndStatus("admin", StatusUsuario.ATIVO);
        
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getUsername()).isEqualTo("admin");
        assertThat(encontrado.get().getStatus()).isEqualTo(StatusUsuario.ATIVO);
    }
    
    @Test
    void deveRetornarVazioQuandoUsernameExisteMasStatusDiferente() {
        Optional<Usuario> encontrado = usuarioRepository.findByUsernameAndStatus("admin", StatusUsuario.INATIVO);
        
        assertThat(encontrado).isEmpty();
    }
    
    @Test
    void deveBuscarTodosUsuariosPorStatus() {
        List<Usuario> ativos = usuarioRepository.findByStatus(StatusUsuario.ATIVO);
        List<Usuario> inativos = usuarioRepository.findByStatus(StatusUsuario.INATIVO);
        
        assertThat(ativos).hasSize(1);
        assertThat(ativos.get(0).getUsername()).isEqualTo("admin");
        
        assertThat(inativos).hasSize(1);
        assertThat(inativos.get(0).getUsername()).isEqualTo("inativo");
    }
    
    @Test
    void deveVerificarSeUsernameExiste() {
        boolean existe = usuarioRepository.existsByUsername("admin");
        boolean naoExiste = usuarioRepository.existsByUsername("naoexiste");
        
        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }
    
    @Test
    void deveSalvarNovoUsuario() {
        Usuario novoUsuario = new Usuario("novo", "senha789", "Novo Usuário");
        
        Usuario salvo = usuarioRepository.save(novoUsuario);
        
        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getUsername()).isEqualTo("novo");
        assertThat(salvo.getCriadoEm()).isNotNull();
        assertThat(salvo.getAtualizadoEm()).isNotNull();
    }
    
    @Test
    void deveAtualizarUsuarioExistente() {
        Usuario usuario = usuarioRepository.findByUsername("admin").orElseThrow();
        usuario.setNome("Administrador Atualizado");
        
        Usuario atualizado = usuarioRepository.save(usuario);
        
        assertThat(atualizado.getNome()).isEqualTo("Administrador Atualizado");
        assertThat(atualizado.getAtualizadoEm()).isNotNull();
    }
}
