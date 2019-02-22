package com.mateolegi.rostrum;

import com.mateolegi.rostrum.constant.PropertiesConstants;
import com.mateolegi.rostrum.exception.DecryptionException;
import com.mateolegi.rostrum.exception.EncryptionException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Implements AES256 cipher method to secure texts.
 * @author <a href="mateolegi.github.io">Mateo Leal</a>
 * @version 1.0.0
 */
public class AES256 {

    private static String privateKey = Properties.getString(PropertiesConstants.AES256_SECRET_KEY);
    private static String salt = Properties.getString(PropertiesConstants.AES256_SALT);

    /**
     * Encrypt the text with the AES256 encryption method.
     * @param original text to be encrypted
     * @return encrypted text
     */
    public static String encrypt(String original) {
        try {
            AlgorithmParameterSpec ivspec = getAlgorithmParameterSpec();
            Key secretKey = getKey();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(original.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new EncryptionException("Error while encrypting: ", e);
        }
    }


    /**
     * Decrypt the text with the AES256 decryption method.
     * @param encryptedText text encrypted previously
     * @return original text
     */
    @NotNull
    @Contract("_ -> new")
    public static String decrypt(String encryptedText) {
        try {
            AlgorithmParameterSpec ivspec = getAlgorithmParameterSpec();
            Key secretKey = getKey();
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
        } catch (Exception e) {
            throw new DecryptionException("Error while decrypting", e);
        }
    }

    @NotNull
    @Contract(" -> new")
    private static AlgorithmParameterSpec getAlgorithmParameterSpec() {
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        return new IvParameterSpec(iv);
    }

    @NotNull
    private static Key getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(privateKey.toCharArray(), salt.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}
