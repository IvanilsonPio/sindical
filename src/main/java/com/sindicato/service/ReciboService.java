package com.sindicato.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Pagamento;
import com.sindicato.model.Socio;
import com.sindicato.repository.PagamentoRepository;

/**
 * Service for generating PDF receipts for payments.
 * Uses Flying Saucer + OpenPDF (LGPL) instead of iText 7 (AGPL).
 *
 * Requirements:
 * - 4.1: Generate receipt with unique sequential numbering (handled by PagamentoService)
 * - 4.2: Include all mandatory data: member info, amount, date, receipt number
 * - 4.3: Support second copy (segunda via) generation
 */
@Service
@Transactional(readOnly = true)
public class ReciboService {

    private static final Logger logger = LoggerFactory.getLogger(ReciboService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
    private static final String[] MESES = {
        "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    };

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private final PagamentoRepository pagamentoRepository;

    public ReciboService(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    /**
     * Generates a PDF receipt for a payment (original copy).
     */
    public byte[] gerarRecibo(Long pagamentoId) {
        return gerarRecibo(pagamentoId, false);
    }

    /**
     * Generates a PDF receipt for a payment with option for second copy.
     * Implements requirements 4.2 and 4.3.
     */
    public byte[] gerarRecibo(Long pagamentoId, boolean segundaVia) {
        logger.info("Generating {} receipt for payment: {}",
                segundaVia ? "second copy" : "original", pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "id", pagamentoId));

        try {
            return criarPdfRecibo(pagamento, segundaVia);
        } catch (Exception e) {
            logger.error("Error generating PDF receipt for payment: {}", pagamentoId, e);
            throw new RuntimeException("Erro ao gerar recibo em PDF", e);
        }
    }

    /**
     * Generates and saves a PDF receipt permanently.
     * Implements requirement 4.4: Permanent storage of receipts.
     */
    @Transactional
    public String gerarESalvarRecibo(Long pagamentoId) {
        logger.info("Generating and saving receipt for payment: {}", pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "id", pagamentoId));

        try {
            byte[] pdfBytes = criarPdfRecibo(pagamento, false);

            Path recibosDir = Paths.get(uploadDir).resolve("recibos").toAbsolutePath().normalize();
            Files.createDirectories(recibosDir);

            String fileName = String.format("recibo-%s.pdf", pagamento.getNumeroRecibo());
            Path filePath = recibosDir.resolve(fileName);
            Files.write(filePath, pdfBytes);
            logger.info("Receipt saved successfully at: {}", filePath);

            String relativePath = "recibos/" + fileName;
            pagamento.setCaminhoRecibo(relativePath);
            pagamentoRepository.save(pagamento);

            return relativePath;
        } catch (Exception e) {
            logger.error("Error generating and saving PDF receipt for payment: {}", pagamentoId, e);
            throw new RuntimeException("Erro ao gerar e salvar recibo em PDF", e);
        }
    }

    /**
     * Loads a saved receipt from disk.
     * Implements requirement 4.5: Allow download and reprint of receipts.
     */
    public byte[] carregarRecibo(String caminhoRecibo) throws IOException {
        logger.info("Loading receipt from: {}", caminhoRecibo);

        Path filePath = Paths.get(uploadDir).resolve(caminhoRecibo).toAbsolutePath().normalize();

        if (!Files.exists(filePath)) {
            logger.warn("Receipt file not found: {}", filePath);
            throw new ResourceNotFoundException("Recibo", "caminho", caminhoRecibo);
        }

        return Files.readAllBytes(filePath);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private byte[] criarPdfRecibo(Pagamento pagamento, boolean segundaVia) throws Exception {
        String html = montarHtml(pagamento, segundaVia);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(baos);

        logger.info("PDF receipt created successfully for payment: {}", pagamento.getId());
        return baos.toByteArray();
    }

    private String montarHtml(Pagamento pagamento, boolean segundaVia) throws IOException {
        Socio socio = pagamento.getSocio();

        String template = carregarTemplate();

        // Monta endereço completo
        String endereco = "";
        if (socio.getEndereco() != null && !socio.getEndereco().isEmpty()) {
            endereco = socio.getEndereco();
            if (socio.getCidade() != null && !socio.getCidade().isEmpty()) {
                endereco += ", " + socio.getCidade();
                if (socio.getEstado() != null && !socio.getEstado().isEmpty()) {
                    endereco += " - " + socio.getEstado();
                }
            }
        }

        String periodo = MESES[pagamento.getMes()] + "/" + pagamento.getAno();

        // Substitui variáveis no template
        return template
            .replace("[[${segundaVia}]]",       String.valueOf(segundaVia))
            .replace("th:if=\"${segundaVia}\"",  segundaVia ? "" : "style=\"display:none\"")
            .replace("th:unless=\"${segundaVia}\"", segundaVia ? "style=\"display:none\"" : "")
            .replace("th:if=\"${telefone != null and !#strings.isEmpty(telefone)}\"",
                     (socio.getTelefone() != null && !socio.getTelefone().isEmpty()) ? "" : "style=\"display:none\"")
            .replace("th:if=\"${endereco != null and !#strings.isEmpty(endereco)}\"",
                     !endereco.isEmpty() ? "" : "style=\"display:none\"")
            .replace("th:if=\"${observacoes != null and !#strings.isEmpty(observacoes)}\"",
                     (pagamento.getObservacoes() != null && !pagamento.getObservacoes().isEmpty()) ? "" : "style=\"display:none\"")
            .replace("[[${numeroRecibo}]]",  escapeXml(pagamento.getNumeroRecibo()))
            .replace("[[${dataPagamento}]]", pagamento.getDataPagamento().format(DATE_FORMATTER))
            .replace("[[${nome}]]",          escapeXml(socio.getNome()))
            .replace("[[${cpf}]]",           escapeXml(socio.getCpf()))
            .replace("[[${matricula}]]",     escapeXml(socio.getMatricula()))
            .replace("[[${telefone}]]",      escapeXml(socio.getTelefone() != null ? socio.getTelefone() : ""))
            .replace("[[${endereco}]]",      escapeXml(endereco))
            .replace("[[${periodo}]]",       escapeXml(periodo))
            .replace("[[${valor}]]",         formatarValor(pagamento.getValor()))
            .replace("[[${observacoes}]]",   escapeXml(pagamento.getObservacoes() != null ? pagamento.getObservacoes() : ""))
            .replace("[[${dataGeracao}]]",   LocalDate.now().format(DATE_FORMATTER));
    }

    private String carregarTemplate() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/templates/recibo.html")) {
            if (is == null) {
                throw new IOException("Template recibo.html não encontrado");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String escapeXml(String value) {
        if (value == null) return "";
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
    }

    private String formatarValor(BigDecimal valor) {
        return String.format("R$ %,.2f", valor).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
