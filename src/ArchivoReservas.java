import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class ArchivoReservas {

    private static final Path RUTA_CSV_ORIGEN = Paths.get("MOCK_DATA (1).csv");
    private static final Scanner scanner = new Scanner(System.in);

    // Mapeo de países a código ISO-2 exigido para el nombre del fichero
    private static final Map<String, String> CODIGOS_ISO = new HashMap<>();

    static {
        CODIGOS_ISO.put("THAILAND", "TH");
        CODIGOS_ISO.put("FRANCE", "FR");
        CODIGOS_ISO.put("INDONESIA", "ID");
        CODIGOS_ISO.put("CHINA", "CN");
        CODIGOS_ISO.put("UNITED STATES", "US");
        CODIGOS_ISO.put("BRAZIL", "BR");
        CODIGOS_ISO.put("IRELAND", "IE");
        CODIGOS_ISO.put("PORTUGAL", "PT");
        CODIGOS_ISO.put("SPAIN", "ES");
        CODIGOS_ISO.put("ITALY", "IT");
        CODIGOS_ISO.put("UNITED KINGDOM", "GB");
        CODIGOS_ISO.put("GERMANY", "DE");
        CODIGOS_ISO.put("RUSSIA", "RU");
        CODIGOS_ISO.put("JAPAN", "JP");
        CODIGOS_ISO.put("CANADA", "CA");
        CODIGOS_ISO.put("MEXICO", "MX");
    }

    public static void main(String[] args) {
        boolean salir = false;

        while (!salir) {
            System.out.println("   AIRTORTILLA - SISTEMA DE GESTIÓN DE RESERVAS");
            System.out.println("1. Generar ficheros por destino (Modalidad TOTAL)");
            System.out.println("2. Generar ficheros por destino (Modalidad LÍNEA A LÍNEA)");
            System.out.println("3. Consultar reservas (Modalidad TOTAL)");
            System.out.println("4. Consultar reservas (Modalidad LÍNEA A LÍNEA)");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    generarModalidadTotal();
                    break;
                case "2":
                    generarModalidadLineaALinea();
                    break;
                case "3":
                    consultarModalidadTotal();
                    break;
                case "4":
                    consultarModalidadLineaALinea();
                    break;
                case "5":
                    System.out.println("Cerrando la aplicación...");
                    salir = true;
                    break;
                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
                    break;
            }
        }
    }

    private static String obtenerIso(String pais) {
        if (pais == null || pais.trim().isEmpty()) return "XX";
        String limpio = pais.trim().toUpperCase();
        if (CODIGOS_ISO.containsKey(limpio)) return CODIGOS_ISO.get(limpio);
        return limpio.length() >= 2 ? limpio.substring(0, 2) : "XX";
    }

    private static String construirNombreFichero(String isoDestino, LocalDate fecha) {
        return String.format("AirTortilla_%s_%04d_%02d_%02d.csv",
                isoDestino, fecha.getYear(), fecha.getMonthValue(), fecha.getDayOfMonth());
    }

    // Lee todo el archivo en memoria y detecta colisiones de (Nombre + Origen + Destino) asignando localizadores unicos y escribe ficheros por pais.

    private static void generarModalidadTotal() {
        if (!Files.exists(RUTA_CSV_ORIGEN)) {
            System.err.println("No se encontró el fichero: " + RUTA_CSV_ORIGEN.toAbsolutePath());
            return;
        }

        List<Reserva> lista = new ArrayList<>();
        Map<String, Integer> colisiones = new HashMap<>();
        LocalDate fechaHoy = LocalDate.now();

        try (BufferedReader br = Files.newBufferedReader(RUTA_CSV_ORIGEN)) {
            String linea = br.readLine(); // Descartar cabecera
            int idGlobal = 1;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] campos = linea.split(",");
                if (campos.length >= 4) {
                    String destino = campos[0].trim();
                    String origen = campos[1].trim();
                    String vuelo = campos[2].trim();
                    String nombre = campos[3].trim();

                    String clave = (nombre + "|" + origen + "|" + destino).toUpperCase();
                    int coincidencias = colisiones.getOrDefault(clave, 0) + 1;
                    colisiones.put(clave, coincidencias);

                    String localizador = String.format("RES-%04d", idGlobal);
                    if (coincidencias > 1) {
                        localizador += "-LOC" + coincidencias;
                    }

                    Persona persona = new Persona("DOC-" + idGlobal, nombre, 30);
                    lista.add(new Reserva(localizador, origen, destino, vuelo, persona, fechaHoy));
                    idGlobal++;
                }
            }

            // Agrupar por código ISO de destino
            Map<String, List<Reserva>> gruposPorDestino = new HashMap<>();
            for (Reserva r : lista) {
                String iso = obtenerIso(r.getDestino());
                gruposPorDestino.computeIfAbsent(iso, k -> new ArrayList<>()).add(r);
            }

            for (Map.Entry<String, List<Reserva>> entrada : gruposPorDestino.entrySet()) {
                String nombreFichero = construirNombreFichero(entrada.getKey(), fechaHoy);
                Path salida = Paths.get(nombreFichero);

                try (BufferedWriter bw = Files.newBufferedWriter(salida)) {
                    bw.write("Localizador,Origen,Destino,Vuelo,Pasajero,Fecha\n");
                    for (Reserva r : entrada.getValue()) {
                        bw.write(String.format("%s,%s,%s,%s,%s,%s\n",
                                r.getIdReserva(), r.getOrigen(), r.getDestino(),
                                r.getVuelo(), r.getPasajero().getNombre(), r.getFechaReserva()));
                    }
                }
                System.out.println(" -> Fichero creado: " + nombreFichero + " (" + entrada.getValue().size() + " reservas)");
            }

            System.out.println("Generación total completada.");
        } catch (IOException e) {
            System.err.println("Error en generación total: " + e.getMessage());
        }
    }

    /**
     * MODALIDAD LÍNEA A LÍNEA: Lee registro a registro mediante flujos (streams)
     * y añade cada línea directamente al fichero correspondiente sin acumular todo en memoria.
     */
    private static void generarModalidadLineaALinea() {
        if (!Files.exists(RUTA_CSV_ORIGEN)) {
            System.err.println("No se encontró el fichero: " + RUTA_CSV_ORIGEN.toAbsolutePath());
            return;
        }

        LocalDate fechaHoy = LocalDate.now();
        Map<String, Integer> colisiones = new HashMap<>();
        Set<String> cabecerasEscritas = new HashSet<>();

        try (BufferedReader br = Files.newBufferedReader(RUTA_CSV_ORIGEN)) {
            String linea = br.readLine();
            int contador = 1;

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] campos = linea.split(",");
                if (campos.length >= 4) {
                    String destino = campos[0].trim();
                    String origen = campos[1].trim();
                    String vuelo = campos[2].trim();
                    String nombre = campos[3].trim();

                    String clave = (nombre + "|" + origen + "|" + destino).toUpperCase();
                    int coincidencias = colisiones.getOrDefault(clave, 0) + 1;
                    colisiones.put(clave, coincidencias);

                    String localizador = String.format("RES-%04d", contador);
                    if (coincidencias > 1) {
                        localizador += "-LOC" + coincidencias;
                    }

                    String iso = obtenerIso(destino);
                    String nombreFichero = construirNombreFichero(iso, fechaHoy);
                    Path rutaSalida = Paths.get(nombreFichero);

                    boolean escribirCabecera = !cabecerasEscritas.contains(nombreFichero) && !Files.exists(rutaSalida);
                    try (BufferedWriter bw = Files.newBufferedWriter(rutaSalida,
                            java.nio.file.StandardOpenOption.CREATE,
                            java.nio.file.StandardOpenOption.APPEND)) {

                        if (escribirCabecera) {
                            bw.write("Localizador,Origen,Destino,Vuelo,Pasajero,Fecha\n");
                            cabecerasEscritas.add(nombreFichero);
                        }
                        bw.write(String.format("%s,%s,%s,%s,%s,%s\n",
                                localizador, origen, destino, vuelo, nombre, fechaHoy));
                    }
                    contador++;
                }
            }
            System.out.println("Generación línea a línea completada exitosamente.");
        } catch (IOException e) {
            System.err.println("Error en generación línea a línea: " + e.getMessage());
        }
    }

    // Consulta Total: Carga y muestra los ficheros generados actualmente.
    private static void consultarModalidadTotal() {
        System.out.println("Ficheros AirTortilla generados disponibles");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."), "AirTortilla_*.csv")) {
            boolean encontrado = false;
            for (Path entry : stream) {
                encontrado = true;
                List<String> todasLasLineas = Files.readAllLines(entry);
                System.out.println("Fichero: " + entry.getFileName() + " (" + (todasLasLineas.size() - 1) + " registros)");
                for (String l : todasLasLineas) {
                    System.out.println("  " + l);
                }
            }
            if (!encontrado) {
                System.out.println("No hay ficheros de salida generados aún. Ejecute primero la opción 1 o 2.");
            }
        } catch (IOException e) {
            System.err.println("Error al consultar ficheros: " + e.getMessage());
        }
    }

     //Lee el fichero CSV de entrada en tiempo real con BufferedReader.
    private static void consultarModalidadLineaALinea() {
        if (!Files.exists(RUTA_CSV_ORIGEN)) {
            System.err.println("No se encontró el fichero: " + RUTA_CSV_ORIGEN.toAbsolutePath());
            return;
        }

        System.out.println("Consulta en Streaming (Línea a Línea) ");
        try (BufferedReader br = Files.newBufferedReader(RUTA_CSV_ORIGEN)) {
            String linea;
            int num = 0;
            while ((linea = br.readLine()) != null) {
                if (num == 0) {
                    System.out.println("[CABECERA] -> " + linea);
                } else {
                    System.out.printf("[%03d] -> %s%n", num, linea);
                }
                num++;
            }
        } catch (IOException e) {
            System.err.println("Error en lectura línea a línea: " + e.getMessage());
        }
    }
}