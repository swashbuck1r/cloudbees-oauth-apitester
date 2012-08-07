package org.cloudbees.example;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

public class CloudBeesClient {
    private String baseUrl;
    private String oauthToken;

    private CloudBeesClient() {
    }
    
    public String call(String query) throws Exception {
        String url = baseUrl + "?" + query;
        System.out.println(url);
        String result = sendOAuthHttpRequest(new URL(url));
        return result;
    }

    public String listApps() throws Exception {
        String url = baseUrl + "?action=application.list";
        System.out.println(url);
        String result = sendOAuthHttpRequest(new URL(url));
        return result;
    }

    public String sendOAuthHttpRequest(URL url) throws Exception {
        StringWriter sw = new StringWriter();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        //authenticate the request using the OAuth Bearer scheme
        String encodedToken = Base64.encodeBase64String(oauthToken.getBytes()).trim();
        System.out.println(oauthToken);
        
        
        conn.setRequestProperty("Authorization", "Bearer " + encodedToken);

        InputStream input = null;
        if (conn.getResponseCode() < 300) {
            input = conn.getInputStream();
        } else {
            input = conn.getErrorStream();
        }
        try {
            InputStreamReader reader = new InputStreamReader(input);

            char[] chars = new char[1024];
            int numRead = reader.read(chars);
            while (numRead != -1) {
                sw.write(chars, 0, numRead);
                numRead = reader.read(chars);
            }
        } finally {
            conn.disconnect();
            if (input != null)
                input.close();
        }
        return sw.toString();
    }

    public static class Builder {
        private String oauthToken;
        private String baseUrl = "https://api.cloudbees.com/api/";

        public Builder oauthToken(String oauthToken) {
            this.oauthToken = oauthToken;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public CloudBeesClient build() {
            CloudBeesClient api = new CloudBeesClient();
            api.baseUrl = baseUrl;
            api.oauthToken = oauthToken;
            return api;
        }
    }
}
