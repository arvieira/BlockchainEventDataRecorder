package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering;

public class ClusteringStateMachine {

    // Possíveis estados da máquina de estados dos veículos
    public final static String SOLE = "sole";
    public final static String LEADER = "leader";
    public final static String WAITING = "waiting";
    public final static String ASP = "aspirant";
    public final static String MEMBER = "member";

    // Status atual
    private String state;

    // Construtor
    public ClusteringStateMachine(String state) {
        this.state = state;
    }

    // Getter do estado
    public String getState() {
        return state;
    }

    // Setter do estado
    public void setState(String state) {
        this.state = state;
    }

    // Verifica se é Sole
    public boolean isSole() {
        return state.equals(SOLE);
    }

    // Verifica se é Leader
    public boolean isLeader() {
        return state.equals(LEADER);
    }

    // Verifica se é Member
    public boolean isMember() {
        return state.equals(MEMBER);
    }

    // Verifica se está em Waiting
    public boolean isWaiting() {
        return state.equals(WAITING);
    }

    // Verifica se está em ASP
    public boolean isAsp() {
        return state.equals(ASP);
    }
}
