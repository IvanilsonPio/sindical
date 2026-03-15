package com.sindicato.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sindicato.model.Usuario;
import com.sindicato.repository.UsuarioRepository;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        boolean ativo = usuario.getStatus() == com.sindicato.model.StatusUsuario.ATIVO;

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                ativo,        // enabled
                true,         // accountNonExpired
                true,         // credentialsNonExpired
                true,         // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name()))
        );
    }
}
