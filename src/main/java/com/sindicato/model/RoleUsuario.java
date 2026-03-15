package com.sindicato.model;

/**
 * Enum que representa os perfis de acesso de um usuário no sistema.
 */
public enum RoleUsuario {
    /**
     * Administrador - acesso total, incluindo gestão de usuários
     */
    ADMIN,

    /**
     * Operador - acesso às funcionalidades do sistema, exceto gestão de usuários
     */
    OPERADOR
}
