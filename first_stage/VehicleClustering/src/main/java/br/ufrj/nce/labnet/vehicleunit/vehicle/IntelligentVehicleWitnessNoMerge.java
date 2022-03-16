package br.ufrj.nce.labnet.vehicleunit.vehicle;

import br.ufrj.nce.labnet.vehicleunit.vehicle.clustering.clusteringmessages.InvitationMsg;
import org.eclipse.mosaic.fed.application.app.api.CommunicationApplication;
import org.eclipse.mosaic.fed.application.app.api.VehicleApplication;


@SuppressWarnings("unused")
public class IntelligentVehicleWitnessNoMerge extends IntelligentVehicleWitness implements VehicleApplication, CommunicationApplication {

    // Método para tratar a mudança de grupo quando ocorrer um merge
    @Override
    public void mergeChangeGroup(InvitationMsg message) {
    }

}
