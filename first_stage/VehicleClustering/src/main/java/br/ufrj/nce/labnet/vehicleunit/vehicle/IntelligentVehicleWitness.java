package br.ufrj.nce.labnet.vehicleunit.vehicle;

import br.ufrj.nce.labnet.vehicleunit.vehicle.signer.CryptoModule;
import br.ufrj.nce.labnet.vehicleunit.vehicle.signer.EC;
import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages.*;
import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.*;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.Node;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.SimLogger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.CamBuilder;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedAcknowledgement;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;
import org.eclipse.mosaic.fed.application.app.AbstractApplication;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.AdHocModuleConfiguration;
import org.eclipse.mosaic.fed.application.app.api.os.VehicleOperatingSystem;
import org.eclipse.mosaic.interactions.communication.V2xMessageTransmission;
import org.eclipse.mosaic.lib.objects.addressing.SourceAddressContainer;
import org.eclipse.mosaic.lib.objects.vehicle.VehicleData;
import org.eclipse.mosaic.lib.util.scheduling.Event;
import org.eclipse.mosaic.lib.enums.AdHocChannel;
import org.eclipse.mosaic.fed.application.ambassador.simulation.communication.ReceivedV2xMessage;
import org.json.simple.parser.ParseException;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;


public abstract class IntelligentVehicleWitness extends AbstractApplication<VehicleOperatingSystem> implements VehicleApplication, CommunicationApplication {

    // Liga e desliga o envio de transa????es
    public static boolean SEND_TRANSACTIONS = true;

    // Define o tamanho m??ximo dos grupos
    public static int GROUP_LIMITER = 0;

    // Liga e desliga o workload natural dos ve??culos.
    // Os ve??culos enviam quando tem atualiza????o dos sensores.
    // Isso desabilita o TRANSACTION_UNITS e TRANSACTION_INTERVAL
    // public static boolean NATURAL_WORKLOAD = false;

    // Comp??e com o intervalo para menos de 1 transa????o por segundo
    // Colocar TRANSACTION_UNITS em 1 para definir a transa????o por segundo de acordo com a tabela abaixo
    // Equivalencia de valores para TPS
    // 1000000000 = 1TPS
    // 500000000 = 2TPS
    // 333333333 = 3TPS
    // 250000000 = 4TPS
    // 200000000 = 5TPS
    // 125000000 = 8TPS
    // 100000000 = 10TPS
    // 50000000 = 20TPS
    // 25000000 = 40TPS
    // 20000000 = 50TPS
    // 10000000 = 100TPS
    // public static final long TRANSACTION_UNITS = 1;
    // public static final long TRANSACTION_INTERVAL = 100000000L;

    // Arquivo que guarda as configura????es de NATURAL_WORKLOAD, TRANSACTION_UNITS e TRANSACTION_INTERVAL
    // Vari??veis para guardar os par??metros
    private static final String PARAM_FILE = "./param.json";
    public boolean naturalWorkload;
    public long transactionUnit;
    public long transactionInterval;
    private static final String NATURAL_NAME = "natural";
    private static final String UNIT_NAME = "unit";
    private static final String INTERVAL_NAME = "interval";


    // Classes auxiliares para clustering
    // Objeto respons??vel por gerenciar a m??quina de estados de forma????o de grupos
    ClusteringStateMachine clusteringStateMachine;

    // Objeto respons??vel por gerenciar o grupo atual e os anteriores
    GroupManager groupManager;

    // M??dulo para tratamento de mensagens
    MsgHandler msgHandler;

    // M??dulo para tratamento de eventos
    private EventHandler eventHandler;

    // M??dulo para tratamento das transa????es
    private TransactionHandler transactionHandler;


    // M??dulo para escrita do log dos eventos do simulador
    private SimLogger simLogger;


    // Defini????es relativas a cores dos estados e dos diferentes grupos
    public final static Color SOLE_COLOR = Color.WHITE;
    public final static Color LEADER_COLOR = Color.YELLOW;
    public final static Color WAITING_COLOR = Color.ORANGE;
    public final static Color ASP_COLOR = Color.BLACK;
    public final static ArrayList<Color> MEMBER_COLORS  = new ArrayList<>(
            Arrays.asList(
                    Color.MAGENTA,
                    Color.BLUE,
                    Color.CYAN,
                    //Color.DARK_GRAY,
                    //Color.GRAY,
                    Color.GREEN,
                    Color.LIGHT_GRAY,
                    Color.PINK,
                    Color.RED)
    );

    // M??dulo de criptografia
    // Classe Singleton para realizar as opera????es de crypto
    private CryptoModule crypto;

    // Par de chaves criptogr??ficas do ve??culo
    private KeyPair keyPair;






    // M??todo chamado pelo simulador quando o ve??culo ?? ligado
    @Override
    public void onStartup() {
        // Lendo par??metros do arquivo de configura????o
        read_params();

    	// Inicializando a rede wifi do ve??culo
        getLog().infoSimTime(this, "Initialize application");
        getOperatingSystem().getAdHocModule().enable(new AdHocModuleConfiguration()
            .addRadio()
            .channel(AdHocChannel.CCH)
            //.power(10)
            .distance(50)
            .create());
        getLog().infoSimTime(this, "Activated AdHoc Module");


        // Inicializando par??metros do protocolo de clustering
        // Estado inicial sozinho SOLE
        clusteringStateMachine = new ClusteringStateMachine(ClusteringStateMachine.SOLE);

        // Registrando estado inicial SOLE no log
        getLog().info("SOLE");


        // Inicializando m??dulo de criptografia posso alternar entre Rsa() ou EC() (crypto = new RSA())
        crypto = new EC();

        // Gera????o do par de chaves do ve??culo
        keyPair = crypto.generateKeyPair();


        // Primeiro ID de grupo ser?? 0
        // O grupo do ve??culo j?? come??a com 1 integrante que ?? ele mesmo
        groupManager = new GroupManager(
                0,
                1,
                new Node(new SourceAddressContainer(
                        getOperatingSystem().getAdHocModule().getSourceAddress(),
                        getOperatingSystem().getId(),
                        getOperatingSystem().getPosition()
                    ),
                    true,
                    keyPair.getPublic()
                )
        );


        // Modifica a cor de todos os carros para SOLE logo ap??s eles entrarem no mapa
        getOperatingSystem().applyVehicleParametersChange(getOperatingSystem().requestVehicleParametersUpdate().changeColor(SOLE_COLOR));


        // Inicializa as classes auxiliares para tratar as mensagens, eventos, produ????o de logs e transa????es
        simLogger = new SimLogger(this);
        msgHandler = new MsgHandler(this);
        eventHandler = new EventHandler(this);
        msgHandler.setEventHandler(eventHandler);
        transactionHandler = new TransactionHandler(this, crypto, keyPair);

        // Inicializa o agendamento de eventos para o envio dos convites
        eventHandler.startInviting();

        // Se o envio de transa????es estiver ligado, inicializa o agendamento de eventos constantes para envio de transa????es
        if (SEND_TRANSACTIONS) {
            transactionHandler.startTransactions();
        }
    }


    // M??todo chamado toda vez que um temporizador termina
    @Override
    public void processEvent(Event event) {
        // ###################### Eventos de clustering ######################
        // Timeout para envio de convites
        if (event.equals(eventHandler.getInviteTimeout())) {
            // Se for l??der, precisa verififcar o grupo antes de enviar os convites
            eventHandler.handleInviteTimeoutEvent();

            // A cada evento de timeout de convites, reagenda o timeout para o envio de convites e envia os convites
            eventHandler.startInviting();
        }

        // Timeout do temporizador aleat??rio para o envio da mensagem de resposta RESP
        if (event.equals(eventHandler.getAnswerTimeout())) {
            eventHandler.handleAnswerEvent();
        }

        // Timeout do membro, indicando que seu l??der saiu do alcance
        if (event.equals(eventHandler.getMemberKeepAliveTimeout())) {
            eventHandler.handleMemberKeepAliveTimeoutEvent();
        }

        // Timeout para o recebimento do Ack vindo do l??der para se tornar um membro
        if (event.equals(eventHandler.getAckTimeout())) {
            eventHandler.handleAckTimeoutEvent();
        }
        // ###################### Eventos de clustering ######################



        // ################## Eventos de envio de transa????es ##################
        if (event.equals(transactionHandler.getSendTransactionTimeout())) {
            transactionHandler.handleSensorUpdate();
        }
        // ################## Eventos de envio de transa????es ##################
    }


    // M??todo chamado toda vez que uma mensagem ?? recebida
    public void onMessageReceived(ReceivedV2xMessage receivedV2xMessage) {
        // Aqui eu vou receber as mensagens e trat??-las
        Object resource = receivedV2xMessage.getMessage();

        // ###################### Mensagens de clustering ######################
        // Recebendo um convite (CONV) e tratando
        if (resource instanceof InvitationMsg) {
            msgHandler.handleInvitationMsg(receivedV2xMessage);
        }

        // Recebendo uma resposta (RESP) e tratando
        if (resource instanceof AnswerMsg) {
            // Utilizando o limitador de grupo
            // Se o limitador for 0, o l??der sempre adiciona novos membros
            // Caso constr??rio, verifica se o grupo ?? menor que o tamanho do limitador
            if (clusteringStateMachine.isLeader()
                    && IntelligentVehicleWitness.GROUP_LIMITER != 0
                    && groupManager.getGroupSize() >= IntelligentVehicleWitness.GROUP_LIMITER) {
                getLog().infoSimTime(this,
                        "Limitador de grupo! Tamanho atual {}",
                        groupManager.getGroupSize());
            } else {
                msgHandler.handleAnswerMsg(receivedV2xMessage);
            }
        }

        // Recebendo um acknowledgement (ACK) e tratando
        if (resource instanceof AckMsg) {
            msgHandler.handleAckMsg(receivedV2xMessage);
        }

        // Quando um l??der recebe um KeepAlive de um membro
        if (resource instanceof KeepAliveMsg) {
            msgHandler.handleKeepAliveMsg(receivedV2xMessage);
        }

        // Quando um l??der recebe uma mensagem de um membro que passou
        // por um merge e est?? indo para outro grupo. Isto serve para
        // agilizar o processo de transi????o
        if (resource instanceof MergeLeavingMsg) {
            msgHandler.handleMergeLeavingMsg(receivedV2xMessage);
        }
        // ###################### Mensagens de clustering ######################
    }


    // M??todo para o desligamento da unidade
    @Override
    public void onShutdown() {
        // Registrando desligamento do ve??culo
        simLogger.tearDown();
    }


    // M??todo a ser chamado toda vez que um sensor do ve??culo mudou seus dados
    @Override
    public void onVehicleUpdated(@Nullable VehicleData previousVehicleData, @Nonnull VehicleData updatedVehicleData) {
    	// Retirei daqui as transa????es para poder fazer um throughput constante, mas basta
        // descomentar a linha abaixo que volta a funcionar. Vou criar um evento de tempo
        // constante que enviar?? a transa????o para o l??der do consenso.
        if (naturalWorkload) {
            transactionHandler.handleSensorUpdate();
        }
    }







    // M??todo abstrato para realizar a mudan??a de grupo no merge
    // Ser?? implementado nas classes que realizam o merge, aqui ?? s?? um prot??tipo
    public abstract void mergeChangeGroup(InvitationMsg message);





    // Getter do MsgHandler
    public MsgHandler getMsgHandler() {
        return msgHandler;
    }

    // Getter do EventHandler
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    // Getter da StateMachine
    public ClusteringStateMachine getStateMachine() {
        return clusteringStateMachine;
    }

    // Getter do GroupManager para poder gerenciar o grupo do ve??culo
    public GroupManager getGroupManager() {
        return groupManager;
    }

    // Getter do SimLogger para o registro de eventos no log
    public SimLogger getSimLogger() {
        return simLogger;
    }

    // Getter da chave p??blica do ve??culo
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    // Descriptografa uma mensagem
    public String decrypt(String encrypted) {
        return crypto.decrypt(encrypted, keyPair.getPrivate());
    }

    // Assina uma mensagem
    public String sign(String plain) {
        return crypto.sign(plain, keyPair.getPrivate());
    }

    // L?? os par??metros do arquivo
    private void read_params() {
        JSONParser parser = new JSONParser();
        try {
            JSONObject params = (JSONObject) parser.parse(new FileReader(PARAM_FILE));
            naturalWorkload = (boolean) params.get(NATURAL_NAME);
            transactionUnit = (long) params.get(UNIT_NAME);
            transactionInterval = (long) params.get(INTERVAL_NAME);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }



    // ######################## Outros M??todos ##################################
    @Override
    public void onAcknowledgementReceived(ReceivedAcknowledgement receivedAcknowledgement) {

    }

    @Override
    public void onCamBuilding(CamBuilder camBuilder) {

    }

    @Override
    public void onMessageTransmitted(V2xMessageTransmission v2xMessageTransmission) {

    }
    // ######################## Outros M??todos ##################################
}
