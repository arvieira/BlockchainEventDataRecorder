package br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.sensors;

import java.sql.Timestamp;

// Classe que representa um evento ocorrido.
// Um conjunto de eventos se tornará uma transação
public class Event {

    private EventTypes eventType;       // Tipo do evento é um dos EventTypes da enumeração
    private String param;               // Parâmetros diversos do evento que ocorreu
    private Timestamp timestamp;        // Momento em que o evento ocorreu


    // Construtor da classe
    public Event(EventTypes eventType, String param) {
        this.eventType = eventType;
        this.param = param;
        this.timestamp = new Timestamp(System.currentTimeMillis());
    }

    // Getter do tipo do evento
    public EventTypes getEventType() {
        return eventType;
    }

    // Setter do tipo do evento
    public void setEventType(EventTypes eventType) {
        this.eventType = eventType;
    }

    // Getter dos parâmetros
    public String getParam() {
        return param;
    }

    // Setter dos parâmetros
    public void setParam(String param) {
        this.param = param;
    }

    // Getter do timestamp
    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Setter do timestamp
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    // Método para impressão de um evento
    @Override
    public String toString() {
        return "Event{" +
                "eventType=" + eventType +
                ", param='" + param + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
