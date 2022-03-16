package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;


// Classe que representa uma mensagem de acknowledgement (ACK)
public final class MergeLeavingMsg extends V2xMessage {

    // Variáveis próprias da classe V2xMessage que está herdando
	private static final long serialVersionUID = 1L;
    private final EncodedPayload encodedV2xMessage;
    private final static long minLen = 128L;

    // Variáveis do protocolo
    private String srcId;                 // Id do membro origem
    private String dstId;                 // Id do líder destino
    private String mergeGroup;            // Id do grupo que o membro está indo

    // Construtor
    public MergeLeavingMsg(MessageRouting routing, String src, String dst, String mergeGroup) {
        // Variáveis setadas devido a herança
        super(routing);
        encodedV2xMessage = new EncodedPayload(16L, minLen);

        // Variáveis do protocolo
        this.srcId = src;                                             // Id da origem da mensagem
        this.dstId = dst;                                             // Id do destino da mensagem
        this.mergeGroup = mergeGroup;                                 // Grupo que o membro está indo

    }

    // Getter da origem
    public String getSrcId() {
        return srcId;
    }

    // Getter do destino
    public String getDstId() {
        return dstId;
    }

    // Getter do grupo que está realizando o merge
    public String getMergeGroup() {
        return mergeGroup;
    }

    // Método próprio da classe herdada
    @Override
    public EncodedPayload getPayLoad() {
        return encodedV2xMessage;
    }

    // Método para impressão de um MergeLeaving
    @Override
    public String toString() {
        return "MergeLeavingMsg{"
                + "SRC=" + srcId
                + ", DST=" + dstId
                + "GroupMerge=" + mergeGroup + '}';
    }
}
