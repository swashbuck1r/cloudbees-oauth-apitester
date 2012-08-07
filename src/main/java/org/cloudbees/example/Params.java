package org.cloudbees.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

/**
 * Utility for resolving parameters using ServletAPI, System Properties or
 * Environment Variables. ServletContext values matching ${VAR_NAME} will
 * resolve from System.properties or Environment variables.
 */
public class Params {
    private static Pattern pattern = Pattern
            .compile("\\$\\{[a-z,A-Z,.,0-9]*\\}");
    private ServletContext sc;

    private Params() {
    }

    public static Params create() {
        return new Params();
    }
    
    public static Params create(ServletContext sc) {
        Params params = new Params();
        params.sc = sc;
        return params;
    }

    public boolean optBool(String pname, boolean def) {
        String val = get(pname, new Boolean(def).toString());
        return Boolean.parseBoolean(val);
    }

    public String get(String pname) {
        String val = get(pname, null);

        if (val == null) {
            throw new IllegalStateException("Missing required context param: "
                    + pname);
        }
        return val;
    }

    public String get(String pname, String def) {
        String val = null;
        if (sc != null) {
            val = sc.getInitParameter(pname);
            if (val != null)
                val = replaceVars(val);
        }
        if (val == null) {
            val = sysOrEnvParam(pname, def);
        }
        return val;
    }

    private String sysOrEnvParam(String pname, String def) {
        String val = sysParam(pname);
        if (val == null) {
            val = envParam(pname);
        }
        return val;
    }
    
    private String sysParam(String pname) {
        return System.getProperty(pname);
    }

    private String envParam(String pname) {
        String val = System.getenv(pname);
        if (val == null) {
            String envName = pname.replace('.', '_').toUpperCase();
            val = System.getenv(envName);
        }
        return val;
    }

    private String replaceVars(String input) {
        Matcher m = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String text = m.group(0);
            String varName = text.substring(2, text.length() - 1);
            String replacement = sysOrEnvParam(varName, text);
            if (replacement != null) {
                m.appendReplacement(sb, replacement);
            }
        }
        m.appendTail(sb);

        String result = sb.toString();
        return result;
    }
}