package no.kaedeno.detector.utils;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.Scanner;

public class GenerationUtils {

    /**
     * Generates a string with HTML markup and the appropriate JavaScript code
     * to run Modernizr tests on the client.
     *
     * @param httpServletRequest
     *            the http request
     * @return the generated markup
     */
    public static String generateMarkup(HttpServletRequest httpServletRequest, String modernizrUri, String modernizrCookieId, String noscriptParameter) {
        Scanner scanner = getFileScanner(modernizrUri);

        String modernizrScript = scanner.useDelimiter("\\Z").next();
        scanner.close();

        return "<!DOCTYPE html><html><head><meta charset='utf-8'><script type='text/javascript'>"
                + modernizrScript + generateCookieJS(true, modernizrCookieId)
                + "</script></head><body><noscript><meta http-equiv='refresh' content='0; url="
                + generateNoscriptRedirect(httpServletRequest, noscriptParameter) + "'></noscript></body></html>";
    }

    /**
     * Gets a file scanner. Reads from different sources depending on
     * the file being the default file contained in the project or an
     * external, user-defined file.
     *
     * @param uri the file uri.
     *
     * @return a file Scanner.
     */
    private static Scanner getFileScanner(String uri) {
        Scanner scanner;
        try {
            File file = new File(uri);
            scanner = new Scanner(file);
        } catch (FileNotFoundException fileNotFoundException) {
            InputStream is = null;
            try {
                is = new URL("/" + uri).openStream();
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            scanner = new Scanner(is);
        }
        return scanner;
    }

    /**
     * Generates the JavaScript code for reading the Modernizr test result
     * object on the client, and writes the results to a cookie formatted in
     * key-value pairs.
     *
     * Adapted from modernizr-server
     *
     * @param reload
     *            true if the script should reload the page after creating the
     *            cookie, false if not
     * @return the generated code
     */
    public static String generateCookieJS(boolean reload, String modernizrCookieId) {
        String output = "var m=Modernizr,c='';" + "for(var f in m){" + "if(f[0]=='_'){continue;}"
                + "var t=typeof m[f];" + "if(t=='function'){continue;}" + "c+=(c?'|':'"
                + modernizrCookieId + "=')+f+'--';" + "if(t=='object'){"
                + "for(var s in m[f]){" + "c+='/'+s+'--'+(m[f][s]?'1':'0');" + "}" + "}else{"
                + "c+=m[f]?'1':'0';" + "}" + "}" + "c+=';path=/';" + "try{" + "document.cookie=c;";
        if (reload) {
            output += "document.location.reload();";
        }
        output += "}catch(e){}";

        return output;
    }

    /**
     * Generates a noscript redirect url, so that browsers without JavaScript
     * support can be redirected back to the correct page. Adds a GET parameter
     * to the URL so that the plugin can detect the lack of JavaScript support
     *
     * @param httpServletRequest
     *            the http request
     * @return the generated noscript redirect url
     */
    public static String generateNoscriptRedirect(HttpServletRequest httpServletRequest, String noscriptParameter) {
        String url = httpServletRequest.getRequestURL().toString();
        String queryParams = httpServletRequest.getQueryString();
        if (queryParams != null) {
            url += "?" + queryParams + "&" + noscriptParameter + "=true";
        } else {
            url += "?" + noscriptParameter + "=true";
        }
        return url;
    }


    private GenerationUtils() {}
}
