import java.security.MessageDigest;
import java.util.HexFormat;

// Utilidad para encriptar y verificar contraseñas usando SHA-256 nativo de Java
public class SeguridadUtil {

    // Convierte una contraseña en texto plano a su hash SHA-256
    public static String hashear(String textoPlano) {
        if (textoPlano == null) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(textoPlano.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    // Comprueba si la contraseña introducida coincide con el hash almacenado
    public static boolean verificar(String textoPlano, String hashGuardado) {
        if (textoPlano == null || hashGuardado == null) {
            return false;
        }
        return hashear(textoPlano).equalsIgnoreCase(hashGuardado);
    }
}
