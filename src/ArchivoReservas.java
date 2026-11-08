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

    // Tabla de países y sus códigos ISO-2 para nombrar los ficheros de salida
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

    // MENÚ PRINCIPAL

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
            }
        }
    }

    // Devuelve el código ISO-2 del país, o las dos primeras letras si no está en la
    // tabla
    private static String obtenerIso(String pais) {
        if (pais == null || pais.trim().isEmpty())
            return "XX";
        String limpio = pais.trim().toUpperCase();
        return CODIGOS_ISO.getOrDefault(limpio,
                limpio.length() >= 2 ? limpio.substring(0, 2) : "XX");
    }

    // Construye el nombre del fichero de salida:
    // AirTortilla_<ISO>_<AÑO>_<MES>_<DIA>.csv
    private static String construirNombreFichero(String isoDestino, LocalDate fecha) {
        return "AirTortilla_" + isoDestino
                + "_" + fecha.getYear()
                + "_" + fecha.getMonthValue()
                + "_" + fecha.getDayOfMonth()
                + ".csv";
    }

    // Construye una línea CSV a partir de los datos de una reserva
    private static String lineaCSV(String... campos) {
        return String.join(",", campos) + "\n";
    }

    // OPCIÓN 1: GENERACIÓN TOTAL (todo en memoria)

    // Lee el CSV completo, agrupa las reservas por país de destino y escribe un
    // fichero por país.
    // Detecta duplicados (mismo nombre + origen + destino) y añade un sufijo -LOC
    // al localizador.
    private static void generarModalidadTotal() {
        if (!Files.exists(RUTA_CSV_ORIGEN)) {
            System.err.println("No se encontró el fichero: " + RUTA_CSV_ORIGEN.toAbsolutePath());
            return;
        }

        List<Reserva> lista = new ArrayList<>();
        Map<String, Integer> colisiones = new HashMap<>();
        LocalDate fechaHoy = LocalDate.now();

        try (BufferedReader br = Files.newBufferedReader(RUTA_CSV_ORIGEN)) {

            br.readLine(); // Saltar la cabecera
            int idGlobal = 1;

            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty())
                    continue;
                String[] campos = linea.split(",");
                if (campos.length < 4)
                    continue;

                String destino = campos[0].trim();
                String origen = campos[1].trim();
                String vuelo = campos[2].trim();
                String nombre = campos[3].trim();

                // Detectar duplicados por clave nombre|origen|destino
                String clave = (nombre + "|" + origen + "|" + destino).toUpperCase();
                int coincidencias = colisiones.merge(clave, 1, Integer::sum);

                String localizador = "RES-" + idGlobal;
                if (coincidencias > 1)
                    localizador += "-LOC" + coincidencias;

                Persona persona = new Persona("DOC-" + idGlobal, nombre, 30);
                lista.add(new Reserva(localizador, origen, destino, vuelo, persona, fechaHoy));
                idGlobal++;
            }

            // Agrupar reservas por código ISO del país de destino
            Map<String, List<Reserva>> gruposPorDestino = new HashMap<>();
            for (Reserva r : lista) {
                String iso = obtenerIso(r.getDestino());
                gruposPorDestino.computeIfAbsent(iso, k -> new ArrayList<>()).add(r);
            }

            // Escribir un fichero CSV por cada país de destino
            for (Map.Entry<String, List<Reserva>> entrada : gruposPorDestino.entrySet()) {
                String nombreFichero = construirNombreFichero(entrada.getKey(), fechaHoy);
                Path salida = Paths.get(nombreFichero);

                try (BufferedWriter bw = Files.newBufferedWriter(salida)) {
                    bw.write("Localizador,Origen,Destino,Vuelo,Pasajero,Fecha\n");
                    for (Reserva r : entrada.getValue()) {
                        bw.write(lineaCSV(
                                r.getIdReserva(), r.getOrigen(), r.getDestino(),
                                r.getVuelo(), r.getPasajero().getNombre(), r.getFechaReserva().toString()));
                    }
                }
                System.out.println(" -> Fichero creado: " + nombreFichero
                        + " (" + entrada.getValue().size() + " reservas)");
            }

            System.out.println("Generación total completada.");

        } catch (IOException e) {
            System.err.println("Error en generación total: " + e.getMessage());
        }
    }

    // OPCIÓN 2: GENERACIÓN LÍNEA A LÍNEA (streaming)

    // Lee el CSV registro a registro y vuelca cada línea directamente al fichero
    // correspondiente sin acumular todo en memoria.
    private static void generarModalidadLineaALinea() {
        if (!Files.exists(RUTA_CSV_ORIGEN)) {
            System.err.println("No se encontró el fichero: " + RUTA_CSV_ORIGEN.toAbsolutePath());
            return;
        }

        LocalDate fechaHoy = LocalDate.now();
        Map<String, Integer> colisiones = new HashMap<>();
        Set<String> cabecerasEscritas = new HashSet<>();

        try (BufferedReader br = Files.newBufferedReader(RUTA_CSV_ORIGEN)) {

            br.readLine(); // Saltar la cabecera
            int contador = 1;

            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty())
                    continue;
                String[] campos = linea.split(",");
                if (campos.length < 4)
                    continue;

                String destino = campos[0].trim();
                String origen = campos[1].trim();
                String vuelo = campos[2].trim();
                String nombre = campos[3].trim();

                // Detectar duplicados
                String clave = (nombre + "|" + origen + "|" + destino).toUpperCase();
                int coincidencias = colisiones.merge(clave, 1, Integer::sum);

                String localizador = "RES-" + contador;
                if (coincidencias > 1)
                    localizador += "-LOC" + coincidencias;

                // Determinar el fichero de destino y abrirlo en modo añadir
                String iso = obtenerIso(destino);
                String nombreFichero = construirNombreFichero(iso, fechaHoy);
                Path rutaSalida = Paths.get(nombreFichero);

                boolean escribirCabecera = !cabecerasEscritas.contains(nombreFichero)
                        && !Files.exists(rutaSalida);

                try (BufferedWriter bw = Files.newBufferedWriter(rutaSalida,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.APPEND)) {

                    if (escribirCabecera) {
                        bw.write("Localizador,Origen,Destino,Vuelo,Pasajero,Fecha\n");
                        cabecerasEscritas.add(nombreFichero);
                    }
                    bw.write(lineaCSV(localizador, origen, destino, vuelo, nombre, fechaHoy.toString()));
                }
                contador++;
            }

            System.out.println("Generación línea a línea completada exitosamente.");

        } catch (IOException e) {
            System.err.println("Error en generación línea a línea: " + e.getMessage());
        }
    }

    // OPCIÓN 3: CONSULTA TOTAL (leer ficheros generados)

    // Muestra el contenido de todos los ficheros AirTortilla_*.csv ya generados.
    private static void consultarModalidadTotal() {
        System.out.println("Ficheros AirTortilla generados disponibles");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."), "AirTortilla_*.csv")) {
            boolean encontrado = false;

            for (Path entry : stream) {
                encontrado = true;
                List<String> lineas = Files.readAllLines(entry);
                System.out.println("Fichero: " + entry.getFileName() + " (" + (lineas.size() - 1) + " registros)");
                for (String l : lineas) {
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

    // OPCIÓN 4: CONSULTA LÍNEA A LÍNEA (streaming del origen)

    // Lee el CSV de entrada en tiempo real y muestra cada línea por consola.
    private static void consultarModalidadLineaALinea() {
        if (!Files.exists(RUTA_CSV_ORIGEN)) {
            System.err.println("No se encontró el fichero: " + RUTA_CSV_ORIGEN.toAbsolutePath());
            return;
        }

        System.out.println("Consulta en Streaming (Línea a Línea)");

        try (BufferedReader br = Files.newBufferedReader(RUTA_CSV_ORIGEN)) {
            String linea;
            int num = 0;

            while ((linea = br.readLine()) != null) {
                if (num == 0) {
                    System.out.println("[CABECERA] -> " + linea);
                } else {
                    System.out.println("[" + num + "] -> " + linea);
                }
                num++;
            }

        } catch (IOException e) {
            System.err.println("Error en lectura línea a línea: " + e.getMessage());
        }
    }
}
