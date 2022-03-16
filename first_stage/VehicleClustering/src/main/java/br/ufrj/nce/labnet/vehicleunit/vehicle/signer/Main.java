package br.ufrj.nce.labnet.vehicleunit.vehicle.signer;

import java.security.KeyPair;
import java.sql.Timestamp;

public class Main {
    private static final String RSA = "RSA";
    private static final String EC = "EC";

    public static void main(String[] args) {
        example();
        tries(500);
    }

    static void tries(int number) {
        System.out.println("\nTeste RSA: " + testCrypto(RSA, number));
        System.out.println("\nTeste EC: " + testCrypto(EC, number));
    }

    static void example() {
        run(new Rsa(), true);
        run(new EC(), true);
    }

    static long testCrypto(String type, int number) {
        CryptoModule crypto;

        switch (type) {
            case RSA:
                crypto = new Rsa();
                break;
            case EC:
                crypto = new EC();
                break;
            default:
                System.out.println("Erro. Crypto não encontrada.");
                return 0;
        }

        int count = 0;
        long acumulator = 0;

        while (count < number) {
            acumulator += run(crypto, false);

            count++;
        }

        return (acumulator/number);
    }

    static long run(CryptoModule crypto, boolean debug) {
        try {
            long inicio;
            long fim;

            inicio = System.currentTimeMillis();
            if (debug)
                System.out.println("Início: " + new Timestamp(inicio));

            // Gera o pares de chaves
            KeyPair pair = crypto.generateKeyPair();
            KeyPair pair2 = crypto.generateKeyPair();

            // Mensagem
            String msg = "Minha mensagem secreta";
            if (debug)
                System.out.println("\nMensagem: " + msg + "\n");

            // Mensagem criptografada
            String cipher = crypto.encrypt(msg, pair.getPublic());
            if (debug)
                System.out.println("Cifrada: " + cipher + "\n");

            // Mensagem decifrada
            String deciphered = crypto.decrypt(cipher, pair.getPrivate());
            if (debug)
                System.out.println("Decifrada: " + deciphered + "\n");

            // Assinatura
            String sign = crypto.sign(msg, pair.getPrivate());
            if (debug)
                System.out.println("Assinatura: " + sign + "\n");

            // Verificação de assinatura
            boolean one = crypto.verify(msg, sign, pair.getPublic());
            boolean two = crypto.verify(msg, sign, pair2.getPublic());
            if (debug) {
                System.out.println("Verificação: " + one);
                System.out.println("Verificação 2: " + two);
            }

            fim = System.currentTimeMillis();
            if (debug) {
                System.out.println("\nFim: " + new Timestamp(fim));
                System.out.println("\nDuração: " + (fim-inicio) + "\n\n\n");
            }

            if (!debug)
                System.out.print(".");

            return (fim-inicio);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}


