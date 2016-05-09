package utils.shopyourway;

import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;

import play.Play;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

public class HashGenerator {

    public static String generateHash(String token, String appSecret) {
        if (token == null || appSecret == null) {
            throw new RuntimeException("Neither token nor appSecret can be null !");
        }
        MessageDigest md;
        ArrayList<Byte> temp;
        try {
            String hashAlgorithm = Play.configuration.getProperty("mag.shopyourway.hash.generator.algorithm");
            md = MessageDigest.getInstance(hashAlgorithm);
            md.reset();
            String hashEncoding = Play.configuration.getProperty("mag.shopyourway.hash.generator.encoding");
            temp = Lists.newArrayList();
			temp.addAll(Bytes.asList(token.getBytes(hashEncoding)));
            temp.addAll(Bytes.asList(appSecret.getBytes(hashEncoding)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Hex.encodeHexString(md.digest(Bytes.toArray(temp)));
    }
}
