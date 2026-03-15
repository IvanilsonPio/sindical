package com.sindicato.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sindicato.dto.UsuarioRequest;
import com.sindicato.dto.UsuarioResponse;
import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import com.sindicato.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> response = usuarioService.listarTodos().stream()
            .map(UsuarioResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(new UsuarioResponse(usuarioService.buscar(id)));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioResponse(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        Usuario usuario = usuarioService.atualizar(id, request);
        return ResponseEntity.ok(new UsuarioResponse(usuario));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        StatusUsuario novoStatus = StatusUsuario.valueOf(body.get("status"));
        usuarioService.alterarStatus(id, novoStatus, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        usuarioService.excluir(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
