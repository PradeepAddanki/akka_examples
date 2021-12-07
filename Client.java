import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
private static final Pattern pat = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
private static final String clientId = "";//clientId
private static final String clientSecret = "";//client secret
private static final String tokenUrl = "https://api.byu.edu/token";
private static final String auth = clientId + ":" + clientSecret;
private static final String authentication = Base64.getEncoder().encodeToString(auth.getBytes());
private static String getClientCredentials() {
    String content = "grant_type=client_credentials";
    BufferedReader reader = null;
    HttpsURLConnection connection = null;
    String returnValue = "";
    try {
        URL url = new URL(tokenUrl);
        connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Basic " + authentication);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        PrintStream os = new PrintStream(connection.getOutputStream());
        os.print(content);
        os.close();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = null;
        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        String response = out.toString();
        Matcher matcher = pat.matcher(response);
        if (matcher.matches() && matcher.groupCount() > 0) {
            returnValue = matcher.group(1);
        }
    } catch (Exception e) {
        System.out.println("Error : " + e.getMessage());
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
        connection.disconnect();
    }
    return returnValue;
}
private static void useBearerToken(String bearerToken) {
    BufferedReader reader = null;
    try {
        URL url = new URL("https://api.byu.edu:443/echo/v1/status?test=testing");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = null;
        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        String response = out.toString();
        System.out.println(response);
    } catch (Exception e) {

    }
}
