package com.sindicato.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindicato.dto.UsuarioRequest;
import com.sindicato.exception.BusinessException;
import com.sindicato.exception.ResourceNotFoundException;
import com.sindicato.model.RoleUsuario;
import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import com.sindicato.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Transactional
    public Usuario criar(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("DUPLICATE_USERNAME", "Username já está em uso");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("PASSWORD_REQUIRED", "Senha é obrigatória ao criar usuário");
        }

        Usuario usuario = new Usuario(
            request.getUsername(),
            passwordEncoder.encode(request.getPassword()),
            request.getNome(),
            request.getRole()
        );

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioRequest request) {
        Usuario usuario = buscar(id);

        // Verifica duplicidade de username apenas se mudou
        if (!usuario.getUsername().equals(request.getUsername())
                && usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("DUPLICATE_USERNAME", "Username já está em uso");
        }

        usuario.setUsername(request.getUsername());
        usuario.setNome(request.getNome());
        usuario.setRole(request.getRole());

        // Só atualiza senha se foi fornecida
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarStatus(Long id, StatusUsuario novoStatus, String usernameLogado) {
        Usuario usuario = buscar(id);

        // Impede que o admin desative a si mesmo
        if (usuario.getUsername().equals(usernameLogado)) {
            throw new BusinessException("SELF_STATUS_CHANGE", "Não é possível alterar o próprio status");
        }

        // Garante que sempre exista pelo menos um admin ativo
        if (novoStatus == StatusUsuario.INATIVO && usuario.getRole() == RoleUsuario.ADMIN) {
            long adminsAtivos = usuarioRepository.findByStatus(StatusUsuario.ATIVO).stream()
                .filter(u -> u.getRole() == RoleUsuario.ADMIN)
                .count();
            if (adminsAtivos <= 1) {
                throw new BusinessException("LAST_ADMIN", "Não é possível desativar o último administrador");
            }
        }

        usuario.setStatus(novoStatus);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(Long id, String usernameLogado) {
        Usuario usuario = buscar(id);

        if (usuario.getUsername().equals(usernameLogado)) {
            throw new BusinessException("SELF_DELETE", "Não é possível excluir o próprio usuário");
        }

        if (usuario.getRole() == RoleUsuario.ADMIN) {
            long adminsAtivos = usuarioRepository.findByStatus(StatusUsuario.ATIVO).stream()
                .filter(u -> u.getRole() == RoleUsuario.ADMIN)
                .count();
            if (adminsAtivos <= 1) {
                throw new BusinessException("LAST_ADMIN", "Não é possível excluir o último administrador");
            }
        }

        usuarioRepository.delete(usuario);
    }
}
