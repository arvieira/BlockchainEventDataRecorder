package br.ufrj.nce.labnet.vehicleunit.vehicle.utils;

import org.eclipse.mosaic.lib.objects.addressing.SourceAddressContainer;

import java.security.PublicKey;

// Classe que representa um nó qualquer, ou seja, é um veículo
public class Node {

	private SourceAddressContainer address;		// Contém o endereço do nó
	private String nodeName;							// Contém o nome do nó
	private boolean alive;						// Indicação se o mesmo está próximo ou não
	private PublicKey publicKey;				// Chave pública do nó
	private boolean chainStatus;


	// Construtor
	public Node(SourceAddressContainer address, boolean alive, PublicKey publicKey) {
		this.address = address;
		this.nodeName = address.getSourceName();
		this.alive = alive;
		this.publicKey = publicKey;
	}

	// Coloca o nó como vivo
	public void setAlive() {
		alive = true;
	}

	// Coloca o nó como morto
    public void unsetAlive() {
		alive = false;
	}

	// Verifica se o nó está vivo
	public boolean isAlive() {
		return alive;
	}

	// Pega o endereço do nó
	public SourceAddressContainer getAddress () {
		return address;
	}

	// Pega o nome de um nó
	public String getNodeName() {
		return nodeName;
	}

	// Pega a chave pública do nó
	public PublicKey getPublicKey() {
		return publicKey;
	}

	// Define a chain como válida
	public void setChain() {
		chainStatus = true;
	}

	// Define a chain como inválida
	public void unsetChain() {
		chainStatus = false;
	}

	// Verifica se a chain do bloco está correta
	public boolean isChainCorrect() {
		return chainStatus;
	}

	// Método para imprimir um nó
	@Override
	public String toString() {
		return "Node{" +
				"address=" + address +
				", alive=" + alive +
				'}';
	}
}
