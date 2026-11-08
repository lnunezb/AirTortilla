import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Clase para realizar operaciones de base de datos sobre usuarios y sesiones
public class UsuarioDAO {

    // Representa un usuario en memoria
    public static class UsuarioInfo {
        public int id;
        public String nombreUsuario;
        public int rolId;
        public String rolNombre;
        public boolean activo;

        public UsuarioInfo(int id, String nombreUsuario, int rolId, String rolNombre, boolean activo) {
            this.id = id;
            this.nombreUsuario = nombreUsuario;
            this.rolId = rolId;
            this.rolNombre = rolNombre;
            this.activo = activo;
        }

        public boolean esAdmin() {
            return rolId == 1 || "ADMINISTRADOR".equalsIgnoreCase(rolNombre);
        }
    }

    // Valida las credenciales de un usuario y registra el inicio de sesión
    public static UsuarioInfo autenticar(String username, String passwordPlana) {
        String sql = "SELECT u.id, u.nombre_usuario, u.contrasena, u.rol_id, r.nombre, u.activo "
                   + "FROM usuarios u "
                   + "JOIN roles r ON u.rol_id = r.id "
                   + "WHERE u.nombre_usuario = ? AND u.activo = 1";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("contrasena");

                    // Comprobación segura mediante hash SHA-256
                    if (SeguridadUtil.verificar(passwordPlana, hashGuardado)) {
                        int idUsuario = rs.getInt("id");
                        UsuarioInfo usuario = new UsuarioInfo(
                                idUsuario,
                                rs.getString("nombre_usuario"),
                                rs.getInt("rol_id"),
                                rs.getString("nombre"),
                                rs.getInt("activo") == 1
                        );

                        // Registrar sesión abierta en la tabla sesiones
                        registrarInicioSesion(idUsuario);

                        return usuario;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error en autenticacion: " + e.getMessage());
        }
        return null; // Credenciales incorrectas o usuario inactivo
    }

    // Registra en la base de datos la apertura de sesión
    private static void registrarInicioSesion(int usuarioId) {
        String sql = "INSERT INTO sesiones (usuario_id, fecha_inicio) VALUES (?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error al registrar inicio de sesion: " + e.getMessage());
        }
    }

    // Registra en la base de datos el cierre de sesión
    public static void registrarCierreSesion(int usuarioId) {
        String sql = "UPDATE sesiones SET fecha_fin = ? WHERE usuario_id = ? AND fecha_fin IS NULL";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setInt(2, usuarioId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error al registrar fin de sesion: " + e.getMessage());
        }
    }

    // Permite al Administrador dar de alta un nuevo usuario con contraseña hasheada
    public static boolean crearUsuario(String username, String passwordPlana, int rolId) {
        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena, rol_id, activo) VALUES (?, ?, ?, 1)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, SeguridadUtil.hashear(passwordPlana)); // Siempre hasheada
            ps.setInt(3, rolId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            return false;
        }
    }

    // Obtiene el listado completo de usuarios
    public static List<UsuarioInfo> listarTodos() {
        List<UsuarioInfo> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.nombre_usuario, u.rol_id, r.nombre, u.activo "
                   + "FROM usuarios u "
                   + "JOIN roles r ON u.rol_id = r.id ORDER BY u.id ASC";

        try (Connection con = ConexionBD.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new UsuarioInfo(
                        rs.getInt("id"),
                        rs.getString("nombre_usuario"),
                        rs.getInt("rol_id"),
                        rs.getString("nombre"),
                        rs.getInt("activo") == 1
                ));
            }
        } catch (Exception e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }
}

