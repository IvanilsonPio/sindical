package com.sindicato.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sindicato.dto.PastaRequest;
import com.sindicato.dto.PastaResponse;
import com.sindicato.model.Pasta;
import com.sindicato.service.PastaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pastas")
public class PastaController {
    
    private final PastaService pastaService;
    
    public PastaController(PastaService pastaService) {
        this.pastaService = pastaService;
    }
    
    @PostMapping
    public ResponseEntity<PastaResponse> criarPasta(@Valid @RequestBody PastaRequest request) {
        Pasta pasta = pastaService.criarPasta(request);
        long subpastas = pastaService.contarSubpastas(pasta.getId());
        long arquivos = pastaService.contarArquivos(pasta.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(new PastaResponse(pasta, subpastas, arquivos));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PastaResponse> buscarPasta(@PathVariable Long id) {
        Pasta pasta = pastaService.buscarPasta(id);
        long subpastas = pastaService.contarSubpastas(id);
        long arquivos = pastaService.contarArquivos(id);
        return ResponseEntity.ok(new PastaResponse(pasta, subpastas, arquivos));
    }
    
    @GetMapping("/raiz")
    public ResponseEntity<List<PastaResponse>> listarPastasRaiz() {
        List<Pasta> pastas = pastaService.listarPastasRaiz();
        List<PastaResponse> response = pastas.stream()
            .map(p -> new PastaResponse(p, 
                pastaService.contarSubpastas(p.getId()), 
                pastaService.contarArquivos(p.getId())))
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/subpastas")
    public ResponseEntity<List<PastaResponse>> listarSubpastas(@PathVariable Long id) {
        List<Pasta> subpastas = pastaService.listarSubpastas(id);
        List<PastaResponse> response = subpastas.stream()
            .map(p -> new PastaResponse(p, 
                pastaService.contarSubpastas(p.getId()), 
                pastaService.contarArquivos(p.getId())))
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PastaResponse> atualizarPasta(
            @PathVariable Long id,
            @Valid @RequestBody PastaRequest request) {
        Pasta pasta = pastaService.atualizarPasta(id, request);
        long subpastas = pastaService.contarSubpastas(id);
        long arquivos = pastaService.contarArquivos(id);
        return ResponseEntity.ok(new PastaResponse(pasta, subpastas, arquivos));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirPasta(@PathVariable Long id) {
        pastaService.excluirPasta(id);
        return ResponseEntity.noContent().build();
    }
}
