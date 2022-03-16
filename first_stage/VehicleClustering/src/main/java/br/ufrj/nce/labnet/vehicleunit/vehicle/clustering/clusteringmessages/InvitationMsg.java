package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages;

import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.Node;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import java.util.ArrayList;


// Classe que representa uma mensagem de convite (CONV)
public final class InvitationMsg extends V2xMessage {

    // Variáveis próprias da classe V2xMessage que está herdando
	private static final long serialVersionUID = 1L;
    private final EncodedPayload encodedV2xMessage;
    private final static long minLen = 128L;

    // Variáveis do protocolo
    private Node leader;                     // Leader do grupo que está enviando o convite
    private ArrayList<Node> membersList;     // Lista de membros do grupo, representa a LUM
    private final int groupSize;             // Tamanho do grupo, representa o CIG
    private String groupId;                  // Id do grupo ou NULO caso não tenha grupo

    // Construtor
    public InvitationMsg(MessageRouting routing, Node leader, ArrayList<Node> membersList, String groupId) {
        // Variáveis setadas devido a herança
        super(routing);
        encodedV2xMessage = new EncodedPayload(16L, minLen);

        // Variáveis do protocolo
        this.leader = leader;

        // Quando está em SOLE, a lista de membros é null e envia convites
        if (membersList != null) {
            this.groupSize = membersList.size() + 1;                           // CIG, o +1 indica a presença do líder que não está no memberList
            this.membersList = new ArrayList<>(membersList);                   // LUM
        } else {
            this.groupSize = 1;                                                // CIG
            this.membersList = null;                                           // LUM
        }

        // Id do grupo ou null caso não tenha
        this.groupId = groupId;
    }

    // Método para fornecer a lista de membros (LUM)
    public ArrayList<Node> getMembersList() {
        return membersList;
    }

    // Método para fornecer o tamanho do grupo (CIG)
    public int getGroupSize() {
        return groupSize;
    }

    // Método para fornecer o identificador do grupo
    public String getGroupId() {
        return groupId;
    }

    // Método próprio da classe herdada
    @Override
    public EncodedPayload getPayLoad() {
        return encodedV2xMessage;
    }

    // Método para impressão de um convite
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InvitationMsg{");
        sb.append("groupId=").append(groupId);
        sb.append(", groupSize=").append(groupSize);

        // Append do leader na mensagem
        if (leader != null)
            sb.append(", leader=").append(leader.getNodeName());
        else
            sb.append(", leader=").append(leader);

        // Append da lista de membros na mensagem
        if (membersList != null) {
            for (Node temp : membersList) {
                sb.append(", member: ").append(temp.getNodeName());
            }
        }
        sb.append('}');
        return sb.toString();
    }
}
