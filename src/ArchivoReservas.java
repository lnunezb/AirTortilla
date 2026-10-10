import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ArchivoReservas {

    public static void main(String[] args) {
        // Rutas relativas al directorio de ejecución del proyecto
        Path rutaCsv = Paths.get("MOCK_DATA (1).csv");
        Path rutaTxt = Paths.get("mi_archivo.txt");

        List<Reserva> listaReservas = new ArrayList<>();

        // Reservas de prueba
        listaReservas.add(new Reserva("LOC-001", "Madrid", "FR", "IB3401",
                new Persona("12345678A", "Carlos Ruiz", 30), LocalDate.now()));
        listaReservas.add(new Reserva("LOC-002", "Barcelona", "IT", "VY6210",
                new Persona("87654321B", "Lucía Gómez", 25), LocalDate.now()));
        listaReservas.add(new Reserva("LOC-003", "Valencia", "GB", "FR4522",
                new Persona("45678912C", "Marcos Soler", 42), LocalDate.now()));

        // Lectura del CSV si existe en la ruta local
        if (Files.exists(rutaCsv)) {
            try (BufferedReader br = Files.newBufferedReader(rutaCsv)) {
                String linea = br.readLine(); // Saltar cabecera
                int contador = 4;
                while ((linea = br.readLine()) != null) {
                    if (linea.trim().isEmpty()) continue;

                    String[] campos = linea.split(",");
                    if (campos.length >= 4) {
                        String destino = campos[0].trim();
                        String origen = campos[1].trim();
                        String vuelo = campos[2].trim();
                        String nombrePasajero = campos[3].trim();

                        Persona persona = new Persona("DOC-" + contador, nombrePasajero, 30);
                        Reserva reserva = new Reserva("LOC-" + String.format("%03d", contador),
                                origen, destino, vuelo, persona, LocalDate.now());

                        listaReservas.add(reserva);
                        contador++;
                    }
                }
                System.out.println("Se cargaron las reservas del CSV correctamente (" + listaReservas.size() + " en total).");
            } catch (IOException e) {
                System.err.println("Error al leer el archivo CSV: " + e.getMessage());
            }
        } else {
            System.out.println("Aviso: No se encontró el archivo CSV en la raíz. Trabajando solo con datos en memoria.");
        }

        // Guardado de respaldo en archivo de texto
        StringBuilder contenido = new StringBuilder();
        for (Reserva r : listaReservas) {
            contenido.append(r);
        }

        try {
            Files.writeString(rutaTxt, contenido);
            System.out.println("Datos guardados con éxito en: " + rutaTxt.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }
}