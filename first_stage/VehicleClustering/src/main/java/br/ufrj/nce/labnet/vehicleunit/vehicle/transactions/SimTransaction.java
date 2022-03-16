package br.ufrj.nce.labnet.vehicleunit.vehicle.transactions;

import br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.hasher.Hasher;
import br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.sensors.SimEvent;
import br.ufrj.nce.labnet.vehicleunit.vehicle.IntelligentVehicleWitness;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.json.JSONObject;

import java.security.PublicKey;


public class SimTransaction {

    // Constantes para criação do objeto json
    private static final String ORIGIN = "OriginId";
    private static final String SIMEVENT = "SimEvent";
    private static final String HASH = "Hash";
    private static final String TRANSACTION_TIMESTAMP = "TransactionTimestamp";
    private static final String SIGN = "Signature";
    private static final String PUBLIC_KEY = "PublicKey";

    private IntelligentVehicleWitness originVehicle;      // Objeto do veículo que realizou a transação
    private String originId;                              // Veículo que realizou a transação
    private SimEvent simEvent;                            // Evento que irá compor a transação
    private String hash;                                  // Hash da transação para identificar ela
    private long timestamp;                               // Timestamp de quando foi criada a transação, serve para ordenação
    private String sign;                                  // Assinatura da transação

    // É a chave pública de quem assinou a transação.
    // A garantia de que é realmente da pessoa se dará por uma PKI.
    // Isso foi necessário pq o veículo pode sair do grupo e a sua transação
    // permanecer na transaction pool dos carros para ser submetida. Nesse
    // caso, os carros não terão mais a chave pública nos seus gerenciadores
    // de grupo.
    private PublicKey publicKey;


    // Construtor da transação
    public SimTransaction(IntelligentVehicleWitness vehicle, SimEvent simEvent, long timestamp) {
        this.originVehicle = vehicle;
        this.originId = vehicle.getOperatingSystem().getId();
        this.simEvent = simEvent;
        this.hash = Hasher.calculateHash(serialize());
        this.timestamp = timestamp;
        this.sign = "";
    }


    // Transforma transação em uma string
    public String serialize() {
        String content = originId;

        content += simEvent.toString();

        content += timestamp;

        return content;
    }




    // Getter do veículo que originou a transação
    public IntelligentVehicleWitness getOriginVehicle() {
        return originVehicle;
    }

    // Getter do hash identificador da transação
    String getHash() {
        return hash;
    }

    // Getter da assinatura da transação
    public String getSign() {
        return sign;
    }

    // Getter da origem da transação
    public String getOriginId() {
        return originId;
    }

    // Getter da lista de eventos dos sensores
    public SimEvent getSimEvent() {
        return simEvent;
    }

    // Getter do timestamp da transação
    public long getTimestamp() {
        return timestamp;
    }

    // Getter da chave pública correspondente a assinatura do objeto
    public PublicKey getPublicKey() {
        return publicKey;
    }





    // Setter da assinatura
    public void setSign(String sign, PublicKey publicKey) {
        this.sign = sign;
        this.publicKey = publicKey;
    }

    // Setter da origem da transação
    public void setOriginId(String originId) {
        this.originId = originId;
        this.hash = Hasher.calculateHash(serialize());
    }

    // Setter da lista de eventos dos sensores
    public void setSimEvent(SimEvent simEvent) {
        this.simEvent = simEvent;
        this.hash = Hasher.calculateHash(serialize());
    }





    // Método para impressão do bloco
    @Override
    public String toString() {
        String content = "\n\t\t\t\t\tOriginId: " + originId;
        content += ",\n\t\t\t\t\tHash: " + hash;
        content += ",\n\t\t\t\t\tTransactionTimestamp: " + timestamp;
        content += ",\n\t\t\t\t\tSign: " + sign;
        content += ",\n\t\t\t\t\tPublicKey: " + publicKey.toString();
        content += ",\n\t\t\t\t\tEvents:[";
        content += simEvent.toString();
        content += "\n\t\t\t\t\t]\n\t\t\t\t";

        return content;
    }

    // Método para transformação do bloco em json
    public JSONObject toJson () {
        JSONObject json = new JSONObject();
        json.put(ORIGIN, originId);
        json.put(SIMEVENT, simEvent.toJson());
        json.put(HASH, hash);
        json.put(TRANSACTION_TIMESTAMP, timestamp);
        json.put(SIGN, sign);
        json.put(PUBLIC_KEY, publicKey.toString());

        return json;
    }
}
