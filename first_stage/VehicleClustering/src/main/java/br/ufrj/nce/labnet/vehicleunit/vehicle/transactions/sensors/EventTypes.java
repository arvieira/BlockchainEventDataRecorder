package br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.sensors;

public enum EventTypes {
    ACCELERATE(0),
    BRAKE(1),
    CHANGE_LANE(2),
    TURN_LEFT(3),
    TURN_RIGHT(4),
    SPEED_CHANGE(5),
    START(6),
    STOP(7);

    private final int value;

    EventTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
