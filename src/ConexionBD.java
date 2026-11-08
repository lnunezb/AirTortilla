import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Clase encargada de conectar con la base de datos SQLite y crear las tablas
public class ConexionBD {

    // Archivo local donde SQLite guardara la base de datos
    private static final String URL = "jdbc:sqlite:airtortilla.db";

    // Obtiene una conexion activa a la base de datos
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // Crea las tablas y carga los datos iniciales leyendo schema.sql y data.sql
    public static void inicializarBaseDatos() {
        try (Connection con = obtenerConexion();
             Statement stmt = con.createStatement()) {

            ejecutarScript(stmt, "schema.sql");
            ejecutarScript(stmt, "data.sql");
            System.out.println("-> Base de datos inicializada correctamente.");

        } catch (Exception e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
        }
    }

    // Lee un archivo SQL y ejecuta cada instruccion separada por punto y coma
    private static void ejecutarScript(Statement stmt, String rutaArchivo) throws Exception {
        if (!Files.exists(Paths.get(rutaArchivo))) return;

        String contenido = Files.readString(Paths.get(rutaArchivo));
        for (String sentencia : contenido.split(";")) {
            if (!sentencia.trim().isEmpty()) {
                stmt.execute(sentencia.trim());
            }
        }
    }
}
