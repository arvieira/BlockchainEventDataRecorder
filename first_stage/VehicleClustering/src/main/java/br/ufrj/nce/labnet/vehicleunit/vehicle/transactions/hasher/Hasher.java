package br.ufrj.nce.labnet.vehicleunit.vehicle.transactions.hasher;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Classe para calculos de hash
public class Hasher {

    // Método estático para calcular o hash de um objeto serializado como string
    public static String calculateHash(String content) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Não foi encontrado o algoritmo SHA256");
            e.printStackTrace();
        }

        byte[] encodedhash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }

    // Método auxiliar para geração do hash
    private static String bytesToHex(byte[] hash) {
        // Transforma bytes para hexadecimal
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
