package com.sindicato.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a entidade Usuario.
 */
class UsuarioTest {
    
    @Test
    void deveCriarUsuarioComConstrutorPadrao() {
        Usuario usuario = new Usuario();
        
        assertThat(usuario).isNotNull();
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
    }
    
    @Test
    void deveCriarUsuarioComConstrutorParametrizado() {
        Usuario usuario = new Usuario("admin", "senha123", "Administrador");
        
        assertThat(usuario.getUsername()).isEqualTo("admin");
        assertThat(usuario.getPassword()).isEqualTo("senha123");
        assertThat(usuario.getNome()).isEqualTo("Administrador");
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
    }
    
    @Test
    void deveDefinirStatusPadraComoATIVO() {
        Usuario usuario = new Usuario();
        
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.ATIVO);
    }
    
    @Test
    void devePermitirAlterarStatus() {
        Usuario usuario = new Usuario("admin", "senha123", "Administrador");
        usuario.setStatus(StatusUsuario.INATIVO);
        
        assertThat(usuario.getStatus()).isEqualTo(StatusUsuario.INATIVO);
    }
    
    @Test
    void deveImplementarEqualsCorretamente() {
        Usuario usuario1 = new Usuario("admin", "senha123", "Administrador");
        usuario1.setId(1L);
        
        Usuario usuario2 = new Usuario("admin", "senha123", "Administrador");
        usuario2.setId(1L);
        
        Usuario usuario3 = new Usuario("outro", "senha456", "Outro");
        usuario3.setId(2L);
        
        assertThat(usuario1).isEqualTo(usuario2);
        assertThat(usuario1).isNotEqualTo(usuario3);
    }
    
    @Test
    void deveImplementarHashCodeCorretamente() {
        Usuario usuario1 = new Usuario("admin", "senha123", "Administrador");
        usuario1.setId(1L);
        
        Usuario usuario2 = new Usuario("admin", "senha123", "Administrador");
        usuario2.setId(1L);
        
        assertThat(usuario1.hashCode()).isEqualTo(usuario2.hashCode());
    }
    
    @Test
    void deveGerarToStringComInformacoesRelevantes() {
        Usuario usuario = new Usuario("admin", "senha123", "Administrador");
        usuario.setId(1L);
        
        String toString = usuario.toString();
        
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("username='admin'");
        assertThat(toString).contains("nome='Administrador'");
        assertThat(toString).contains("status=ATIVO");
        assertThat(toString).doesNotContain("senha123"); // Senha não deve aparecer no toString
    }
}
