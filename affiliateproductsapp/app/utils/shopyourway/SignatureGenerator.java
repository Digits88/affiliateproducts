package utils.shopyourway;

import java.security.MessageDigest;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Hex;

import play.Play;

import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

public class SignatureGenerator {

    public static String generateSignature(Long userId, Long appId, Long timestamp, String appSecret) {
        if (userId == null || appId == null || timestamp == null || appSecret == null) {
            throw new RuntimeException("Neither userId nor appId nor timestamp nor appSecret can be null !");
        }
        MessageDigest md;
        ArrayList<Byte> temp;
        try {
            String hashAlgorithm = Play.configuration.getProperty("mag.shopyourway.signature.generator.algorithm");
            md = MessageDigest.getInstance(hashAlgorithm);
            md.reset();
            String hashEncoding = Play.configuration.getProperty("mag.shopyourway.signature.generator.encoding");
            temp = Lists.newArrayList();
			temp.addAll(Bytes.asList(userId.toString().getBytes(hashEncoding)));
            temp.addAll(Bytes.asList(appId.toString().getBytes(hashEncoding)));
            temp.addAll(Bytes.asList(timestamp.toString().getBytes(hashEncoding)));
            temp.addAll(Bytes.asList(appSecret.getBytes(hashEncoding)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Hex.encodeHexString(md.digest(Bytes.toArray(temp)));
    }
}
