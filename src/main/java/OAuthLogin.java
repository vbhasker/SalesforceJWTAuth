import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class OAuthLogin {
    private static String INSTANCE_URL = "https://vinodhab-dev-ed.my.salesforce.com";
    private static String OAUTH_ENDPOINT = "/services/oauth2/token";

    public static void main(String[] args) {
        System.out.println("_______________ Login _______________");
        HttpResponse response = null;
        String loginHostUri = INSTANCE_URL + OAUTH_ENDPOINT;

        try {
            CloseableHttpClient httpClient = createAcceptSelfSignedCertificateClient();
            HttpPost httpPost = new HttpPost(loginHostUri);
            StringBuffer requestBodyText = new StringBuffer("grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer");
            requestBodyText.append("&assertion=");
            requestBodyText.append(JwtExample.generateToken());
            System.out.println("Enviado: "+requestBodyText.toString());
            StringEntity requestBody = new StringEntity(requestBodyText.toString());
            requestBody.setContentType("application/x-www-form-urlencoded");
            httpPost.setEntity(requestBody);

            //Make the request and store the result
            response = httpClient.execute(httpPost);

            //Parse the result if we were able to connect.
            if (  response.getStatusLine().getStatusCode() == 200 ) {
                String response_string = EntityUtils.toString(response.getEntity());
                try {
                    JSONObject json = new JSONObject(response_string);
                    System.out.println("JSON returned by response: +\n" + json.toString(1));
                } catch (JSONException je) {
                    je.printStackTrace();
                }
                System.out.println("\nSuccessfully logged in to instance: " + response_string);
            } else {
                System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
                System.out.println(response.getEntity().getContent());
                System.exit(-1);
            }
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private static CloseableHttpClient createAcceptSelfSignedCertificateClient()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        // use the TrustSelfSignedStrategy to allow Self Signed Certificates
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();

        // we can optionally disable hostname verification.
        // if you don't want to further weaken the security, you don't have to include this.
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

        // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
        // and allow all hosts verifier.
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

        // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
        return HttpClients
                .custom()
                .setSSLSocketFactory(connectionFactory)
                .build();
    }
}
