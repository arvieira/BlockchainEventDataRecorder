package br.ufrj.nce.labnet.vehicleunit.vehicle.signer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

public class EC implements CryptoModule {

    // O Java por si só não implementa criptografia e decriptografia com curvas elípiticas.
    // Nativamente, ele apenas provê assinatura. Para a cripto e decripto eu preciso usar ECDH
    // que é o uso de curvas elípticas com Diffie-Hellman para troca de uma chave de sessão.
    // Nesse caso, o uso da crypto e decrypto se dá com um algorítmo simétrico como o DES ou o AES.
    // Para resolver esse problema, eu precisei usar uma classe chamada BouncyCastleProvider que
    // provê o ECIES (Elliptic Curve Integrated Ecryption Scheme) que faz a cripto e dedcripto com o EC
    // sem a necessidade de criptografia simétrica.
    // Preciso lembrar de levar o jar do BouncyCastleProvider junto

    // Gera um par de chaves completamente novo
    public KeyPair generateKeyPair() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
            generator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
            KeyPair pair = generator.generateKeyPair();

            return pair;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            System.out.println("Algoritmo EC não disponível.");
            e.printStackTrace();
            return null;
        }
    }

    // Método para criptografar um texto
    public String encrypt(String plainText, PublicKey publicKey) {
        byte[] cipherText = new byte[0];

        try {
            Cipher encryptCipher = Cipher.getInstance("ECIES", BouncyCastleProvider.PROVIDER_NAME);
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = encryptCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Algoritmo ECIES.");
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            System.out.println("Faltou padding");
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            System.out.println("Chave inválida");
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            System.out.println("Tamanho de bloco inválido");
            e.printStackTrace();
            return null;
        } catch (BadPaddingException e) {
            System.out.println("Padding inválido");
            e.printStackTrace();
            return null;
        }

        return Base64.getEncoder().encodeToString(cipherText);
    }

    // Método para decriptografar um texto
    public String decrypt(String cipherText, PrivateKey privateKey) {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        try {
            Cipher decriptCipher = Cipher.getInstance("ECIES", BouncyCastleProvider.PROVIDER_NAME);
            decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.out.println("Algoritmo ECIES.");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            System.out.println("Faltou padding");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            System.out.println("Chave inválida");
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            System.out.println("Tamanho de bloco inválido");
            e.printStackTrace();
        } catch (BadPaddingException e) {
            System.out.println("Padding inválido\n");
            System.out.println("CipherText = " + cipherText);
            System.out.println("PrivateKey = " + privateKey);
            e.printStackTrace();
        }

        return null;
    }

    // Método para assinar um texto
    // Aqui eu posso passar somente o texto mesmo pq ele usa o hash SHA-256 para o calculo antes da assinatura
    public String sign(String plainText, PrivateKey privateKey) {
        byte[] signature;

        try {
            Signature privateSignature = Signature.getInstance("SHA256withECDSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(plainText.getBytes(StandardCharsets.UTF_8));
            signature = privateSignature.sign();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo EC e SHA256 não disponível.");
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            System.out.println("Chave inválida.");
            e.printStackTrace();
            return null;
        } catch (SignatureException e) {
            System.out.println("Problemas durante a assinatura");
            e.printStackTrace();
            return null;
        }

        return Base64.getEncoder().encodeToString(signature);
    }

    // Método para verificar uma assinatura
    public boolean verify(String plainText, String signature, PublicKey publicKey) {
        Signature publicSignature = null;
        try {
            publicSignature = Signature.getInstance("SHA256withECDSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return publicSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo EC e SHA256 não disponível.");
            e.printStackTrace();
            return false;
        } catch (InvalidKeyException e) {
            System.out.println("Chave inválida.");
            e.printStackTrace();
            return false;
        } catch (SignatureException e) {
            System.out.println("Problemas durante a verificação");
            e.printStackTrace();
            return false;
        }
    }
}
