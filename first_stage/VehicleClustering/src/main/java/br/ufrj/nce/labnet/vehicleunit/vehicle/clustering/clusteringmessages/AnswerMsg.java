package br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages;

import org.eclipse.mosaic.lib.objects.v2x.EncodedPayload;
import org.eclipse.mosaic.lib.objects.v2x.MessageRouting;
import org.eclipse.mosaic.lib.objects.v2x.V2xMessage;

import java.security.PublicKey;


// Classe que representa uma mensagem de reposta (RESP)
public final class AnswerMsg extends V2xMessage {

    // Variáveis próprias da classe V2xMessage que está herdando
	private static final long serialVersionUID = 1L;
    private final EncodedPayload encodedV2xMessage;
    private final static long minLen = 128L;

    // Variáveis do protocolo
    private String srcId;
    private String dstId;

    // Chave pública do veículo que se tornará membro
    private PublicKey publicKey;

    // Construtor
    public AnswerMsg(MessageRouting routing, String src, String dst, PublicKey publicKey) {
        // Variáveis setadas devido a herança
        super(routing);
        encodedV2xMessage = new EncodedPayload(16L, minLen);

        // Variáveis do protocolo
        this.srcId = src;
        this.dstId = dst;

        // Inserindo a chave pública na mensagem
        this.publicKey = publicKey;
    }

    // Getter da chave pública do veículo origem
    public PublicKey getPublicKey() {
        return publicKey;
    }

    // Método próprio da classe herdada
    @Override
    public EncodedPayload getPayLoad() {
        return encodedV2xMessage;
    }

    // Método para impressão de um RESP
    @Override
    public String toString() {
        return "AnswerMsg{" + "SRC=" + srcId + ", DST=" + dstId + '}';
    }
}
