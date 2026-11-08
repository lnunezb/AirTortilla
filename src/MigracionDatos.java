import java.io.BufferedReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// Clase que lee los ficheros AirTortilla_*.csv y los migra a la base de datos
public class MigracionDatos {

    // Ejecuta la migración si la tabla reservas aún no tiene datos
    public static void ejecutarMigracion() {
        if (yaHayReservasEnBaseDeDatos()) {
            System.out.println("-> Las reservas ya se encontraban migradas en la base de datos.");
            return;
        }

        System.out.println("-> Iniciando migracion de ficheros CSV a la base de datos relacional...");
        int totalInsertadas = 0;

        String sql = "INSERT OR IGNORE INTO reservas (localizador, origen, destino, vuelo, pasajero, fecha) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."), "AirTortilla_*.csv")) {

            for (Path archivoCsv : stream) {
                totalInsertadas += migrarFichero(archivoCsv, ps);
            }

            System.out.println("-> Migracion completada exitosamente. Total de reservas migradas: " + totalInsertadas);

        } catch (Exception e) {
            System.err.println("Error durante la migracion de CSV a la BD: " + e.getMessage());
        }
    }

    // Comprueba si la tabla de reservas ya contiene registros para no duplicar
    private static boolean yaHayReservasEnBaseDeDatos() {
        String sql = "SELECT COUNT(*) FROM reservas";
        try (Connection con = ConexionBD.obtenerConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            // Si la tabla aún no existe o hay error, asumimos que no hay datos
        }
        return false;
    }

    // Lee un fichero CSV concreto e inserta sus filas en la base de datos
    private static int migrarFichero(Path rutaCsv, PreparedStatement ps) {
        int contador = 0;
        try (BufferedReader br = Files.newBufferedReader(rutaCsv)) {
            String linea = br.readLine(); // Descartar cabecera

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(",");
                if (campos.length < 6) continue;

                ps.setString(1, campos[0].trim()); // localizador
                ps.setString(2, campos[1].trim()); // origen
                ps.setString(3, campos[2].trim()); // destino
                ps.setString(4, campos[3].trim()); // vuelo
                ps.setString(5, campos[4].trim()); // pasajero
                ps.setString(6, campos[5].trim()); // fecha

                ps.executeUpdate();
                contador++;
            }
        } catch (Exception e) {
            System.err.println("Error procesando fichero " + rutaCsv.getFileName() + ": " + e.getMessage());
        }
        return contador;
    }
}

