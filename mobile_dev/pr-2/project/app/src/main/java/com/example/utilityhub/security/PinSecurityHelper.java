package com.example.utilityhub.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PinSecurityHelper {
    private static final String PREFS = "vault_prefs";
    private static final String KEY_SALT = "pin_salt";
    private static final String KEY_HASH = "pin_hash";
    private static final String KEY_KDF = "pin_kdf";
    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    private PinSecurityHelper() { }

    public static void savePin(Context context, String pin) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String algorithm = getDefaultAlgorithm();
        byte[] hash = hashPin(pin.toCharArray(), salt, algorithm);
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_SALT,
                        Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_HASH,
                        Base64.encodeToString(hash, Base64.NO_WRAP))
                .putString(KEY_KDF, algorithm)
                .apply();
    }

    public static boolean verifyPin(Context context, String pin) throws Exception {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);
        String saltText = prefs.getString(KEY_SALT, null);
        String hashText = prefs.getString(KEY_HASH, null);
        String algorithm = prefs.getString(KEY_KDF, null);
        if (saltText == null || hashText == null || algorithm == null) {
            return false;
        }
        byte[] salt = Base64.decode(saltText, Base64.NO_WRAP);
        byte[] savedHash = Base64.decode(hashText, Base64.NO_WRAP);
        byte[] enteredHash = hashPin(pin.toCharArray(), salt, algorithm);
        return MessageDigest.isEqual(savedHash, enteredHash);
    }

    private static byte[] hashPin(char[] pin, byte[] salt,
                                  String algorithm) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(
                pin, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance(algorithm);
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static String getDefaultAlgorithm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return "PBKDF2WithHmacSHA256";
        }
        return "PBKDF2WithHmacSHA1";
    }
}
