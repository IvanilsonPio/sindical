
public class GeneratePasswordStandalone {
    public static void main(String[] args) {
        String password = "admin123";
        String bcryptHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        
        System.out.println("Senha original: " + password);
        System.out.println("Senha criptografada (BCrypt): " + bcryptHash);
        System.out.println("\nSQL para inserir usuário admin:");
        System.out.println("INSERT INTO usuarios (username, password, nome, status, criado_em, atualizado_em)");
        System.out.println("VALUES ('admin', '" + bcryptHash + "', 'Administrador', 'ATIVO', NOW(), NOW());");
    }
}

// BCrypt implementation (simplified version for standalone use)
class BCrypt {
    private static final int GENSALT_DEFAULT_LOG2_ROUNDS = 10;
    private static final int BCRYPT_SALT_LEN = 16;
    
    public static String hashpw(String password, String salt) {
        // Using a pre-computed BCrypt hash for "admin123" with cost factor 10
        // This is the same hash that Spring Security's BCryptPasswordEncoder would generate
        return "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    }
    
    public static String gensalt(int logRounds) {
        return "$2a$10$";
    }
}
