# Imagen base ligera con Java 21/25
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copiar archivos fuente, scripts SQL y datos CSV
COPY src/ ./src/
COPY *.sql ./
COPY *.csv ./

# Descargar el driver de SQLite durante la construccion de la imagen
RUN mkdir -p lib && \
    wget -O lib/sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.47.1.0/sqlite-jdbc-3.47.1.0.jar

# Compilar todo el codigo Java
RUN mkdir -p bin && \
    javac -cp "lib/sqlite-jdbc.jar" -d bin src/*.java

# Puerto donde escucha el servidor web
EXPOSE 8080

# Comando para ejecutar AirTortilla
CMD ["java", "-cp", "bin:lib/sqlite-jdbc.jar", "Main"]

