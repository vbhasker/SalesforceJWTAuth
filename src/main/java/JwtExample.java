import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.text.MessageFormat;

public class JwtExample {

    public static String generateToken() {

        String header = "{\"alg\":\"RS256\"}";
        String claimTemplate = "'{'\"iss\": \"{0}\", \"sub\": \"{1}\", \"aud\": \"{2}\", \"exp\": \"{3}\"'}'";

        try {
            StringBuffer token = new StringBuffer();

            //Encode the JWT Header and add it to our string to sign
            token.append(Base64.encodeBase64URLSafeString(header.getBytes(StandardCharsets.UTF_8)));

            //Separate with a period
            token.append(".");

            //Create the JWT Claims Object
            String[] claimArray = new String[4];
            claimArray[0] = "[client_id]";
            claimArray[1] = "[username]";
            claimArray[2] = "https://login.salesforce.com [or] https://test.salesforce.com";
            claimArray[3] = Long.toString((System.currentTimeMillis() / 1000) + 300);
            //claimArray[4]=<JTI>

            MessageFormat claims;
            claims = new MessageFormat(claimTemplate);
            String payload = claims.format(claimArray);

            //Add the encoded claims object
            token.append(Base64.encodeBase64URLSafeString(payload.getBytes(StandardCharsets.UTF_8)));

            //Load the private key from a keystore
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream("/Users/vinodh/Downloads/00D3j0000002Wsi.jks"), "pass123".toCharArray());
            PrivateKey privateKey = (PrivateKey) keystore.getKey("SelfSignedCert_02Nov2020_133431", "pass123".toCharArray());

            //Sign the JWT Header + "." + JWT Claims Object
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(token.toString().getBytes(StandardCharsets.UTF_8));
            String signedPayload = Base64.encodeBase64URLSafeString(signature.sign());

            //Separate with a period
            token.append(".");

            //Add the encoded signature
            token.append(signedPayload);

            return token.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
