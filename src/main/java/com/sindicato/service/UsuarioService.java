package com.sindicato.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        usuario.setEmail(request.getEmail());

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
        usuario.setEmail(request.getEmail());

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

    @Transactional
    public void alterarPropriaSenha(String usernameLogado, com.sindicato.dto.AlterarSenhaRequest request) {
        if (!request.getNovaSenha().equals(request.getConfirmacaoSenha())) {
            throw new BusinessException("PASSWORD_MISMATCH", "Nova senha e confirmação não conferem");
        }

        Usuario usuario = usuarioRepository.findByUsername(usernameLogado)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", usernameLogado));

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getPassword())) {
            throw new BusinessException("WRONG_PASSWORD", "Senha atual incorreta");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void resetarSenha(Long id, com.sindicato.dto.ResetarSenhaRequest request) {
        Usuario usuario = buscar(id);
        usuario.setPassword(passwordEncoder.encode(request.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void solicitarRecuperacaoSenha(String email) {
        // Não revela se o e-mail existe ou não por segurança
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            if (usuario.getStatus() != StatusUsuario.ATIVO) return;

            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            usuarioRepository.save(usuario);

            emailService.enviarRecuperacaoSenha(email, usuario.getNome(), token);
        });
    }

    @Transactional
    public void redefinirSenhaComToken(String token, String novaSenha) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
            .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Token inválido ou expirado"));

        if (usuario.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(usuario.getResetTokenExpiry())) {
            throw new BusinessException("EXPIRED_TOKEN", "Token expirado. Solicite uma nova recuperação de senha.");
        }

        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);
    }
}
