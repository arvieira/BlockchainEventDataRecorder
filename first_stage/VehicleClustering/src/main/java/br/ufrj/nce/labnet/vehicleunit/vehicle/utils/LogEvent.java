package br.ufrj.nce.labnet.vehicleunit.vehicle.utils;

import br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.SimTransaction;
import org.json.JSONObject;

public class LogEvent {

    // Comandos
    public static final String CREATE_CHANNEL = "CREATE CHANNEL";
    public static final String JOIN_PEER = "JOIN PEER";
    public static final String PAUSE_PEER = "PAUSE PEER";
    public static final String TRANSACTION = "TRANSACTION";
    public static final String JOIN_RESUME_CHANNEL = "JOIN/RESUME CHANNEL";

    // Tags para o json
    public static final String COMMAND = "command";
    public static final String LEADER = "leader";
    public static final String MEMBER = "member";
    public static final String GROUP = "group";
    public static final String TRANSACTION_TAG = "transaction";

    private JSONObject json;


    public LogEvent(String command, String leader, String member, String group) {
        json = new JSONObject();
        json.put(COMMAND, command);
        json.put(LEADER, leader.replaceAll(LEADER, "").trim());
        json.put(MEMBER, member.replaceAll(MEMBER, "").trim());
        json.put(GROUP, group.replaceAll(GROUP, "").trim());
    }

    public LogEvent(String command, String vehicle, String group) {
        json = new JSONObject();
        json.put(COMMAND, command);

        if (vehicle.contains(LEADER)) {
            json.put(LEADER, vehicle.replaceAll(LEADER, "").trim());
        } else {
            json.put(MEMBER, vehicle.replaceAll(MEMBER, "").trim());
        }

        json.put(GROUP, group.replaceAll(GROUP, "").trim());
    }

    public LogEvent(String command, String vehicle, String group, SimTransaction transaction) {
        json = new JSONObject();
        json.put(COMMAND, command);

        if (vehicle.contains(LEADER)) {
            json.put(LEADER, vehicle.replaceAll(LEADER, "").trim());
        } else {
            json.put(MEMBER, vehicle.replaceAll(MEMBER, "").trim());
        }

        json.put(GROUP, group.replaceAll(GROUP, "").trim());
        json.put(TRANSACTION_TAG, transaction.toJson());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
