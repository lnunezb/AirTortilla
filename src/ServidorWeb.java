import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Servidor web nativo de Java (puerto 8080) con interfaz grafica moderna
public class ServidorWeb {

    // Usuario que tiene la sesion iniciada actualmente en la web
    private static UsuarioDAO.UsuarioInfo usuarioActivo = null;

    public static void iniciar(int puerto) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);

            // Rutas de la aplicacion
            server.createContext("/", new LoginHandler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/dashboard", new DashboardHandler());
            server.createContext("/generar", new GenerarHandler());
            server.createContext("/admin", new AdminHandler());
            server.createContext("/auditoria", new AuditoriaHandler());
            server.createContext("/logout", new LogoutHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("=================================================");
            System.out.println("  AIRTORTILLA WEB INICIADO CON EXITO");
            System.out.println("  Abre tu navegador en: http://localhost:" + puerto);
            System.out.println("=================================================");

        } catch (IOException e) {
            System.err.println("Error al arrancar el servidor web: " + e.getMessage());
        }
    }

    // Cabecera HTML comun con estilos Bootstrap 5
    private static String cabecera(String titulo) {
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>"
             + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
             + "<title>" + titulo + " - AirTortilla</title>"
             + "<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css' rel='stylesheet'>"
             + "<style>body{background-color:#f4f7f6;} .navbar{box-shadow: 0 2px 4px rgba(0,0,0,.08);}</style>"
             + "</head><body>";
    }

    // Barra de navegacion superior
    private static String navbar() {
        if (usuarioActivo == null) return "";
        String badgeRol = usuarioActivo.esAdmin() ? "<span class='badge bg-danger'>ADMIN</span>" : "<span class='badge bg-primary'>EMPLEADO</span>";
        String enlaceAdmin = usuarioActivo.esAdmin() 
            ? "<li class='nav-item'><a class='nav-link' href='/admin'>Usuarios</a></li>"
            + "<li class='nav-item'><a class='nav-link' href='/auditoria'>Auditoría</a></li>" 
            : "";

        return "<nav class='navbar navbar-expand-lg navbar-dark bg-dark mb-4'>"
             + "<div class='container'>"
             + "<a class='navbar-brand fw-bold' href='/dashboard'>✈️ AirTortilla</a>"
             + "<div class='collapse navbar-collapse'>"
             + "<ul class='navbar-nav me-auto'>"
             + "<li class='nav-item'><a class='nav-link' href='/dashboard'>Inicio</a></li>"
             + enlaceAdmin
             + "</ul>"
             + "<div class='d-flex align-items-center text-white'>"
             + "<span class='me-3'>👤 <strong>" + usuarioActivo.nombreUsuario + "</strong> " + badgeRol + "</span>"
             + "<a href='/logout' class='btn btn-outline-light btn-sm'>Cerrar sesión</a>"
             + "</div>"
             + "</div></div></nav>";
    }

    private static String pie() {
        return "<script src='https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js'></script></body></html>";
    }

    // Envia una respuesta HTML al navegador
    private static void responder(HttpExchange t, int codigo, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        t.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        t.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Redirige al navegador a otra ruta
    private static void redirigir(HttpExchange t, String ruta) throws IOException {
        t.getResponseHeaders().set("Location", ruta);
        t.sendResponseHeaders(302, -1);
    }

    // Extrae los datos enviados por formulario POST
    private static Map<String, String> parsearFormulario(HttpExchange t) throws IOException {
        String body = new String(t.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                params.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                           URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    // -------------------------------------------------------------
    // 1. MANEJADOR: LOGIN
    // -------------------------------------------------------------
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                Map<String, String> form = parsearFormulario(t);
                String user = form.getOrDefault("username", "").trim();
                String pass = form.getOrDefault("password", "").trim();

                UsuarioDAO.UsuarioInfo u = UsuarioDAO.autenticar(user, pass);
                if (u != null) {
                    usuarioActivo = u;
                    AuditoriaDAO.registrar(u.id, "LOGIN", "Inicio de sesion exitoso");
                    redirigir(t, "/dashboard");
                    return;
                }

                // Error de credenciales
                String html = cabecera("Login")
                        + "<div class='container mt-5'><div class='row justify-content-center'><div class='col-md-4'>"
                        + "<div class='alert alert-danger'>Usuario o contraseña incorrectos.</div>"
                        + formularioLogin()
                        + "</div></div></div>" + pie();
                responder(t, 200, html);
                return;
            }

            // GET Login
            String html = cabecera("Login")
                    + "<div class='container mt-5'><div class='row justify-content-center'><div class='col-md-4'>"
                    + formularioLogin()
                    + "</div></div></div>" + pie();
            responder(t, 200, html);
        }

        private String formularioLogin() {
            return "<div class='card shadow-sm'>"
                 + "<div class='card-header bg-dark text-white text-center py-3'>"
                 + "<h4 class='mb-0'>✈️ AirTortilla</h4><small>Acceso al sistema</small></div>"
                 + "<div class='card-body p-4'>"
                 + "<form method='POST' action='/login'>"
                 + "<div class='mb-3'><label class='form-label'>Usuario</label>"
                 + "<input type='text' name='username' class='form-control' required autofocus placeholder='ej: admin'></div>"
                 + "<div class='mb-3'><label class='form-label'>Contraseña</label>"
                 + "<input type='password' name='password' class='form-control' required placeholder='ej: admin123'></div>"
                 + "<button type='submit' class='btn btn-primary w-100 py-2'>Iniciar sesión</button>"
                 + "</form></div></div>";
        }
    }

    // -------------------------------------------------------------
    // 2. MANEJADOR: DASHBOARD (PANEL PRINCIPAL)
    // -------------------------------------------------------------
    static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (usuarioActivo == null) {
                redirigir(t, "/login");
                return;
            }

            int totalReservas = contarReservas();

            String html = cabecera("Dashboard") + navbar()
                    + "<div class='container'>"
                    + "<div class='row mb-4'>"
                    + "  <div class='col-md-6'>"
                    + "    <div class='card border-primary shadow-sm h-100'>"
                    + "      <div class='card-body'>"
                    + "        <h5 class='card-title text-primary'>📊 Reservas en Base de Datos</h5>"
                    + "        <h2 class='display-5 fw-bold'>" + totalReservas + "</h2>"
                    + "        <p class='text-muted'>Registros almacenados en la tabla SQLite</p>"
                    + "      </div>"
                    + "    </div>"
                    + "  </div>"
                    + "  <div class='col-md-6'>"
                    + "    <div class='card border-success shadow-sm h-100'>"
                    + "      <div class='card-body'>"
                    + "        <h5 class='card-title text-success'>⚙️ Operaciones de Ficheros</h5>"
                    + "        <div class='d-grid gap-2 mt-3'>"
                    + "          <a href='/generar?modo=total' class='btn btn-outline-primary'>Generar Ficheros (Modalidad Total)</a>"
                    + "          <a href='/generar?modo=linea' class='btn btn-outline-success'>Generar Ficheros (Modalidad Línea a Línea)</a>"
                    + "        </div>"
                    + "      </div>"
                    + "    </div>"
                    + "  </div>"
                    + "</div>"
                    + tablaReservas()
                    + "</div>" + pie();

            responder(t, 200, html);
        }

        private int contarReservas() {
            String sql = "SELECT COUNT(*) FROM reservas";
            try (Connection con = ConexionBD.obtenerConexion();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (Exception e) {}
            return 0;
        }

        private String tablaReservas() {
            StringBuilder sb = new StringBuilder();
            sb.append("<div class='card shadow-sm'><div class='card-header bg-light fw-bold'>📋 Últimas Reservas Registradas</div>");
            sb.append("<div class='table-responsive'><table class='table table-hover mb-0'><thead class='table-dark'>");
            sb.append("<tr><th>Localizador</th><th>Origen</th><th>Destino</th><th>Vuelo</th><th>Pasajero</th><th>Fecha</th></tr></thead><tbody>");

            String sql = "SELECT localizador, origen, destino, vuelo, pasajero, fecha FROM reservas ORDER BY id DESC LIMIT 15";
            try (Connection con = ConexionBD.obtenerConexion();
                 Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    sb.append("<tr>")
                      .append("<td><span class='badge bg-secondary'>").append(rs.getString("localizador")).append("</span></td>")
                      .append("<td>").append(rs.getString("origen")).append("</td>")
                      .append("<td>").append(rs.getString("destino")).append("</td>")
                      .append("<td>").append(rs.getString("vuelo")).append("</td>")
                      .append("<td>").append(rs.getString("pasajero")).append("</td>")
                      .append("<td>").append(rs.getString("fecha")).append("</td>")
                      .append("</tr>");
                }
            } catch (Exception e) {}

            sb.append("</tbody></table></div></div>");
            return sb.toString();
        }
    }

    // -------------------------------------------------------------
    // 3. MANEJADOR: GENERAR FICHEROS
    // -------------------------------------------------------------
    static class GenerarHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (usuarioActivo == null) {
                redirigir(t, "/login");
                return;
            }

            String query = t.getRequestURI().getQuery();
            if (query != null && query.contains("modo=linea")) {
                ArchivoReservas.generarModalidadLineaALinea();
                AuditoriaDAO.registrar(usuarioActivo.id, "GENERAR_FICHEROS", "Modalidad Línea a Línea");
            } else {
                ArchivoReservas.generarModalidadTotal();
                AuditoriaDAO.registrar(usuarioActivo.id, "GENERAR_FICHEROS", "Modalidad Total");
            }

            redirigir(t, "/dashboard");
        }
    }

    // -------------------------------------------------------------
    // 4. MANEJADOR: ADMIN (GESTIÓN DE USUARIOS)
    // -------------------------------------------------------------
    static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (usuarioActivo == null || !usuarioActivo.esAdmin()) {
                redirigir(t, "/dashboard");
                return;
            }

            if ("POST".equalsIgnoreCase(t.getRequestMethod())) {
                Map<String, String> form = parsearFormulario(t);
                String user = form.getOrDefault("username", "").trim();
                String pass = form.getOrDefault("password", "").trim();
                int rol = Integer.parseInt(form.getOrDefault("rol", "2"));

                if (!user.isEmpty() && !pass.isEmpty()) {
                    boolean ok = UsuarioDAO.crearUsuario(user, pass, rol);
                    if (ok) {
                        AuditoriaDAO.registrar(usuarioActivo.id, "CREAR_USUARIO", "Nuevo usuario: " + user);
                    }
                }
                redirigir(t, "/admin");
                return;
            }

            List<UsuarioDAO.UsuarioInfo> usuarios = UsuarioDAO.listarTodos();

            StringBuilder sb = new StringBuilder(cabecera("Usuarios") + navbar());
            sb.append("<div class='container'><div class='row'>");
            sb.append("<div class='col-md-4'>");
            sb.append("<div class='card shadow-sm mb-4'>");
            sb.append("<div class='card-header bg-dark text-white fw-bold'>➕ Dar de alta usuario</div><div class='card-body'>");
            sb.append("<form method='POST' action='/admin'>");
            sb.append("<div class='mb-3'><label class='form-label'>Nombre de usuario</label><input type='text' name='username' class='form-control' required></div>");
            sb.append("<div class='mb-3'><label class='form-label'>Contraseña</label><input type='password' name='password' class='form-control' required></div>");
            sb.append("<div class='mb-3'><label class='form-label'>Rol</label><select name='rol' class='form-select'><option value='2'>EMPLEADO</option><option value='1'>ADMINISTRADOR</option></select></div>");
            sb.append("<button type='submit' class='btn btn-success w-100'>Crear Usuario</button></form></div></div></div>");

            sb.append("<div class='col-md-8'>");
            sb.append("<div class='card shadow-sm'><div class='card-header bg-light fw-bold'>👥 Usuarios del Sistema</div>");
            sb.append("<table class='table table-striped mb-0'><thead class='table-dark'><tr><th>ID</th><th>Usuario</th><th>Rol</th><th>Estado</th></tr></thead><tbody>");
            for (UsuarioDAO.UsuarioInfo u : usuarios) {
                sb.append("<tr><td>").append(u.id).append("</td>")
                  .append("<td><strong>").append(u.nombreUsuario).append("</strong></td>")
                  .append("<td>").append(u.rolNombre).append("</td>")
                  .append("<td>").append(u.activo ? "<span class='badge bg-success'>Activo</span>" : "<span class='badge bg-secondary'>Inactivo</span>").append("</td></tr>");
            }
            sb.append("</tbody></table></div></div></div></div>").append(pie());

            responder(t, 200, sb.toString());
        }
    }

    // -------------------------------------------------------------
    // 5. MANEJADOR: AUDITORÍA (TRAZABILIDAD / LOGS)
    // -------------------------------------------------------------
    static class AuditoriaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (usuarioActivo == null || !usuarioActivo.esAdmin()) {
                redirigir(t, "/dashboard");
                return;
            }

            List<AuditoriaDAO.LogInfo> logs = AuditoriaDAO.listarTodos();

            StringBuilder sb = new StringBuilder(cabecera("Auditoría") + navbar());
            sb.append("<div class='container'><div class='card shadow-sm'>");
            sb.append("<div class='card-header bg-dark text-white fw-bold'>📜 Registro de Auditoría (Trazabilidad)</div>");
            sb.append("<div class='table-responsive'><table class='table table-hover mb-0'><thead class='table-dark'>");
            sb.append("<tr><th>ID</th><th>Usuario</th><th>Acción</th><th>Recurso / Detalle</th><th>Fecha y Hora</th></tr></thead><tbody>");

            for (AuditoriaDAO.LogInfo log : logs) {
                sb.append("<tr>")
                  .append("<td>").append(log.id).append("</td>")
                  .append("<td><strong>").append(log.nombreUsuario).append("</strong></td>")
                  .append("<td><span class='badge bg-info text-dark'>").append(log.accion).append("</span></td>")
                  .append("<td>").append(log.recurso != null ? log.recurso : "-").append("</td>")
                  .append("<td><small class='text-muted'>").append(log.fechaHora).append("</small></td>")
                  .append("</tr>");
            }

            sb.append("</tbody></table></div></div></div>").append(pie());
            responder(t, 200, sb.toString());
        }
    }

    // -------------------------------------------------------------
    // 6. MANEJADOR: LOGOUT
    // -------------------------------------------------------------
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (usuarioActivo != null) {
                AuditoriaDAO.registrar(usuarioActivo.id, "LOGOUT", "Cierre de sesion");
                UsuarioDAO.registrarCierreSesion(usuarioActivo.id);
                usuarioActivo = null;
            }
            redirigir(t, "/login");
        }
    }
}

