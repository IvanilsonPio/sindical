package com.sindicato.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sindicato.model.Socio;
import com.sindicato.model.StatusSocio;

/**
 * DTO para resposta detalhada de sócio.
 * Inclui todos os dados pessoais, endereço, contato, status, 
 * histórico de pagamentos e arquivos vinculados.
 * 
 * Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7
 */
/**
 * DTO para resposta detalhada de sócio.
 * Inclui todos os dados pessoais, endereço, contato, status,
 * histórico de pagamentos, arquivos vinculados e histórico de alterações.
 *
 * Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 5.5
 */
public class SocioDetalhadoResponse {

    // Identificação
    private Long id;

    // Dados pessoais
    private String nome;
    private String cpf;
    private String matricula;
    private String rg;
    private LocalDate dataNascimento;
    private String profissao;

    // Endereço
    private String cep;
    private String endereco;
    private String cidade;
    private String estado;

    // Contato
    private String telefone;
    private String email;

    // Status e metadados
    private StatusSocio status;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Relacionamentos
    private List<PagamentoResponse> pagamentos = new ArrayList<>();
    private List<ArquivoResponse> arquivos = new ArrayList<>();
    private List<HistoricoAlteracaoResponse> historico = new ArrayList<>();

    // Construtores

    public SocioDetalhadoResponse() {
    }

    /**
     * Construtor que converte uma entidade Socio para o DTO detalhado.
     * Os pagamentos e arquivos são ordenados por data decrescente.
     *
     * @param socio Entidade Socio com relacionamentos carregados
     */
    public SocioDetalhadoResponse(Socio socio) {
        this.id = socio.getId();

        // Dados pessoais
        this.nome = socio.getNome();
        this.cpf = socio.getCpf();
        this.matricula = socio.getMatricula();
        this.rg = socio.getRg();
        this.dataNascimento = socio.getDataNascimento();
        this.profissao = socio.getProfissao();

        // Endereço
        this.cep = socio.getCep();
        this.endereco = socio.getEndereco();
        this.cidade = socio.getCidade();
        this.estado = socio.getEstado();

        // Contato
        this.telefone = socio.getTelefone();
        this.email = socio.getEmail();

        // Status e metadados
        this.status = socio.getStatus();
        this.criadoEm = socio.getCriadoEm();
        this.atualizadoEm = socio.getAtualizadoEm();

        // Relacionamentos - ordenados por data decrescente
        if (socio.getPagamentos() != null) {
            this.pagamentos = socio.getPagamentos().stream()
                    .map(PagamentoResponse::new)
                    .sorted((p1, p2) -> p2.getDataPagamento().compareTo(p1.getDataPagamento()))
                    .collect(Collectors.toList());
        }

        if (socio.getArquivos() != null) {
            this.arquivos = socio.getArquivos().stream()
                    .map(ArquivoResponse::new)
                    .sorted((a1, a2) -> a2.getCriadoEm().compareTo(a1.getCriadoEm()))
                    .collect(Collectors.toList());
        }
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StatusSocio getStatus() {
        return status;
    }

    public void setStatus(StatusSocio status) {
        this.status = status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public List<PagamentoResponse> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<PagamentoResponse> pagamentos) {
        this.pagamentos = pagamentos;
    }

    public List<ArquivoResponse> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<ArquivoResponse> arquivos) {
        this.arquivos = arquivos;
    }

    public List<HistoricoAlteracaoResponse> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoAlteracaoResponse> historico) {
        this.historico = historico;
    }
}
