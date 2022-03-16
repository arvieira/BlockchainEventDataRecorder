package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering;

import br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.SimTransaction;
import br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.sensors.SimEvent;
import br.ufrj.nce.labnet.vehicleunit.vehicle.signer.CryptoModule;
import br.ufrj.nce.labnet.vehicleunit.vehicle.IntelligentVehicleWitness;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.LogEvent;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.SimLogger;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.lib.util.scheduling.Event;

import java.security.KeyPair;

public class TransactionHandler {

    // Variável que representa o veículo
    private final IntelligentVehicleWitness vehicle;

    // Variáveis para operar o veículo
    private final VehicleOperatingSystem operatingSystem;
    private final ClusteringStateMachine clusteringStateMachine;

    // Variáveis para operar os logs
    private final UnitLogger logger;
    private final SimLogger simLogger;

    // Variáveis para operar a criptografia
    private final CryptoModule crypto;
    private final KeyPair keyPair;

    // Evento para indicar que o veículo deve enviar uma transação
    private Event sendTransactionTimeout;


    // Construtor
    public TransactionHandler(IntelligentVehicleWitness vehicle, CryptoModule crypto, KeyPair keyPair) {
        this.vehicle = vehicle;

        operatingSystem = vehicle.getOperatingSystem();
        clusteringStateMachine = vehicle.getStateMachine();

        logger = vehicle.getLog();
        simLogger = vehicle.getSimLogger();

        this.crypto = crypto;
        this.keyPair = keyPair;
    }

    // Agenda um evento para enviar transações em tempos constantes com os estados dos sensores
    public void startTransactions() {
        if (!vehicle.naturalWorkload) {
            // Cria o novo evento para agendar o próximo envio de transações
            sendTransactionTimeout = new Event(operatingSystem.getSimulationTime() + (vehicle.transactionUnit * vehicle.transactionInterval), vehicle);
            operatingSystem.getEventManager().addEvent(sendTransactionTimeout);
        }
    }

    // Método para tratar o evento de ter que enviar uma transação
    public void handleSensorUpdate() {
        // Garantindo o consumo do evento
        sendTransactionTimeout = null;

        // Se o veículo é um líder ou um membro, ele pode submeter uma transação
        if (clusteringStateMachine.isLeader() || clusteringStateMachine.isMember()) {
            // Logando o início do processo
            logger.warnSimTime(vehicle, "Veículo {} realizando transação no ledger", operatingSystem.getId());

            // Cria um evento novo e pega os dados dos sensores
            SimEvent simEvent = new SimEvent(vehicle);
            simEvent.getState();

            // Criar uma transação
            SimTransaction transaction = new SimTransaction(vehicle, simEvent, operatingSystem.getSimulationTime());

            // Assinando a transação e colocando a chave pública que pode ser verificada por PKI
            transaction.setSign(crypto.sign(transaction.serialize(), keyPair.getPrivate()), keyPair.getPublic());

            // Tratando a transação gerada
            if (clusteringStateMachine.isLeader()) {
                logger.warnSimTime(
                        vehicle,
                        new LogEvent(
                                LogEvent.TRANSACTION,
                                LogEvent.LEADER + " " + operatingSystem.getId(),
                                LogEvent.GROUP + " " + vehicle.getGroupManager().getCompleteGroupId(),
                                transaction).toString()
                );
            } else {
                logger.warnSimTime(
                        vehicle,
                        new LogEvent(
                                LogEvent.TRANSACTION,
                                LogEvent.MEMBER + " " + operatingSystem.getId(),
                                LogEvent.GROUP + " " + vehicle.getGroupManager().getCompleteGroupId(),
                                transaction).toString()
                );
            }
        }

        // Agendando o envio da próxima transação
        startTransactions();
    }

    // Getter do evento sendTransactionTimeout
    public Event getSendTransactionTimeout() {
        return sendTransactionTimeout;
    }

}
