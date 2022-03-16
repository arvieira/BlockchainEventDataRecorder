package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;


// Classe que representa uma mensagem KeepAlive
public final class KeepAliveMsg extends V2xMessage {

    // Variáveis próprias da classe V2xMessage que está herdando
	private static final long serialVersionUID = 1L;
    private final EncodedPayload encodedV2xMessage;
    private final static long minLen = 128L;

    // Variáveis do protocolo
    private String srcId;
    private String dstId;

    // Construtor
    public KeepAliveMsg(MessageRouting routing, String src, String dst) {
        // Variáveis setadas devido a herança
        super(routing);
        encodedV2xMessage = new EncodedPayload(16L, minLen);

        // Variáveis do protocolo
        this.srcId = src;
        this.dstId = dst;
    }

    // Método próprio da classe herdada
    @Override
    public EncodedPayload getPayLoad() {
        return encodedV2xMessage;
    }

    // Método para impressão de um KeepAlive
    @Override
    public String toString() {
        return "KeepAliveMsg{" + "SRC=" + srcId + ", DST=" + dstId + '}';
    }
}
