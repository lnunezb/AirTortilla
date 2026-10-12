# AirTortilla - Sistema de Gestión y Filtro de Reservas

## Descripción del Proyecto
Módulo de procesamiento de ficheros desarrollado en Java para la aerolínea **AirTortilla**. El sistema procesa un conjunto inicial de reservas en formato CSV, aplica un filtro por país de destino y genera múltiples ficheros particionados respetando el estándar `AirTortilla_XX_YYYY_MM_DD`.

---

##  Arquitectura del Sistema (Modelo - Vista - Controlador)

El proyecto sigue el patrón de diseño **MVC**:

* **Modelo (`Model`)**:
    * `Persona`: Representa los datos del pasajero (Documento, Nombre, Edad).
    * `Reserva`: Modela la entidad de la reserva con identificador unívoco / localizador, origen, país de destino (ISO-2), número de vuelo y fecha de realización.
* **Vista (`View`)**:
    * Menú interactivo por **Terminal / Consola** implementado con estructuras `switch-case`, permitiendo interactuar con el sistema tanto en modalidad total como en modalidad línea a línea.
* **Controlador (`Controller`)**:
    * Lógica de lectura de flujos de datos (`BufferedReader`), validación, resolución de colisiones con localizadores y exportación a los ficheros correspondientes por país de destino.

---

## Diagrama de Flujo del Proceso

![Diagrama de Flujo](diagrama.jpg)

## Estructura del Fichero CSV de Entrada
El sistema procesa archivos `.csv` con la siguiente estructura de columnas:

| Campo | Cabecera | Tipo | Descripción | Ejemplo |
|---|---|---|---|---|
| 1 | `Destino` | Texto (ISO-2 o País) | País de destino de la reserva | `FR` / `France` |
| 2 | `Origen` | Texto | País o ciudad de origen | `Spain` / `Madrid` |
| 3 | `No. Vuelo` | Texto / Numérico | Identificador del vuelo | `IB3401` |
| 4 | `Nombre` | Texto | Nombre y apellidos del pasajero | `Carlos Ruiz` |

> **Nota:** Los ficheros de datos (`*.csv`) se encuentran excluidos del repositorio mediante `.gitignore` conforme a las especificaciones de entrega.