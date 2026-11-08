# Acceso a Datos: Entrega 2 — Migración AirTortilla a Base de Datos Relacional

---

## 1. Objetivo General
Convertir las historias de usuario (*User Stories*) y los requerimientos encomendados en un conjunto estructurado de tareas medibles, completando la iteración técnica y funcional correspondiente a esta entrega.

---

## 2. Historias de Usuario (User Stories)

| Rol | Deseo / Acción | Beneficio / Justificación |
| :--- | :--- | :--- |
| **Empleado** | Poder hacer log in | Acceder a los recursos internos de la compañía. |
| **Administrador de Sistemas** | Poder dar de alta nuevos usuarios | Garantizar el control de acceso y la seguridad interna de la aplicación. |
| **Administrador de Sistemas** | Mantener la trazabilidad de las modificaciones (auditoría / logs) | Mantener las buenas prácticas de seguridad y control de cambios en la aplicación. |

---

## 3. Requerimientos de Infraestructura y Base de Datos

### 3.1. Migración Obligatoria a Base de Datos Relacional
* **Requisito legal / obligatorio:** AirTortilla solicita por normativa legal migrar toda su infraestructura actual de almacenamiento de datos a un **Sistema Gestor de Bases de Datos Relacional (RDBMS)** (ej. PostgreSQL, MySQL, MariaDB, SQLite).
* Debe diseñarse el modelo relacional (esquema, claves primarias, claves foráneas e integridad referencial) para usuarios, accesos y registros de auditoría.

### 3.2. Contenerización (Opcional / Deseable)
* Confeccionar un sistema basado en contenedores virtuales (**Docker / Docker Compose**) para facilitar el empaquetado, la portabilidad y los despliegues de la aplicación y la base de datos.
* *Nota:* No es un requisito crítico de bloqueo para esta entrega, pero suma valor arquitectónico.

---

## 4. Stack Tecnológico e Instrucciones de Entrega

> **AVISO IMPORTANTE:**  
> Esta es la **última versión** en la cual se permite una migración en caliente del stack tecnológico principal sin penalización estructural.  
> Si en futuras versiones se desea cambiar de herramientas o lenguajes, deberá justificarse y documentarse obligatoriamente el proceso completo de migración en la memoria técnica.

### Opciones válidas de tecnologías admitidas:

* **Lenguajes Backend / Lógica:**
  * Java
  * Python
  * C#
  * Go
  * JavaScript (Node.js)
  * PHP

* **Interfaz de Usuario:**
  * Terminal (CLI)
  * Interfaz Web
  * Interfaz Nativa (Desktop)
  * Interfaz Móvil

* **Persistencia:**
  * Ficheros (origen / histórico) $\rightarrow$ Base de Datos Relacional (destino obligatorio).

---

## 5. Desglose de Tareas Sugeridas para el Tablero / Backlog

1. **Diseño de Datos:**
   - [ ] Diseñar el diagrama Entidad-Relación (tablas de usuarios, roles, sesiones y logs de auditoría).
   - [ ] Crear scripts de inicialización DDL (`schema.sql`) y datos iniciales / seeds (`data.sql`).
   - [ ] Migrar los datos existentes en ficheros hacia las tablas de la base de datos relacional.

2. **Módulo de Autenticación y Usuarios:**
   - [ ] Implementar endpoint/flujo de Login con validación segura de credenciales (hashing de contraseñas).
   - [ ] Implementar funcionalidad para que el Administrador dé de alta nuevos usuarios con roles definidos.

3. **Módulo de Trazabilidad y Auditoría:**
   - [ ] Crear mecanismo de captura de eventos (quién modificó qué recurso, fecha/hora y tipo de acción).
   - [ ] Persistir los registros de auditoría en la base de datos.

4. **Entorno y Despliegue (DevOps):**
   - [ ] (Opcional) Crear `Dockerfile` y `docker-compose.yml` para levantar la aplicación junto con el servicio de base de datos.