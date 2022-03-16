package br.ufrj.nce.labnet.vehicleunit.vehicle.utils;

import br.ufrj.nce.labnet.vehicleunit.vehicle.IntelligentVehicleWitness;
import br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.SimTransaction;
import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.GroupManager;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class SimLogger {

    // Variáveis para construção e trabalho com o SimCounter
    private static final String GROUP_ID = "groupId";
    private static final String GROUP_SIZE = "groupSize";
    private static final String VEHICLE_ID = "vehicleId";
    private static final String LEADER_CONV = "LEADER CONV EM BROADCAST = ";
    private static final String GROUP_CREATED = "GRUPO CRIADO = ";
    private static final String GROUP_DESTROYED = "GRUPO DESTRUIDO = ";
    private static final String SIM_END = "SHUTDOWN APPLICATION = ";
    private static final String SIM_TIME = "tempo";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";
    private static final String TRANSACTIONS = "TRANSACTIONS = ";
    private static final String TRANSACTION_POOL = "TransactionPool";
    private static final String EMPTY_TRANSACTION_POOL = "Empty Transaction Pool";
    private static final String BLOCKCHAIN = "Blockchains";
    private static final String BLOCKS = "Blocks";

    // Correções do GPS para pode utilizar no point map do flourish
    private static final double LAT_CORRECTION = 0.00145;
    private static final double LNG_CORRECTION = -23.992928;


    // Objetos necessários para operar o veículo
    private IntelligentVehicleWitness vehicle;
    private VehicleOperatingSystem operatingSystem;
    private UnitLogger logger;


    // Construtor
    public SimLogger(IntelligentVehicleWitness vehicle) {
        this.vehicle = vehicle;
        operatingSystem = vehicle.getOperatingSystem();
        logger = vehicle.getLog();
    }


    // Registra o envio de um convite
    public void sendInvitation(GroupManager groupManager) {
        // Montando objeto JSON para escrever no log os dados do grupo
        JSONObject json = new JSONObject();
        json.put(GROUP_ID, operatingSystem.getId() + "_" + groupManager.getGroupId());
        json.put(GROUP_SIZE, groupManager.getGroupSize());
        json.put(SIM_TIME, Long.toString(operatingSystem.getSimulationTime()));

        // Envia o convite no estado LEADER
        logger.info(LEADER_CONV + json.toString());
    }

    // Registra a criação de um grupo
    public void groupCreated(GroupManager groupManager) {
        // Montando objeto JSON para escrever no log os dados do grupo
        JSONObject json = new JSONObject();
        json.put(GROUP_ID, operatingSystem.getId() + "_" + groupManager.getGroupId());
        json.put(GROUP_SIZE, groupManager.getGroupSize());
        json.put(SIM_TIME, Long.toString(operatingSystem.getSimulationTime()));
        json.put(LATITUDE, operatingSystem.getPosition().getLatitude() + LAT_CORRECTION);
        json.put(LONGITUDE, operatingSystem.getPosition().getLongitude() + LNG_CORRECTION);

        // Escrevendo no log
        logger.info(GROUP_CREATED + json.toString());
    }

    // Registra a destruição de um grupo
    public void groupDestroyed(GroupManager groupManager) {
        // Montando objeto JSON para escrever no log os dados do grupo
        JSONObject json = new JSONObject();
        json.put(GROUP_ID, vehicle.getOperatingSystem().getId() + "_" + groupManager.getGroupId());
        json.put(GROUP_SIZE, groupManager.getGroupSize());
        json.put(SIM_TIME, Long.toString(vehicle.getOs().getSimulationTime()));
        json.put(LATITUDE, vehicle.getOperatingSystem().getPosition().getLatitude() + LAT_CORRECTION);
        json.put(LONGITUDE, vehicle.getOperatingSystem().getPosition().getLongitude() + LNG_CORRECTION);

        // Escrevendo no log
        vehicle.getLog().info(GROUP_DESTROYED + json.toString());
    }

    // Registra o desligamento de um veículo
    public void tearDown() {
        // Montando objeto JSON para escrever no log os dados do grupo
        JSONObject json = new JSONObject();
        json.put(VEHICLE_ID, operatingSystem.getId());
        json.put(SIM_TIME, Long.toString(operatingSystem.getSimulationTime()));
        json.put(LATITUDE, operatingSystem.getPosition().getLatitude() + LAT_CORRECTION);
        json.put(LONGITUDE, operatingSystem.getPosition().getLongitude() + LNG_CORRECTION);

        logger.info(SIM_END + json.toString());
    }

    // Imprime a transaction pool no log
    public String printTransactionPool(List<SimTransaction> transactioPool) {
        // Montando objeto JSON para escreber no log a transaction pool
        JSONArray jsonTransactions = new JSONArray();
        for (SimTransaction transaction : transactioPool) {
            jsonTransactions.put(transaction.toJson());
        }

        JSONObject json = new JSONObject();
        json.put(TRANSACTION_POOL, jsonTransactions);

        //logger.info(TRANSACTIONS + json.toString());
        return json.toString();
    }
}
