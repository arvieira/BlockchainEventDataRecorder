package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering;

import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages.*;
import br.ufrj.nce.labnet.vehicleunit.vehicle.IntelligentVehicleWitness;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.LogEvent;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.Node;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.SimLogger;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.rti.TIME;
import org.eclipse.mosaic.lib.objects.addressing.SourceAddressContainer;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import java.util.ArrayList;
import java.util.Random;

public class MsgHandler {

    // Definições relativas ao tempo para envio de convites
    final static int INVITATION_TIME_INTERVAL = 3;                          // Quantidade de tempo entre o envio dos convites
    final static long INVITATION_TIME_UNIT = TIME.SECOND;                   // Unidade do tempo para compor o intervalo de convites

    // Definições relativas ao tempo de espera aleatória
    //private final static int RANDOM_ANSWER_TIME_INTERVAL = 3;                      // Intervalo de tempo no qual vou sortear um inteiro para esperar
    //private final static long  RANDOM_ANSWER_TIME_UNIT = TIME.SECOND;			// Unidade de tempo para compor o intervalo de espera aleatória
    private final static int RANDOM_ANSWER_TIME_INTERVAL = 50;
    private final static long  RANDOM_ANSWER_TIME_UNIT = TIME.MILLI_SECOND;

    // Mensagem armazenada para ser respondida ao final do timeout aleatório
    // Será respondida com uma mensagem RESP
    private ReceivedV2xMessage waitingToAnswerMessage;

    // Objetos necessários para operar o veículo
    private final IntelligentVehicleWitness vehicle;
    private final VehicleOperatingSystem operatingSystem;

    private final ClusteringStateMachine clusteringStateMachine;
    private EventHandler eventHandler;
    private final GroupManager groupManager;

    private final UnitLogger logger;
    private final SimLogger simLogger;



    // Construtor
    public MsgHandler(IntelligentVehicleWitness vehicle) {
        this.vehicle = vehicle;
        operatingSystem = vehicle.getOperatingSystem();

        clusteringStateMachine = vehicle.getStateMachine();
        groupManager = vehicle.getGroupManager();

        logger = vehicle.getLog();
        simLogger = vehicle.getSimLogger();
    }

    // Setter do EventHandler
    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }




    // Getter da mensagem que está esperando para ser respondida
    public ReceivedV2xMessage getWaitingToAnswerMessage() {
        return waitingToAnswerMessage;
    }




    // Setter da mensagem que está esperando para ser respondida
    public void setWaitingToAnswerMessage(ReceivedV2xMessage waitingToAnswerMessage) {
        this.waitingToAnswerMessage = waitingToAnswerMessage;
    }




    // Método para tratar o recebimento de uma mensagem do tipo convite (CONV)
    public void handleInvitationMsg(ReceivedV2xMessage receivedV2xMessage) {
        // Pega a mesagem do tipo convite de dentro da comunicação recebida
        final InvitationMsg message = (InvitationMsg) receivedV2xMessage.getMessage();

        // Se eu estou no estado SOLE e recebo um convite, passo ao estado WAITING e inicio o temporizador
        if (clusteringStateMachine.isSole()) {
            // Registro o recebimento do convite
            logger.infoSimTime(vehicle, "CONV recebido, iniciando random para responder de {}", receivedV2xMessage.getMessage().getRouting().getSource().getSourceName());
            logger.infoSimTime(vehicle, "CONV: {}", message.toString());

            // Passo ao estado WAITING
            clusteringStateMachine.setState(ClusteringStateMachine.WAITING);
            logger.info("WAITING"); // Registrando WAITING no log

            // Modifica a cor do carro
            operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.WAITING_COLOR));

            // Salvo a mensagem a ser respondida e inicio o timer aleatório para responder ao convite
            waitingToAnswerMessage = receivedV2xMessage;
            eventHandler.setAnswerTimeout(new Event(
                    operatingSystem.getSimulationTime() + ((new Random().nextInt(RANDOM_ANSWER_TIME_INTERVAL) + 1) * RANDOM_ANSWER_TIME_UNIT), vehicle));
            operatingSystem.getEventManager().addEvent(eventHandler.getAnswerTimeout());
        }

        // Se estou no estado MEMBER
        if (clusteringStateMachine.isMember()) {
            // Acho que tem um problema aqui se em um grupo de 2 veículos (líder e membro),
            // um membro se afasta do líder momentaneamente e depois retorna.
            // Nesse momento, o líder pode considerar que o o membro se foi e ficar SOLE com o memberList null
            // Mas o membro ainda se considera membro pq ainda não expirou o seu prazo.
            // Nesse caso, um membro recebe um convite do seu próprio líder que está se considerando sozinho com memberList == null
            // Isso não afeta a criação dos grupos, apenas um membro terá a atualização dos seus dados no próximo
            // loop de envio de convites
            if (receivedV2xMessage.getMessage().getRouting().getSource().getSourceAddress().equals(groupManager.getMyLeader().getAddress().getSourceAddress())
                    && message.getMembersList() == null) {
                logger.infoSimTime(vehicle, "PROBLEMA: Líder esqueceu o membro, mas o membro não esqueceu o líder");

                // Modifica o status e a cor do carro
                clusteringStateMachine.setState(ClusteringStateMachine.SOLE);
                operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.SOLE_COLOR));

                // Reinicialização do processo, como se estivesse abandonando o grupo
                groupManager.setMyLeader(null, null);
                groupManager.setMyMembers(null);
                groupManager.setGroupSize(1);

                // trata novamente a mensagem
                handleInvitationMsg(receivedV2xMessage);
            } else {
                // Recebi um convite do meu próprio líder, reseto o timer para sair do grupo e envio o KeepAlive
                if (receivedV2xMessage.getMessage().getRouting().getSource().getSourceAddress().equals(groupManager.getMyLeader().getAddress().getSourceAddress())) {
                    // Cancelo o timer corrente e crio um novo com o tempo maior que o tempo máximo entre convites.
                    eventHandler.setMemberKeepAliveTimeout(null);
                    eventHandler.setMemberKeepAliveTimeout(new Event(
                            operatingSystem.getSimulationTime() + ((INVITATION_TIME_INTERVAL + 2) * INVITATION_TIME_UNIT), vehicle)
                    );
                    operatingSystem.getEventManager().addEvent(eventHandler.getMemberKeepAliveTimeout());

                    // Atualizando a lista de membros no grupo pelo convite
                    // Aqui também são recebidas as chaves públicas vindas do líder
                    groupManager.setMyMembers(message.getMembersList());

                    // Registrando o recebimento do pacote de presença do líder
                    logger.infoSimTime(vehicle, "Recebendo convite do leader como keep alive");
                    logger.infoSimTime(vehicle, "CONV: {}", message.toString());
                    logger.infoSimTime(vehicle, "Enviando resposta keep alive para o leader");

                    // Um membro imprime as chaves públicas de todos do quorum
                    // O líder imprimindo o para verificar está no EventHandler.startInviting() no momento que ele está verificando se os
                    // membros estão mortos
//                    for (Node node:groupManager.getQuorum()) {
//                        logger.info("Chave pública do membro {} = {}", node.getAddress().getSourceName(), node.getPublicKey());
//                    }

                    // Atualizando a lista de membros a partir da mensagem de convite do líder
                    groupManager.setMyMembers(new ArrayList<>(message.getMembersList()));

                    // Atualizando o tamanho do grupo
                    groupManager.setGroupSize(message.getGroupSize());

                    // Enviar de volta um keepAlive do membro do grupo
                    sendAdHocMessage(receivedV2xMessage.getMessage().getRouting().getSource(), MessageType.KEEP_ALIVE, null);
                } else {
                    // Tratar a mudança de grupo no merge
                    vehicle.mergeChangeGroup(message);
                }
            }
        }
    }

    // Método para tratar o recebimento de uma mensagem do tipo resposta (RESP)
    public void handleAnswerMsg(ReceivedV2xMessage receivedV2xMessage) {
        // Pega a mesagem do tipo convite de dentro da comunicação recebida
        final AnswerMsg message = (AnswerMsg) receivedV2xMessage.getMessage();

        if (clusteringStateMachine.isSole() || clusteringStateMachine.isWaiting() || clusteringStateMachine.isLeader()) {
            // Logando a mensagem do tipo RESP recebida
            logger.infoSimTime(vehicle, "RESP recebido enviando ACK e adicionando ao grupo o {}", receivedV2xMessage.getMessage().getRouting().getSource().getSourceName());
            logger.infoSimTime(vehicle, "Mensagem: {}", message.toString());

            // Daqui em diante é o comportamento comum a todos os estados
            // Atualiza o número de membros no grupo
            groupManager.increaseGroupSize();

            // Switch da máquina de estados, aqui eu diferencio o comportamento do SOLE, WAITING e LEADER
            switch (clusteringStateMachine.getState()) {
                case ClusteringStateMachine.SOLE:
                    // Inicializa um novo grupo
                    startGroup(clusteringStateMachine, groupManager);
                    logger.warnSimTime(
                            vehicle,
                            new LogEvent(
                                    LogEvent.CREATE_CHANNEL,
                                    LogEvent.LEADER + " " + operatingSystem.getId(),
                                    LogEvent.MEMBER + " " + receivedV2xMessage.getMessage().getRouting().getSource().getSourceName(),
                                    LogEvent.GROUP + " " + groupManager.getCompleteGroupId()).toString()
                    );
                    break;
                case ClusteringStateMachine.WAITING:
                    // Inicializa um novo grupo
                    startGroup(clusteringStateMachine, groupManager);
                    logger.warnSimTime(
                            vehicle,
                            new LogEvent(
                                    LogEvent.CREATE_CHANNEL,
                                    LogEvent.LEADER + " " + operatingSystem.getId(),
                                    LogEvent.MEMBER + " " + receivedV2xMessage.getMessage().getRouting().getSource().getSourceName(),
                                    LogEvent.GROUP + " " + groupManager.getCompleteGroupId()).toString()
                    );

                    // Cancelar o temporizador aleatório
                    // Garantindo que o evento e a mensagem serão consumidos
                    eventHandler.setAnswerTimeout(null);
                    waitingToAnswerMessage = null;
                    break;
                case ClusteringStateMachine.LEADER:
                    logger.infoSimTime(vehicle, "NOVO MEMBRO ADICIONADO: {}", receivedV2xMessage.getMessage().getRouting().getSource().getSourceName());
                    logger.warnSimTime(
                            vehicle,
                            new LogEvent(
                                    LogEvent.JOIN_PEER,
                                    LogEvent.MEMBER + " " + receivedV2xMessage.getMessage().getRouting().getSource().getSourceName(),
                                    LogEvent.GROUP + " " + groupManager.getCompleteGroupId()).toString()
                    );
                    break;
                default:
                    // Esse caso é para gerar um erro, estou colocando false aqui apenas para o protocolo não dar null pointer exception
                    // newGroup = false;

                    // Logando um possível erro na máquina de estados
                    logger.infoSimTime(vehicle, "Erro na máquina de estados ao receber um RESP");
            }

            // Cria o vetor de membros se este não existir e adiciona o novo membro com o alive true
            if (groupManager.getMyMembers() == null)
                groupManager.setMyMembers(new ArrayList<>());
            groupManager.addMember(new Node(receivedV2xMessage.getMessage().getRouting().getSource(), true, message.getPublicKey()));

            // Envia o ACK aqui
            logger.infoSimTime(vehicle, "Enviando ACK para novo membro");
            sendAdHocMessage(receivedV2xMessage.getMessage().getRouting().getSource(), MessageType.ACK, null);
        }
    }

    // Método para inicializar o grupo.
    // Utilizado por um líder
    private void startGroup(ClusteringStateMachine clusteringStateMachine, GroupManager groupManager) {
        // Passando ao estado de líder e logando o que ocorreu
        clusteringStateMachine.setState(ClusteringStateMachine.LEADER);
        logger.info("LEADER");

        // Registra a criação do grupo no log
        simLogger.groupCreated(groupManager);

        // Modifica a cor do líder para amarelho
        operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.LEADER_COLOR));

        // Define a cor do grupo aleatóriamente
        groupManager.setMyGroupColor(IntelligentVehicleWitness.MEMBER_COLORS.get(new Random().nextInt(IntelligentVehicleWitness.MEMBER_COLORS.size())));

        // Cria um nó de líder representando ele mesmo e preenche a sua variável de líder
        groupManager.setMyLeader(
                new Node(new SourceAddressContainer(
                        operatingSystem.getAdHocModule().getSourceAddress(),
                        operatingSystem.getId(),
                        operatingSystem.getPosition()
                    ),
                true,
                vehicle.getPublicKey()),
                operatingSystem.getId() + "_" + groupManager.getGroupId()
        );
    }

    // Método para tratar o recebimento de uma mensagem do tipo acknowledgement (ACK)
    public void handleAckMsg(ReceivedV2xMessage receivedV2xMessage) {
        // Pega a mesagem do tipo convite de dentro da comunicação recebida
        final AckMsg message = (AckMsg) receivedV2xMessage.getMessage();

        // Somente aspirantes se importam com ACK para poder virar membros
        if (clusteringStateMachine.isAsp()) {
            // Passando ao estado de membro
            clusteringStateMachine.setState(ClusteringStateMachine.MEMBER);

            // Modifica a cor do veículo
            groupManager.setMyGroupColor(message.getGroupColor());
            operatingSystem.applyVehicleParametersChange(operatingSystem.requestVehicleParametersUpdate().changeColor(groupManager.getMyGroupColor()));

            // Quando se torna membro de um grupo, pega a referência do seu líder em myleader e coloca o alive em true
            groupManager.setMyLeader(
                    new Node (receivedV2xMessage.getMessage().getRouting().getSource(), true, message.getLeaderPublicKey()),
                    message.getGroupId()
            );

            // Criando e populando a lista de membros
            groupManager.setMyMembers(new ArrayList<>(message.getMembersList()));

            // Registra tudo que aconteceu nos logs
            logger.infoSimTime(vehicle, "ACK recebido, se tornando membro do grupo {}", receivedV2xMessage.getMessage().getRouting().getSource().getSourceName());
            logger.infoSimTime(vehicle, "Mensagem: {}", message.toString());
            logger.info("MEMBER");
            logger.warnSimTime(
                    vehicle,
                    new LogEvent(
                            LogEvent.JOIN_RESUME_CHANNEL,
                            LogEvent.MEMBER + " " + operatingSystem.getId(),
                            LogEvent.GROUP + " " + message.getGroupId()).toString()
            );

            // Garantindo que o evento temporizador do ACK será consumido
            eventHandler.setAckTimeout(null);

            // Garantindo o cancelamento de um possível timer corrente e crio um novo com o tempo maior que o tempo máximo entre convites.
            eventHandler.setMemberKeepAliveTimeout(null);
            eventHandler.setMemberKeepAliveTimeout(new Event(
                    operatingSystem.getSimulationTime() + ((INVITATION_TIME_INTERVAL + 2) * INVITATION_TIME_UNIT), vehicle)
            );
            operatingSystem.getEventManager().addEvent(eventHandler.getMemberKeepAliveTimeout());
        }
    }

    // Método para invocado por um líder para tratar o recebimento de um KeepAlive de um membro
    public void handleKeepAliveMsg(ReceivedV2xMessage receivedV2xMessage) {
        // Recebendo a mensagem e passando para o tipo correto que é KeepAlive
        final KeepAliveMsg message = (KeepAliveMsg) receivedV2xMessage.getMessage();

        if (clusteringStateMachine.isLeader()){
            // Registrando o recebimento no log
            logger.infoSimTime(vehicle, "Recebido keep alive de um membro: {}", message.getRouting().getSource().getSourceName());
            logger.infoSimTime(vehicle, "CONV: {}", message.toString());

            // Olhando toda a lista de membros e setando o KeepAlive daquele que enviou a mensagem
            for (Node member : groupManager.getMyMembers()) {
                if (member.getAddress().getSourceAddress().equals(message.getRouting().getSource().getSourceAddress())) {
                    member.setAlive();
                    logger.infoSimTime(vehicle, "Registrando keep alive de um membro: {}", member.getNodeName());
                }
            }
        }
    }

    // Método que o líder executa quando recebe uma mensagem de MergeLeaving
    // um membro está deixando o grupo pq foi para outro grupo
    public void handleMergeLeavingMsg(ReceivedV2xMessage receivedV2xMessage) {
        // Recebendo a mensagem e passando para o tipo correto que é KeepAlive
        final MergeLeavingMsg message = (MergeLeavingMsg) receivedV2xMessage.getMessage();

        if (clusteringStateMachine.isLeader()) {
            // Removendo membro que se foi
            if (vehicle.getGroupManager().removeMember(vehicle, message.getSrcId())) {
                logger.infoSimTime(vehicle, "Membro que sofreu merge removido: {}", message.getSrcId());
            } else {
                logger.infoSimTime(vehicle, "Membro que sofreu merge já havia sido removido");
            }

            // Verificando se o grupo não acabou pq o membro foi embora
            vehicle.getGroupManager().verifyAndDestroyGroup(vehicle);
        }
    }





    // Método para o envio de mensagens
    // O content só utilizado para a mensagem de MergeLeaving pq eu não tenho de onde retirar
    // o grupo que veio na mensagem do CONV do novo grupo, apenas respondendo a que veio
    public void sendAdHocMessage(SourceAddressContainer destiny, MessageType messageType, Object content) {
        // Variáveis necessárias para o envio das mensagens
        V2xMessage message = null;
//        TopocastDestinationAddress tda;
//        DestinationAddressContainer dac;
        MessageRouting routing;

        // Convites e blocos gênesis são enviados em broadcast, as demais mensagens são em resposta a origem
        if (messageType.equals(MessageType.INVITATION)) {
            // Indica que o destino será broadcast para INVITATION (convites) e GENESIS (blocos gênesis)
//            tda = TopocastDestinationAddress.getBroadcastSingleHop();
//            // Coloca o endereço de destino, junto com o canal wifi em um container
//            dac = DestinationAddressContainer.createTopocastDestinationAddressAdHoc(tda, AdHocChannel.CCH);
//            // Define a rota da mensagem, indicando o container criado na linha anterior e pegando um endereço padrão para a origem do pacote
//            routing = new MessageRouting(dac, operatingSystem.generateSourceAddressContainer());
            routing = operatingSystem.getAdHocModule().createMessageRouting().topoBroadCast(1);
        } else {
            // Indica que o destino será o endereço de origem do pacote inicial
            // tda = new TopocastDestinationAddress(destiny.getSourceAddress());
            routing = operatingSystem.getAdHocModule().createMessageRouting().topoCast(
                    destiny.getSourceAddress().getIPv4Address().getAddress(),
                    1);
        }


        // O método getVehicleData pode retornar null em alguns casos, o que geraria NullPointerException na chamada do
        // getName. Para resolver isso, se eu não tiver informações do veículo para o envio da mensagem, eu mando null
        // mesmo e este será passado para String resolvendo o problema
        VehicleData info = operatingSystem.getVehicleData();
        String srcId;
        if (info != null)
            srcId = operatingSystem.getVehicleData().getName();
        else
            srcId = null;

        // Adiciona a rota à mensagem, juntamente com o payload da mesma
        GroupManager groupManager = vehicle.getGroupManager();
        switch (messageType) {
            case INVITATION:
                // CONV
                // Cria uma mensagem do tipo convite e coloca no payload a lista de membros (LUM)
                // A própria mensagem calcula o CIG da lista
                // Usada nos estados SOLE e LEADER
                message = new InvitationMsg(routing, groupManager.getMyLeader(), groupManager.getMyMembers(), groupManager.getCompleteGroupId());
                break;
            case ANSWER:
                // RESP
                // Responde a origem com uma mensagem do tipo RESP
                // Usada no estado WAITING
                message = new AnswerMsg(routing, srcId, destiny.getSourceName(), vehicle.getPublicKey());
                break;
            case ACK:
                // ACK
                // Usada no estado SOLE, após receber uma RESP
                message = new AckMsg(routing, srcId, destiny.getSourceName(), groupManager.getMyMembers(), vehicle.getPublicKey(), groupManager.getMyGroupColor(), groupManager.getCompleteGroupId());
                break;
            case KEEP_ALIVE:
                // KeepAlive
                // Mensagem montada pelo MEMBER para mostrar ao LEADER que ainda está no grupo
                message = new KeepAliveMsg(routing, srcId, destiny.getSourceName());
                break;
            case MERGE_LEAVING:
                // MergeLeaving
                // Mensagem enviada por um membro que está passando por um MERGE ao seu antigo
                // líder para que ele agilize seu processo de retirada
                message = new MergeLeavingMsg(routing, srcId, destiny.getSourceName(), (String) content);
                break;
            default:
                // Erro genérico
                logger.infoSimTime(vehicle, "Mensagem: Tipo desconhecido de mensagem para enviar.");
        }


        //logger.infoSimTime(vehicle, "ENVIANDO MENSAGEM: {}", message.toString());
        // Envio efetivo da mensagem
        operatingSystem.getAdHocModule().sendV2xMessage(message);
    }
}
