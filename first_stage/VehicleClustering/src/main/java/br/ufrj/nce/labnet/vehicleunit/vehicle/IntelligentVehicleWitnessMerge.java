package br.ufrj.nce.labnet.vehicleunit.vehicle;

import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages.*;
import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.ClusteringStateMachine;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;


@SuppressWarnings("unused")
public class IntelligentVehicleWitnessMerge extends IntelligentVehicleWitness implements VehicleApplication, CommunicationApplication {

    // Método para tratar a mudança de grupo quando ocorrer um merge
    @Override
    public void mergeChangeGroup(InvitationMsg message) {

        // O convite é de um líder estrangeiro, preciso comparar o CIG e me manter no grupo maior
        if (groupManager.getQuorum().size() < message.getGroupSize()) {
            // Avisando o antigo líder que ele vai sair para agilizar o processo de sua saída
            msgHandler.sendAdHocMessage(groupManager.getMyLeader().getAddress(), MessageType.MERGE_LEAVING, message.getGroupId());

            // Passando ao estado de ASP para receber o ACK e se tornar membro do outro grupo
            clusteringStateMachine.setState(ClusteringStateMachine.ASP);

            // Enviando resposta do convite do líder estrangeiro
            getLog().infoSimTime(this, "MERGE: Recebido CONV de grupo maior, enviando RESP e aguardando ACK");
            getLog().infoSimTime(this, "Tamanho do meu grupo (quorum): {}", groupManager.getQuorum().size());
            getLog().infoSimTime(this, "CONV: {}", message.toString());
            msgHandler.sendAdHocMessage(message.getRouting().getSource(), MessageType.ANSWER, null);

            // Modifica a cor do carro
            getOperatingSystem().applyVehicleParametersChange(getOperatingSystem().requestVehicleParametersUpdate().changeColor(IntelligentVehicleWitness.ASP_COLOR));

            // Garantindo a reinicialização do processo
            groupManager.setMyLeader(null, null);
            groupManager.setMyMembers(null);
            groupManager.setGroupSize(1);

            // Imprime no log o novo status
            getLog().info("ASP");
        }
    }

}
