package com.EnderLite.Utils;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class DataEncryptor {

    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static byte[] encrypt(String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
        Cipher cipher = Cipher.getInstance("AES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input.getBytes());
    }

    public static String decrypt(byte[] cipherText, SecretKey key)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {

        Cipher cipher = Cipher.getInstance("AES", "BC");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText);
    }

}
