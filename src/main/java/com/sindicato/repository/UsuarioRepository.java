package com.sindicato.repository;

import com.sindicato.model.StatusUsuario;
import com.sindicato.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de persistência da entidade Usuario.
 * Fornece métodos de busca customizados para autenticação e gerenciamento de usuários.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    /**
     * Busca um usuário pelo username.
     * Utilizado principalmente para autenticação.
     *
     * @param username o username do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> findByUsername(String username);
    
    /**
     * Busca um usuário pelo username e status.
     * Útil para verificar se um usuário está ativo antes de autenticar.
     *
     * @param username o username do usuário
     * @param status o status do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<Usuario> findByUsernameAndStatus(String username, StatusUsuario status);
    
    /**
     * Busca todos os usuários com um determinado status.
     *
     * @param status o status dos usuários a buscar
     * @return lista de usuários com o status especificado
     */
    List<Usuario> findByStatus(StatusUsuario status);
    
    /**
     * Verifica se existe um usuário com o username especificado.
     * Útil para validação de duplicação durante cadastro.
     *
     * @param username o username a verificar
     * @return true se existe um usuário com esse username
     */
    boolean existsByUsername(String username);
}
