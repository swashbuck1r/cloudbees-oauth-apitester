package org.cloudbees.example;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class OAuthFilter implements Filter {
    private Log log = LogFactory.getLog(OAuthFilter.class);
    private  OAuthService service;
    private static final Token EMPTY_TOKEN = null;
    public static String AUTH_TOKEN_SESSION = "cloudbees.authToken";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Params params = Params.create(filterConfig.getServletContext());
       
        log.debug("oauth.key: " + params.get("oauth.key"));
        log.debug("oauth.secret: " + params.get("oauth.secret").substring(0, 1) + "[...redacted...]");
        log.debug("oauth.callback: " + params.get("oauth.callback"));
        log.debug("oauth.prompt.scopes: " + params.get("oauth.prompt.scopes"));
        
        service = new ServiceBuilder()
        .provider(CloudBeesOAuthDriver.class)
        .apiKey(params.get("oauth.key"))
        .apiSecret(params.get("oauth.secret"))
        .scope(params.get("oauth.prompt.scopes"))
        .callback(params.get("oauth.callback"))
        .debug()
        .build();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        doHttpFilter((HttpServletRequest)req, (HttpServletResponse)resp, chain);
    }
    
    public void doHttpFilter(HttpServletRequest req, HttpServletResponse resp,
            FilterChain chain) throws IOException, ServletException {
        String path = req.getRequestURI();
        if(path != null && path.startsWith("/oauth")) {
            saveOAuthAccessToken(req);
            String url = req.getParameter("return_to");
            if(url == null)
                url = "/";
            resp.sendRedirect(url);
        }
        else {
            Token authToken = (Token)req.getSession().getAttribute(AUTH_TOKEN_SESSION);
            if(authToken == null) {
                login(req, resp);
            }
            else {
                chain.doFilter(req, resp);
            }
        }
    }
    
    private void saveOAuthAccessToken(HttpServletRequest req) {
        String code = req.getParameter("code");
        String state = req.getParameter("state");
        log.debug(String.format("callback [code=%s, state=%s]:", code, state));
        Verifier verifier = new Verifier(code);
        Token accessToken = service.getAccessToken(EMPTY_TOKEN, verifier);
        req.getSession().setAttribute(AUTH_TOKEN_SESSION, accessToken);
        log.debug(String.format("getAccessToken [result=%s]:", accessToken));
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authUrl = service.getAuthorizationUrl(EMPTY_TOKEN);
        log.debug(String.format("login authUrl [authUrl=%s]:", authUrl));
        resp.sendRedirect(authUrl);
    }

    @Override
    public void destroy() {
    }
}
