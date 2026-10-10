import java.time.LocalDate;

public class Reserva {
    private String idReserva;
    private String origen;
    private String destino; // Código de país / destino
    private String vuelo;
    private Persona pasajero;
    private LocalDate fechaReserva;

    public Reserva(String idReserva, String origen, String destino, String vuelo, Persona pasajero, LocalDate fechaReserva) {
        this.idReserva = idReserva;
        this.origen = origen;
        this.destino = destino;
        this.vuelo = vuelo;
        this.pasajero = pasajero;
        this.fechaReserva = fechaReserva;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getVuelo() {
        return vuelo;
    }

    public Persona getPasajero() {
        return pasajero;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    @Override
    public String toString() {
        return "[" + idReserva + "] Vuelo: " + vuelo + " Origen: " + origen +
                " Destino: " + destino + " Pasajero: " + pasajero +
                " Fecha: " + fechaReserva + "\n";
    }
}