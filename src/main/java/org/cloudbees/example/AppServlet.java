package org.cloudbees.example;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scribe.model.Token;

public class AppServlet extends HttpServlet {
    private Log log = LogFactory.getLog(AppServlet.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Token token = (Token) req.getSession().getAttribute(
                OAuthFilter.AUTH_TOKEN_SESSION);
        String tokenStr = token.getToken();
        String query = req.getParameter("query");

        log.debug("Auth token: " + tokenStr);
        PrintWriter pw = resp.getWriter();
        pw.println("<html><body>");
        pw.println("<h1>CloudBees API call</h1>");
        pw.println("<p><a href='http://wiki.cloudbees.com/bin/view/RUN/API'>Docs</a></p>");

        if (query == null) {
            query = "action=application.list";
            printForm(query, pw);
        } else {
            try {
                printForm(query, pw);
                if (req.getMethod().equalsIgnoreCase("POST")) {
                    printResult(query, tokenStr, pw);
                }

            } catch (Exception e) {
                log.debug(e);
                throw new ServletException(e);
            }
        }

        pw.println("</body></html>");
    }

    private void printResult(String query, String tokenStr, PrintWriter pw)
            throws Exception {
        CloudBeesClient api = new CloudBeesClient.Builder()
                .oauthToken(tokenStr).build();
        String appXML = api.call(query);
        String ecapedXml = StringEscapeUtils.escapeHtml(appXML);
        pw.println("<pre>");
        pw.println(ecapedXml);
        pw.println("</pre>");
    }

    private void printForm(String query, PrintWriter pw) {
        pw.println("<form action='' method='POST'>");
        pw.println("CloudBees API Query String <br />");
        pw.println(String.format(
                "<input name='query' value='%s' style='width: 500px;' />",
                query));
        pw.println("<input type='submit' value='Execute' />");
        pw.println("<form>");
    }
}
