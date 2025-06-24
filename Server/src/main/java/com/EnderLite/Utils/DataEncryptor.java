package com.EnderLite.Utils;

import java.security.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Class that is responsible for encrypting and decrypting data.
 */
public class DataEncryptor {

    {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Static method responsible for encrypting data.
     * 
     * @param input Data to encode
     * @param key   Key used to encode input.
     */
    public static byte[] encrypt(String input, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, NoSuchProviderException {
        Cipher cipher = Cipher.getInstance("AES", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input.getBytes());
    }

    /**
     * Static method responsible for decrypting data from cipher.
     * 
     * @param cipherText Cipher to decode
     * @param key        Key used to decode input.
     */
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
