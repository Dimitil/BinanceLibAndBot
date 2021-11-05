package binance;

import org.apache.commons.codec.binary.Hex;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


class Encryptor {
    final static String algorithm = "HmacSHA256";
    SecretKeySpec secret_key;

    public Encryptor(String key)
    {
        secret_key = new SecretKeySpec(key.getBytes(), algorithm);
    }

    public String getSHA256(String message) {
        String hash = "";
        try {
            Mac sha256_hmac = Mac.getInstance(algorithm);
            sha256_hmac.init(secret_key);
            hash = Hex.encodeHexString(sha256_hmac.doFinal(message.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("noSuchAlgo");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return hash;
    }
}

