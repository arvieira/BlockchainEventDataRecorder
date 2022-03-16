package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages;

import br.ufrj.nce.labnet.vehicleunit.vehicle.utils.Node;
import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;


import java.awt.*;
import java.security.PublicKey;
import java.util.ArrayList;


// Classe que representa uma mensagem de acknowledgement (ACK)
public final class AckMsg extends V2xMessage {

    // Variáveis próprias da classe V2xMessage que está herdando
	private static final long serialVersionUID = 1L;
    private final EncodedPayload encodedV2xMessage;
    private final static long minLen = 128L;

    // Variáveis do protocolo
    private String leaderId;                 // Id do líder
    private String memberId;                 // Id do novo membro
    private ArrayList<Node> membersList;     // Lista de membros do grupo, representa a LUM
    private Color groupColor;                // Cor para os membros do grupo do líder
    private String groupId;                  // Id do grupo que o membro fará parte

    // A chave pública de cada membro já está em membersList, pq eu não posso criar um Node sem chave pública
    // Essa variável guarda a chave pública do líder que está enviando para o novo membro
    private PublicKey leaderPublicKey;

    // Construtor
    public AckMsg(MessageRouting routing, String src, String dst, ArrayList<Node> membersList, PublicKey leaderPublicKey, Color color, String groupId) {
        // Variáveis setadas devido a herança
        super(routing);
        encodedV2xMessage = new EncodedPayload(16L, minLen);

        // Variáveis do protocolo
        this.leaderId = src;                                             // Id da origem da mensagem
        this.memberId = dst;                                             // Id do destino da mensagem
        this.groupColor = color;                                         // Cor do grupo definida pelo líder
        if (membersList != null)
            this.membersList = new ArrayList<>(membersList);             // LUM
        else
            this.membersList = null;

        // Colocando a chave pública do líder na mensagem
        this.leaderPublicKey = leaderPublicKey;

        // Id do grupo que o membro fará parte
        this.groupId = groupId;
    }

    // Getter da lista de membros
    public ArrayList<Node> getMembersList() {
        return membersList;
    }

    // Getter da cor do grupo definida pelo líder
    public Color getGroupColor() {
        return groupColor;
    }

    // Getter da chave pública do líder
    public PublicKey getLeaderPublicKey() {
        return leaderPublicKey;
    }

    // Getter do Id do grupo que o membro fará parte
    public String getGroupId() {
        return groupId;
    }

    // Método próprio da classe herdada
    @Override
    public EncodedPayload getPayLoad() {
        return encodedV2xMessage;
    }

    // Método para impressão de um ACK
    @Override
    public String toString() {
        return "AckMsg{" + "SRC=" + leaderId + ", DST=" + memberId + '}';
    }
}
