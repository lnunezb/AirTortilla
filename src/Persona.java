public class Persona {
    private String documento;
    private String nombre;
    private int edad;

    public Persona(String documento, String nombre, int edad) {
        this.documento = documento;
        this.nombre = nombre;
        this.edad = edad;
    }

    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    @Override
    public String toString() {
        return nombre + " (Doc: " + documento + ", Edad: " + edad + ")";
    }
}