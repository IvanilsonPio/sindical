package com.sindicato.model;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários para a entidade Socio.
 */
class SocioTest {
    
    private Socio socio;
    
    @BeforeEach
    void setUp() {
        socio = new Socio("João Silva", "123.456.789-00", "MAT001");
    }
    
    @Test
    void deveCriarSocioComDadosBasicos() {
        assertThat(socio.getNome()).isEqualTo("João Silva");
        assertThat(socio.getCpf()).isEqualTo("123.456.789-00");
        assertThat(socio.getMatricula()).isEqualTo("MAT001");
        assertThat(socio.getStatus()).isEqualTo(StatusSocio.ATIVO);
    }
    
    @Test
    void deveDefinirEObterTodosCampos() {
        socio.setRg("12.345.678-9");
        socio.setDataNascimento(LocalDate.of(1980, 5, 15));
        socio.setTelefone("(11) 98765-4321");
        socio.setEmail("joao@email.com");
        socio.setEndereco("Rua das Flores, 123");
        socio.setCidade("São Paulo");
        socio.setEstado("SP");
        socio.setCep("01234-567");
        socio.setProfissao("Agricultor");
        socio.setStatus(StatusSocio.INATIVO);
        
        assertThat(socio.getRg()).isEqualTo("12.345.678-9");
        assertThat(socio.getDataNascimento()).isEqualTo(LocalDate.of(1980, 5, 15));
        assertThat(socio.getTelefone()).isEqualTo("(11) 98765-4321");
        assertThat(socio.getEmail()).isEqualTo("joao@email.com");
        assertThat(socio.getEndereco()).isEqualTo("Rua das Flores, 123");
        assertThat(socio.getCidade()).isEqualTo("São Paulo");
        assertThat(socio.getEstado()).isEqualTo("SP");
        assertThat(socio.getCep()).isEqualTo("01234-567");
        assertThat(socio.getProfissao()).isEqualTo("Agricultor");
        assertThat(socio.getStatus()).isEqualTo(StatusSocio.INATIVO);
    }
    
    @Test
    void deveInicializarListasDePagamentosEArquivos() {
        assertThat(socio.getPagamentos()).isNotNull();
        assertThat(socio.getPagamentos()).isEmpty();
        assertThat(socio.getArquivos()).isNotNull();
        assertThat(socio.getArquivos()).isEmpty();
    }
    
    @Test
    void deveCompararSociosPorIdECpf() {
        Socio socio1 = new Socio("João Silva", "123.456.789-00", "MAT001");
        socio1.setId(1L);
        
        Socio socio2 = new Socio("João Silva", "123.456.789-00", "MAT001");
        socio2.setId(1L);
        
        Socio socio3 = new Socio("Maria Santos", "987.654.321-00", "MAT002");
        socio3.setId(2L);
        
        assertThat(socio1).isEqualTo(socio2);
        assertThat(socio1).isNotEqualTo(socio3);
        assertThat(socio1.hashCode()).isEqualTo(socio2.hashCode());
    }
    
    @Test
    void deveGerarToStringComInformacoesBasicas() {
        socio.setId(1L);
        String toString = socio.toString();
        
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("nome='João Silva'");
        assertThat(toString).contains("cpf='123.456.789-00'");
        assertThat(toString).contains("matricula='MAT001'");
        assertThat(toString).contains("status=ATIVO");
    }
}
