package br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.sensors;

import br.ufrj.nce.labnet.vehicleunit.vehicle.IntelligentVehicleWitness;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.geo.GeoPoint;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleSignals;
import org.json.JSONObject;

public class SimEvent {

    // Constantes para formatar o json do evento
    private static final String ID = "id";
    private static final String TIMESTAMP = "timestamp";
    private static final String POSITION_LAT = "latitude";
    private static final String POSITION_LNG = "longitude";
    private static final String LANE = "lane";
    private static final String SPEED = "speed";
    private static final String BRAKE = "brake";
    private static final String THROTTLE = "throttle";
    private static final String STOPPED = "stopped";
    //private static final String FRONTLIGHT = "frontLight";
    //private static final String HIGHBEAM = "highbeam";
    private static final String LEFTLIGHT = "leftLight";
    private static final String RIGHTLIGHT = "rightLight";
    //private static final String EMERGENCY = "emergency";
    private static final String BRAKELIGHT = "brakeLight";
    private static final String BACKDRIVE = "backdrive";

    // Veículo que gerou o evento
    IntelligentVehicleWitness vehicle;
    VehicleOperatingSystem operatingSystem;
    VehicleData vehicleInfo;
    VehicleSignals vehicleSignals;

    // Identificação do veículo e o tempo na simulação
    private String id;                          // Identidade do veículo
    private long timestamp;                       // Tempo da simulação que ocorreu o evento

    // Dados referentes ao posicionamento do veículo
    private GeoPoint position;                  // Posição GPS do evento
    private int laneIndex;                      // Faixa que ocorreu o evento

    // Sensores com relação à velocidade
    private double speed;                       // Valor da velocidade
    private double brake;                       // Valor da desaceleração imposta pelo freio
    private double throttle;                    // Valor da aceleração do acelerador
    private boolean isStopped;                  // Se está parado

    // Sensores da sinalização luminosa do veículo
    //private boolean frontLight;                 // Farol
    //private boolean highbeam;                   // Farol alto
    private boolean brakeLight;                 // Luz de freio
    private boolean leftLight;                  // Seta para esquerda
    private boolean rightLight;                 // Seta para a direita
    //private boolean emergency;                  // Pisca alerta
    private boolean backdrive;                  // Luz de ré


    // Construtor
    public SimEvent(IntelligentVehicleWitness vehicle) {
        // Veículo responsável pelo evento
        this.vehicle = vehicle;

        // Sistema operacional do veículo responsável pelo evento
        operatingSystem = vehicle.getOperatingSystem();

        // Informações do veículo responsável
        vehicleInfo = operatingSystem.getVehicleData();

        // Sinalização luminosa do veículo responsável
        if (vehicleInfo != null) {
            vehicleSignals = vehicleInfo.getVehicleSignals();
        }
    }


    // Pega o status atual do veículo
    public void getState() {
        // Identificação do veículo e o tempo na simulação
        id = operatingSystem.getId();
        timestamp = operatingSystem.getSimulationTime();

        // Dados referentes ao posicionamento do veículo
        position = operatingSystem.getPosition();
        laneIndex = operatingSystem.getRoadPosition().getLaneIndex();

        // Sensores com relação à velocidade
        if (vehicleInfo != null) {
            speed = vehicleInfo.getSpeed();
            brake = vehicleInfo.getBrake();
            throttle = vehicleInfo.getThrottle();
            isStopped = vehicleInfo.isStopped();
        }

        // Sensores da sinalização luminosa do veículo
        if (vehicleSignals != null){
            //frontLight = vehicleSignals.isFrontlight();
            //highbeam = vehicleSignals.isHighbeam();
            brakeLight = vehicleSignals.isBrakeLight();
            leftLight = vehicleSignals.isBlinkerLeft();
            rightLight = vehicleSignals.isBlinkerRight();
            //emergency = vehicleSignals.isBlinkerEmergency();
            backdrive = vehicleSignals.isReverseDrive();
        }
    }

    // Método para imprimir o objeto
    @Override
    public String toString() {
        String content = "{\n\t\t\t\t\t\tID = " + id;
        content += ", \n\t\t\t\t\t\tTIME = " + timestamp;
        content += ", \n\t\t\t\t\t\tPOSITION: (LAT = " + position.getLatitude() + " ; LNG = " + position.getLongitude() + ")";
        content += ", \n\t\t\t\t\t\tLANE = " + laneIndex;
        content += ", \n\t\t\t\t\t\tSPEED = " + speed;
        content += ", \n\t\t\t\t\t\tBRAKE = " + brake;
        content += ", \n\t\t\t\t\t\tTHROTTLE = " + throttle;
        content += ", \n\t\t\t\t\t\tisSTOPPED:" + isStopped;
        //content += ", \n\t\t\t\t\t\tFRONTLIGHT:" + frontLight;
        //content += ", \n\t\t\t\t\t\tHIGHBEAM:" + highbeam;
        content += ", \n\t\t\t\t\t\tBRAKELIGHT:" + brakeLight;
        content += ", \n\t\t\t\t\t\tLEFTLIGHT:" + leftLight;
        content += ", \n\t\t\t\t\t\tRIGHTLIGHT:" + rightLight;
        //content += ", \n\t\t\t\t\t\tEMERGENCY:" + emergency;
        content += ", \n\t\t\t\t\t\tBACKDRIVE:" + backdrive + "}";

        return content;
    }

    // Método para tornar o objeto em um json
    public JSONObject toJson() {
        JSONObject jsonSimEvent = new JSONObject();

        jsonSimEvent.put(ID, id);
        jsonSimEvent.put(TIMESTAMP, timestamp);
        jsonSimEvent.put(POSITION_LAT, position.getLatitude());
        jsonSimEvent.put(POSITION_LNG, position.getLongitude());
        jsonSimEvent.put(LANE, laneIndex);
        jsonSimEvent.put(SPEED, speed);
        jsonSimEvent.put(BRAKE, brake);
        jsonSimEvent.put(THROTTLE, throttle);
        jsonSimEvent.put(STOPPED, isStopped);
        //jsonSimEvent.put(FRONTLIGHT, frontLight);
        //jsonSimEvent.put(HIGHBEAM, highbeam);
        jsonSimEvent.put(LEFTLIGHT, leftLight);
        jsonSimEvent.put(RIGHTLIGHT, rightLight);
        //jsonSimEvent.put(EMERGENCY, emergency);
        jsonSimEvent.put(BRAKELIGHT, brakeLight);
        jsonSimEvent.put(BACKDRIVE, backdrive);

        return jsonSimEvent;
    }
}
