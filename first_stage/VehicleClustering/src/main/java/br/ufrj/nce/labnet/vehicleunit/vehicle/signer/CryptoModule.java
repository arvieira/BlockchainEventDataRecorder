package br.ufrj.nce.labnet.vehicleunit.vehicle.signer;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface CryptoModule {
    KeyPair generateKeyPair();
    String encrypt(String PlainText, PublicKey publicKey);
    String decrypt(String cypherText, PrivateKey privateKey);
    String sign(String plainText, PrivateKey privateKey);
    boolean verify(String plainText, String signature, PublicKey publicKey);
}
