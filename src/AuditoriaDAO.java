import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Clase para registrar y consultar eventos de auditoria en la base de datos
public class AuditoriaDAO {

    // Estructura para guardar en memoria los datos de un registro de auditoria
    public static class LogInfo {
        public int id;
        public String nombreUsuario;
        public String accion;
        public String recurso;
        public String fechaHora;

        public LogInfo(int id, String nombreUsuario, String accion, String recurso, String fechaHora) {
            this.id = id;
            this.nombreUsuario = nombreUsuario;
            this.accion = accion;
            this.recurso = recurso;
            this.fechaHora = fechaHora;
        }
    }

    // Inserta un nuevo evento en la tabla de auditoria
    public static void registrar(int usuarioId, String accion, String recurso) {
        String sql = "INSERT INTO auditoria (usuario_id, accion, recurso, fecha_hora) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setString(2, accion);
            ps.setString(3, recurso);
            ps.setString(4, LocalDateTime.now().toString());

            ps.executeUpdate();

        } catch (Exception e) {
            System.err.println("Error al registrar auditoria: " + e.getMessage());
        }
    }

    // Devuelve todos los eventos ordenados del mas reciente al mas antiguo
    public static List<LogInfo> listarTodos() {
        List<LogInfo> logs = new ArrayList<>();
        String sql = "SELECT a.id, u.nombre_usuario, a.accion, a.recurso, a.fecha_hora "
                   + "FROM auditoria a "
                   + "JOIN usuarios u ON a.usuario_id = u.id "
                   + "ORDER BY a.id DESC";

        try (Connection con = ConexionBD.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(new LogInfo(
                        rs.getInt("id"),
                        rs.getString("nombre_usuario"),
                        rs.getString("accion"),
                        rs.getString("recurso"),
                        rs.getString("fecha_hora")
                ));
            }

        } catch (Exception e) {
            System.err.println("Error al listar auditoria: " + e.getMessage());
        }
        return logs;
    }
}

