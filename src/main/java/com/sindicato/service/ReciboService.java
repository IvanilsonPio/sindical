package com.sindicato.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.Pagamento;
import com.sindicato.model.Socio;
import com.sindicato.repository.PagamentoRepository;

/**
 * Service for generating PDF receipts for payments.
 * Implements requirements 4.1, 4.2, and 4.3 for receipt generation.
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
     * 
     * @param pagamentoId Payment ID
     * @return PDF file as byte array
     * @throws ResourceNotFoundException if payment not found
     */
    public byte[] gerarRecibo(Long pagamentoId) {
        return gerarRecibo(pagamentoId, false);
    }

    /**
     * Generates and saves a PDF receipt for a payment permanently.
     * Implements requirement 4.4: Permanent storage of receipts.
     *
     * @param pagamentoId Payment ID
     * @return Path to the saved receipt file
     * @throws ResourceNotFoundException if payment not found
     */
    @Transactional
    public String gerarESalvarRecibo(Long pagamentoId) {
        logger.info("Generating and saving receipt for payment: {}", pagamentoId);

        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento", "id", pagamentoId));

        try {
            // Generate PDF
            byte[] pdfBytes = criarPdfRecibo(pagamento, false);

            // Create file path
            Path recibosDir = Paths.get(uploadDir).resolve("recibos").toAbsolutePath().normalize();
            Files.createDirectories(recibosDir);

            String fileName = String.format("recibo-%s.pdf", pagamento.getNumeroRecibo());
            Path filePath = recibosDir.resolve(fileName);

            // Save file
            Files.write(filePath, pdfBytes);
            logger.info("Receipt saved successfully at: {}", filePath);

            // Update payment with receipt path
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
     *
     * @param caminhoRecibo Relative path to the receipt file
     * @return PDF file as byte array
     * @throws IOException if file cannot be read
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
    
    /**
     * Generates a PDF receipt for a payment with option for second copy.
     * Implements requirements 4.2 and 4.3.
     * 
     * @param pagamentoId Payment ID
     * @param segundaVia true for second copy, false for original
     * @return PDF file as byte array
     * @throws ResourceNotFoundException if payment not found
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
     * Creates the PDF document for the receipt.
     * Includes all mandatory data as per requirement 4.2.
     * 
     * @param pagamento Payment entity
     * @param segundaVia true for second copy marking
     * @return PDF as byte array
     */
    private byte[] criarPdfRecibo(Pagamento pagamento, boolean segundaVia) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        
        // Set margins
        document.setMargins(50, 50, 50, 50);
        
        Socio socio = pagamento.getSocio();
        
        // Add header
        adicionarCabecalho(document, segundaVia);
        
        // Add receipt number and date
        adicionarNumeroEData(document, pagamento);
        
        // Add member information
        adicionarDadosSocio(document, socio);
        
        // Add payment information
        adicionarDadosPagamento(document, pagamento);
        
        // Add footer
        adicionarRodape(document, pagamento);
        
        document.close();
        
        logger.info("PDF receipt created successfully for payment: {}", pagamento.getId());
        return baos.toByteArray();
    }
    
    /**
     * Adds header section to the receipt.
     */
    private void adicionarCabecalho(Document document, boolean segundaVia) {
        // Title
        Paragraph titulo = new Paragraph("SINDICATO DOS TRABALHADORES RURAIS")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(titulo);
        
        Paragraph subtitulo = new Paragraph("RECIBO DE PAGAMENTO")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(subtitulo);
        
        // Second copy marking (Requirement 4.3)
        if (segundaVia) {
            Paragraph marcaSegundaVia = new Paragraph("*** SEGUNDA VIA ***")
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(ColorConstants.RED)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(marcaSegundaVia);
        } else {
            document.add(new Paragraph(" ").setMarginBottom(15));
        }
    }
    
    /**
     * Adds receipt number and date section.
     */
    private void adicionarNumeroEData(Document document, Pagamento pagamento) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(15);
        
        // Receipt number
        Cell cellNumero = new Cell()
                .add(new Paragraph("Recibo Nº: " + pagamento.getNumeroRecibo())
                        .setFontSize(11)
                        .setBold())
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT);
        
        // Date
        Cell cellData = new Cell()
                .add(new Paragraph("Data: " + pagamento.getDataPagamento().format(DATE_FORMATTER))
                        .setFontSize(11)
                        .setBold())
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        
        table.addCell(cellNumero);
        table.addCell(cellData);
        
        document.add(table);
    }
    
    /**
     * Adds member information section (Requirement 4.2).
     */
    private void adicionarDadosSocio(Document document, Socio socio) {
        // Section title
        Paragraph tituloSecao = new Paragraph("DADOS DO SÓCIO")
                .setFontSize(12)
                .setBold()
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setPadding(5)
                .setMarginBottom(10);
        document.add(tituloSecao);
        
        // Member data table
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(15);
        
        adicionarLinhaDados(table, "Nome:", socio.getNome());
        adicionarLinhaDados(table, "CPF:", socio.getCpf());
        adicionarLinhaDados(table, "Matrícula:", socio.getMatricula());
        
        if (socio.getTelefone() != null && !socio.getTelefone().isEmpty()) {
            adicionarLinhaDados(table, "Telefone:", socio.getTelefone());
        }
        
        if (socio.getEndereco() != null && !socio.getEndereco().isEmpty()) {
            String enderecoCompleto = socio.getEndereco();
            if (socio.getCidade() != null && !socio.getCidade().isEmpty()) {
                enderecoCompleto += ", " + socio.getCidade();
                if (socio.getEstado() != null && !socio.getEstado().isEmpty()) {
                    enderecoCompleto += " - " + socio.getEstado();
                }
            }
            adicionarLinhaDados(table, "Endereço:", enderecoCompleto);
        }
        
        document.add(table);
    }
    
    /**
     * Adds payment information section (Requirement 4.2).
     */
    private void adicionarDadosPagamento(Document document, Pagamento pagamento) {
        // Section title
        Paragraph tituloSecao = new Paragraph("DADOS DO PAGAMENTO")
                .setFontSize(12)
                .setBold()
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setPadding(5)
                .setMarginBottom(10);
        document.add(tituloSecao);
        
        // Payment data table
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth()
                .setMarginBottom(15);
        
        String periodo = MESES[pagamento.getMes()] + "/" + pagamento.getAno();
        adicionarLinhaDados(table, "Período:", periodo);
        adicionarLinhaDados(table, "Valor:", formatarValor(pagamento.getValor()));
        adicionarLinhaDados(table, "Data do Pagamento:", pagamento.getDataPagamento().format(DATE_FORMATTER));
        
        if (pagamento.getObservacoes() != null && !pagamento.getObservacoes().isEmpty()) {
            adicionarLinhaDados(table, "Observações:", pagamento.getObservacoes());
        }
        
        document.add(table);
        
        // Highlight total value
        Table tableTotal = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginTop(10)
                .setMarginBottom(20);
        
        Cell cellLabel = new Cell()
                .add(new Paragraph("VALOR TOTAL PAGO:")
                        .setFontSize(12)
                        .setBold())
                .setBackgroundColor(new DeviceRgb(240, 240, 240))
                .setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(new SolidBorder(1));
        
        Cell cellValor = new Cell()
                .add(new Paragraph(formatarValor(pagamento.getValor()))
                        .setFontSize(12)
                        .setBold())
                .setBackgroundColor(new DeviceRgb(255, 255, 200))
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(new SolidBorder(1));
        
        tableTotal.addCell(cellLabel);
        tableTotal.addCell(cellValor);
        
        document.add(tableTotal);
    }
    
    /**
     * Adds footer section with signature line.
     */
    private void adicionarRodape(Document document, Pagamento pagamento) {
        // Add some space
        document.add(new Paragraph(" ").setMarginTop(30));
        
        // Signature line
        Paragraph assinatura = new Paragraph("_".repeat(50))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        document.add(assinatura);
        
        Paragraph textoAssinatura = new Paragraph("Assinatura do Responsável")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(textoAssinatura);
        
        // Generation info
        Paragraph rodape = new Paragraph(
                "Recibo gerado em " + LocalDate.now().format(DATE_FORMATTER) + 
                " | Sistema de Gerenciamento do Sindicato Rural")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(ColorConstants.GRAY);
        document.add(rodape);
    }
    
    /**
     * Helper method to add a data row to a table.
     */
    private void adicionarLinhaDados(Table table, String label, String valor) {
        Cell cellLabel = new Cell()
                .add(new Paragraph(label)
                        .setFontSize(10)
                        .setBold())
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5);
        
        Cell cellValor = new Cell()
                .add(new Paragraph(valor)
                        .setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPaddingBottom(5);
        
        table.addCell(cellLabel);
        table.addCell(cellValor);
    }
    
    /**
     * Formats a BigDecimal value as Brazilian currency.
     */
    private String formatarValor(BigDecimal valor) {
        return String.format("R$ %,.2f", valor).replace(",", "X").replace(".", ",").replace("X", ".");
    }
}
