// Punto de entrada principal de AirTortilla - Entrega 2
public class Main {

    public static void main(String[] args) {
        System.out.println("Iniciando AirTortilla (Entrega 2)...");

        // 1. Inicializar base de datos SQLite con schema.sql y data.sql
        ConexionBD.inicializarBaseDatos();

        // 2. Migrar las reservas de los ficheros CSV existentes a la base de datos
        MigracionDatos.ejecutarMigracion();

        // 3. Arrancar la aplicacion con la interfaz web en el puerto 8080
        ServidorWeb.iniciar(8080);
    }
}
