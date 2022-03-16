package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering;

import br.ufrj.nce.labnet.vehicleunit.vehicle.IntelligentVehicleWitness;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.LogEvent;
import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.Node;
import org.eclipse.mosaic.fed.application.ambassador.util.UnitLogger;


import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GroupManager {

    public static final int MAX_TOKEN = Integer.MAX_VALUE;    // Valor máximo do token que é um número aleatório

    // Controle de membros do grupo que o veículo pertence
    private int groupId;                                                    // ID do atual grupo do veículo
    private String completeGroupId;                                         // ID completo do grupo atual
    private Node myLeader = null;                                           // Ponteiro para o líder do grupo. Será null se não tem ou se ele mesmo é o líder
    private ArrayList<Node> myMembers = null;                               // Ponteiros para os outros membros do grupo, não inclui o líder
    private int groupSize;                                                  // Quantidade de integrantes no grupo
    private Color myGroupColor;                                             // Cor do grupo que o veículo faz parte

    // Controles do quorum do blockchain
    private ArrayList<Node> quorum;                                         // Contém todos os veículos que compõem a rede Blockchain (Líder + Membros)
    private ArrayList<Node> membersSnapshot;                                // Mantém um snapshot dos membros do grupo durante uma rodada do polleamento
    private ArrayList<Node> membersHistory;                                 // Guarda um histórico de todos os membros que já foram do grupo para rotacionar o token
    private Node tokenOwner;                                                // Veículo que atualmente está com o token
    private int currentToken;                                               // Valor do token atual


    // Construtor
    public GroupManager(int groupId, int groupSize, Node currentVehicle) {
        this.groupId = groupId;
        this.groupSize = groupSize;

        this.quorum = new ArrayList<>();
        this.quorum.add(currentVehicle);

        this.membersHistory = new ArrayList<>();
    }

    // Setter do líder do grupo
    public void setMyLeader(Node myLeader, String completeGroupId) {
        this.myLeader = myLeader;
        this.completeGroupId = completeGroupId;
//        if(myLeader != null)
//            this.completeGroupId = myLeader.getAddress().getSourceName() + "_" + groupId;
//        else
//            this.completeGroupId = null;
    }

    // Setter da lista de membros no grupo
    public void setMyMembers(ArrayList<Node> myMembers) {
        // myMembers são somente membros, não inclui o líder
        this.myMembers = myMembers;

        // Quando recebe um convite do líder, um mebro recebe uma cópia de todos
        // os membros do quorum com suas chaves publicas. Aqui eu tenho que atualizar
        // o quorum da minha rede blockchain
        if (myMembers != null && myLeader != null) {
            quorum = new ArrayList<>(myMembers);
            quorum.add(myLeader);
        }
    }

    // Setter do tamanho do grupo
    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }

    // Setter da cor do grupo
    public void setMyGroupColor(Color myGroupColor) {
        this.myGroupColor = myGroupColor;
    }






    // Getter do líder do grupo
    public Node getMyLeader() {
        return myLeader;
    }

    // Getter da lista de membros
    public ArrayList<Node> getMyMembers() {
        return myMembers;
    }

    // Getter do quorum para a rede Blockchain
    public ArrayList<Node> getQuorum() {
        return quorum;
    }

    // Getter do identificador do grupo
    public int getGroupId() {
        return groupId;
    }

    // Getter do identificador completo do grupo
    public String getCompleteGroupId() {
        return completeGroupId;
    }

    // Getter do tamanho do grupo
    public int getGroupSize() {
        return groupSize;
    }

    // Getter da cor do grupo
    public Color getMyGroupColor() {
        return myGroupColor;
    }

    // Getter de um membro específico
    public Node getMember(String name) {
        for (Node member: quorum) {
            if (member.getNodeName().equals(name))
                return member;
        }

        return null;
    }




    // Diminuir o grupo
    public void decreaseGroupSize() {
        groupSize--;
    }

    // Aumentar o grupo
    public void increaseGroupSize() {
        groupSize++;
    }




    // Avança o id dos grupos
    public void increaseGroupId() {
        groupId++;
    }

    // Adiciona um membro ao grupo
    public void addMember(Node node) {
        myMembers.add(node);
        quorum.add(node);
    }

    // Remove membros mortos
    public boolean removeDeadMembers(IntelligentVehicleWitness vehicle) {
        // Variável para controlar se foi removido algum membro
        boolean memberRemoved = false;

        // Iterador para poder remover membros mortos
        Iterator<Node> iterator = myMembers.iterator();
        while (iterator.hasNext()) {                    // While percorre toda a lista de membros
            Node member = iterator.next();              // Membro atualmente sendo avaliado

            // Se o membro não está vivo, ele deve ser removido
            if(!member.isAlive()) {
                // Indicando que pelo menos um membro estava morto
                memberRemoved = true;

                iterator.remove();                          // Removendo o membro da lista de membros
                decreaseGroupSize();                        // Diminuindo a lista de membros
                quorum.remove(member);                      // Removendo o membro do quorum

                vehicle.getLog().infoSimTime(vehicle, "Removendo membro morto: {}", member.getNodeName());
                vehicle.getLog().warnSimTime(
                        vehicle,
                        new LogEvent(
                                LogEvent.PAUSE_PEER,
                                LogEvent.MEMBER + " " + member.getNodeName(),
                                LogEvent.GROUP + " " + completeGroupId).toString()
                );
            }
        }

        return memberRemoved;
    }

    // Método para remover um membro do grupo
    public boolean removeMember(IntelligentVehicleWitness vehicle, String memberName) {
        Iterator<Node> iterator = myMembers.iterator();
        while (iterator.hasNext()) {
            Node member = iterator.next();
            if (member.getNodeName().equals(memberName)) {
                iterator.remove();                          // Removendo o membro da lista de membros
                decreaseGroupSize();                        // Diminuindo a lista de membros
                quorum.remove(member);                      // Removendo o membro do quorum

                vehicle.getLog().infoSimTime(vehicle, "Removendo membro MERGED: {}", member.getNodeName());
                return true;
            }
        }

        return false;
    }

    // Verifica se o grupo morreu e destroy se foi o caso
    public void verifyAndDestroyGroup(IntelligentVehicleWitness vehicle) {
        if (groupSize == 1) {
            // Pegando objetos necessários para o tratamento
            ClusteringStateMachine clusteringStateMachine = vehicle.getStateMachine();
            UnitLogger logger = vehicle.getLog();

            clusteringStateMachine.setState(ClusteringStateMachine.SOLE);       // Coloca a máquina de estados em SOLE novamente
            logger.info("SOLE");                                                // Registra o evento

            // Registra a destruição do grupo
            vehicle.getSimLogger().groupDestroyed(this);

            // Passando ao próximo id de grupo para caso seja criado um novo grupo a seguir
            increaseGroupId();

            // Modifica a cor do LEADER de volta para SOLE
            vehicle.getOperatingSystem()
                    .applyVehicleParametersChange(vehicle.getOperatingSystem()
                            .requestVehicleParametersUpdate()
                            .changeColor(IntelligentVehicleWitness.SOLE_COLOR)
                    );

            // Limpa o ponteiro para o líder
            setMyLeader(null, null);
        }
    }
}
