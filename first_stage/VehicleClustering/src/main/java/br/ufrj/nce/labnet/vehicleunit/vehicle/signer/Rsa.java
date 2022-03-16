package br.ufrj.nce.labnet.vehicleunit.vehicle.signer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Base64;

public class Rsa implements CryptoModule {

    // Gera um par de chaves completamente novo
    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, new SecureRandom());
            KeyPair pair = generator.generateKeyPair();

            return pair;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo RSA não disponível.");
            e.printStackTrace();
            return null;
        }
    }

    // Extrai o par de chaves de um KeyStore que pode ser gerado por
    //  keytool -genkeypair -alias mykey -storepass s3cr3t -keypass s3cr3t -keyalg RSA -keystore keystore.jks
    // Aqui passa como parâmetro o caminho para o arquivo do KeyStore. No caso da linha acima é o ./keystore.jks
    // O segundo parâmetro é a senha para abrir o keystore
    // O terceiro parâmetro é a chave para utilização das chaves
    public KeyPair getKeyPairFromKeyStore(String path, String secret, String keySecret) throws Exception {
        InputStream ins = Rsa.class.getResourceAsStream(path);

        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        keyStore.load(ins, secret.toCharArray());
        KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(secret.toCharArray());

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(keySecret, keyPassword);

        Certificate cert = keyStore.getCertificate(keySecret);
        PublicKey publicKey = cert.getPublicKey();
        PrivateKey privateKey = privateKeyEntry.getPrivateKey();

        return new KeyPair(publicKey, privateKey);
    }

    // Método para criptografar um texto
    public String encrypt(String plainText, PublicKey publicKey) {
        byte[] cipherText = new byte[0];

        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            cipherText = encryptCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo RSA e SHA256 não disponível");
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
            Cipher decriptCipher = Cipher.getInstance("RSA");
            decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(decriptCipher.doFinal(bytes), StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo RSA e SHA256 não disponível");
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
            System.out.println("Padding inválido");
            e.printStackTrace();
        }

        return null;
    }

    // Método para assinar um texto
    // Aqui eu posso passar somente o texto mesmo pq ele usa o hash SHA-256 para o calculo antes da assinatura
    public String sign(String plainText, PrivateKey privateKey) {
        byte[] signature;

        try {
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(privateKey);
            privateSignature.update(plainText.getBytes(StandardCharsets.UTF_8));
            signature = privateSignature.sign();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo RSA e SHA256 não disponível.");
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
            publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            return publicSignature.verify(signatureBytes);
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Algoritmo RSA e SHA256 não disponível.");
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
