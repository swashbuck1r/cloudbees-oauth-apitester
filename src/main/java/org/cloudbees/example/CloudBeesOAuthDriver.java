package org.cloudbees.example;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;

public class CloudBeesOAuthDriver extends DefaultApi20 {
    private static final String GC_ENDPOINT = "https://grandcentral.cloudbees.com";
    
    @Override
    public String getAccessTokenEndpoint() {
        return GC_ENDPOINT + "/oauth/token?grant_type=authorization_code";
    }
    
    @Override
    public Verb getAccessTokenVerb()
    {
      return Verb.POST;
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        String url = String.format(GC_ENDPOINT + "/oauth/authorize?client_id=%s&response_type=code", config.getApiKey());
        String scopes = config.getScope();
        if(scopes != null) {
            try {
                url += "&scope="+ URLEncoder.encode(scopes,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                url += "&scope="+ URLEncoder.encode(scopes); //lets try with platform encoding!!!
            }
        }
        return url;
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }
}