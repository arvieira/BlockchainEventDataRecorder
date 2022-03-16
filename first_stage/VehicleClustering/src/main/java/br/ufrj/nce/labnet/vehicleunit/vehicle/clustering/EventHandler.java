package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering;

import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages.MessageType;
import br.ufrj.nce.labnet.vehicleunit.vehicle.*;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.LogEvent;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.Node;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.SimLogger;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;

import java.util.Random;

public class EventHandler {

    // Definições relativas ao tempo para recebimento de ACKs
    //private final static int ACK_TIME_INTERVAL = 10;                         // Quantidade de tempo para esperar receber o ACK do RESP
    private final static int ACK_TIME_INTERVAL = 3;
    private final static long ACK_TIME_UNIT = TIME.SECOND;                  // Unidade do tempo para compor o intervalo do ACK


    // Eventos de temporização
    private Event inviteTimeout;                                            // Timeout para envio de um novo convite
    private Event answerTimeout;                                            // Timeout do temporizador aleatório para envio de uma resposta
    private Event memberKeepAliveTimeout;                                   // Timeout do membro para a espera de convites de seu líder, saindo do grupo
    private Event ackTimeout;                                               // Timeout do ASP para receber um ACK da sua RESP


    // Objetos necessários para operar o veículo
    private final IntelligentVehicleWitness vehicle;
    private final VehicleOperatingSystem operatingSystem;

    private final ClusteringStateMachine clusteringStateMachine;
    private final MsgHandler msgHandler;
    private final GroupManager groupManager;

    private final UnitLogger logger;
    private final SimLogger simLogger;


    public EventHandler(IntelligentVehicleWitness vehicle) {
        this.vehicle = vehicle;
        operatingSystem = vehicle.getOperatingSystem();

        clusteringStateMachine = vehicle.getStateMachine();
        msgHandler = vehicle.getMsgHandler();
        groupManager = vehicle.getGroupManager();

        logger = vehicle.getLog();
        simLogger = vehicle.getSimLogger();
    }




    // Getter do evento de timeout do convite
    public Event getInviteTimeout() {
        return inviteTimeout;
    }

    // Getter do evento de timeout da resposta
    public Event getAnswerTimeout() {
        return answerTimeout;
    }

    // Getter do evento de timeout do KeepAlive de um membro
    public Event getMemberKeepAliveTimeout() {
        return memberKeepAliveTimeout;
    }

    // Getter do timeout de um ACK
    public Event getAckTimeout() {
        return ackTimeout;
    }





    // Setter do evento de timeout da resposta
    public void setAnswerTimeout(Event answerTimeout) {
        this.answerTimeout = answerTimeout;
    }

    // Setter do evento de timeout do KeepAlive de um membro
    public void setMemberKeepAliveTimeout(Event memberKeepAliveTimeout) {
        this.memberKeepAliveTimeout = memberKeepAliveTimeout;
    }

    // Setter do timeout de um ACK
    public void setAckTimeout(Event ackTimeout) {
        this.ackTimeout = ackTimeout;
    }




    // Método chamado quando o veículo inicia e quando o timeout de convite espira
    // Esse timeout só será chamado nos estados SOLE e LEADER, que são aqueles que enviam convites
    public void startInviting() {
        // Cria o novo evento para agendar o próximo envio de convite
        inviteTimeout = new Event(operatingSystem.getSimulationTime() + ((new Random().nextInt(MsgHandler.INVITATION_TIME_INTERVAL) + 1) * MsgHandler.INVITATION_TIME_UNIT), vehicle);
        operatingSystem.getEventManager().addEvent(inviteTimeout);

        switch (clusteringStateMachine.getState()) {
            case ClusteringStateMachine.SOLE:
                // Envia o convite no estado SOLE
                logger.infoSimTime(vehicle, "Enviando convites CONV em broadcast");
                msgHandler.sendAdHocMessage(null, MessageType.INVITATION, null);
                break;
            case ClusteringStateMachine.LEADER:
                // Coloca todos os membros como mortos e espera suas respostas para setar o keepalive de novo

                // Imprime a chave pública do líder para verificar
                // O par disso está no MsgHandler.handleInvitationMsg, quando um membro atualiza seu quorum da rede blockchain
//                logger.info("Chave pública do Líder: {}", vehicle.getPublicKey());
                if (groupManager.getMyMembers() != null) {
                    for (Node member : groupManager.getMyMembers()) {
                        member.unsetAlive();
                        logger.info("Marcando como morto para esperar resposta do convite: {}", member.getNodeName());
//                        logger.info("Chave pública do {}: {}", temp.getAddress().getSourceName(), temp.getPublicKey());
                    }
                }

                simLogger.sendInvitation(groupManager);
                msgHandler.sendAdHocMessage(null, MessageType.INVITATION, null);
                break;
            default:
        }
    }




    // Método para tratar o timeout para enviar o próximo convite
    // Aqui o líder pode perceber que seu grupo acabou  (Término do grupo para o líder)
    public void handleInviteTimeoutEvent() {
        // Estando no estado LEADER, antes de enviar o próximo conjunto de convites, verifico por membros mortos e os retiro
        if (clusteringStateMachine.isLeader()) {
            // Garantindo que o evento será consumido
            inviteTimeout = null;

            vehicle.getLog().infoSimTime(vehicle, "KeepAliveTimeout!");
            if (groupManager.getMyMembers() != null) {
                // Remove membros mortos
                boolean memberRemoved = groupManager.removeDeadMembers(vehicle);

                // Se após a limpeza do grupo o líder ficar sozinho,
                // ele se torna SOLE e procede à destruição do grupo
                groupManager.verifyAndDestroyGroup(vehicle);

                if (!memberRemoved) {
                    vehicle.getLog().infoSimTime(vehicle, "Nenhum membro morto encontrado");
                }
            }
        }
    }

    // Método para tratar o timeout do temporizador aleatório de quando recebe um convite
    public void handleAnswerEvent() {
        // Estando no estado de WAITING e tendo o timeout do temporizador aleatório
        // passa ao estado de ASP e envia a resposta a origem
        if (clusteringStateMachine.isWaiting()) {
            clusteringStateMachine.setState(ClusteringStateMachine.ASP);

            // Modifica a cor do carro
            operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.ASP_COLOR));

            // Enviando resposta do convite
            logger.infoSimTime(vehicle, "Timeout, enviando RESP e aguardando ACK");
            msgHandler.sendAdHocMessage(msgHandler.getWaitingToAnswerMessage().getMessage().getRouting().getSource(), MessageType.ANSWER, null);

            // Imprime no log o novo status
            logger.info("ASP");

            // Garantindo que o evento e a mensagem serão consumidos
            answerTimeout = null;
            msgHandler.setWaitingToAnswerMessage(null);

            // Agendando o temporizador do ACK, caso não receba a tempo, ele volta para o estado SOLE
            ackTimeout = new Event(operatingSystem.getSimulationTime() + (ACK_TIME_INTERVAL * ACK_TIME_UNIT), vehicle);
            operatingSystem.getEventManager().addEvent(ackTimeout);
        }
    }

    // Método que um membro vai executar quando não recebeu convites a tempo de seu líder
    // Aqui um mebro pode perceber que seu líder se foi (Término do grupo para um membro)
    public void handleMemberKeepAliveTimeoutEvent() {
        // O líder sumiu, não pertenço mais a um grupo.
        logger.infoSimTime(vehicle, "Timeout, líder não responde");
        logger.infoSimTime(vehicle, "Abandonando grupo");
        clusteringStateMachine.setState(ClusteringStateMachine.SOLE);                   // Arruma o estado na máquina de estados como SOLE novamente
        logger.info("SOLE");                                        // Registra o evento

        logger.warnSimTime(
                vehicle,
                new LogEvent(
                        LogEvent.PAUSE_PEER,
                        LogEvent.MEMBER + " " + operatingSystem.getId(),
                        LogEvent.GROUP + " " + groupManager.getCompleteGroupId()).toString()
        );

        // Modifica a cor do carro
        operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.SOLE_COLOR));

        // Garantindo o consumo do evento e a reinicialização do processo
        groupManager.setMyLeader(null, null);
        groupManager.setMyMembers(null);
        groupManager.setGroupSize(1);
        memberKeepAliveTimeout = null;
    }

    // Método que um ASP vai executar quando não recebeu um ACK a tempo
    public void handleAckTimeoutEvent() {
        // Se estou no estado ASP e não recebi o ACK a tempo, volto ao início da máquina de estados como SOLE
        if (clusteringStateMachine.isAsp()) {
            clusteringStateMachine.setState(ClusteringStateMachine.SOLE);

            // Modifica a cor do carro
            operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.SOLE_COLOR));

            // Registrando no log o não recebimento do ACK a tempo
            logger.infoSimTime(vehicle, "Timeout, o ACK para se tornar membro não foi recebido. Reiniciando máquina de estados.");

            // Imprime no log o novo status
            logger.info("SOLE");

            // Garantindo que o evento temporizador do ACK será consumido
            ackTimeout = null;
        }
    }

}
