package com.sindicato.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sindicato.dto.ArquivoGeralResponse;
import com.sindicato.model.ArquivoGeral;
import com.sindicato.service.ArquivoGeralService;

@RestController
@RequestMapping("/api/arquivos-gerais")
public class ArquivoGeralController {
    
    private final ArquivoGeralService arquivoGeralService;
    
    public ArquivoGeralController(ArquivoGeralService arquivoGeralService) {
        this.arquivoGeralService = arquivoGeralService;
    }
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ArquivoGeralResponse>> uploadArquivos(
            @RequestParam(required = false) Long pastaId,
            @RequestParam("files") MultipartFile[] files) {
        
        List<ArquivoGeral> arquivos = arquivoGeralService.uploadArquivos(pastaId, files);
        List<ArquivoGeralResponse> response = arquivos.stream()
            .map(ArquivoGeralResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<ArquivoGeralResponse>> listarArquivos(
            @RequestParam(required = false) Long pastaId) {
        
        List<ArquivoGeral> arquivos = arquivoGeralService.listarArquivos(pastaId);
        List<ArquivoGeralResponse> response = arquivos.stream()
            .map(ArquivoGeralResponse::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ArquivoGeralResponse> buscarArquivo(@PathVariable Long id) {
        ArquivoGeral arquivo = arquivoGeralService.buscarArquivo(id);
        return ResponseEntity.ok(new ArquivoGeralResponse(arquivo));
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadArquivo(@PathVariable Long id) {
        ArquivoGeral arquivo = arquivoGeralService.buscarArquivo(id);
        Resource resource = arquivoGeralService.downloadArquivo(id);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(arquivo.getTipoConteudo()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + arquivo.getNomeOriginal() + "\"")
            .body(resource);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirArquivo(@PathVariable Long id) {
        arquivoGeralService.excluirArquivo(id);
        return ResponseEntity.noContent().build();
    }
}
