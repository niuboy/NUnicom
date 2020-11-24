package com.xoes.nunicom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUri {

    private String uri;

    private String characteristic;

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckUri.class);

    private String scheme;
    private String host;
    private Integer port;

    public CheckUri(String uri, String characteristic) {
        this.uri = uri;
        this.characteristic = characteristic;
        analysis();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    private void analysis(){
        Pattern pattern = Pattern.compile("(\\w+):\\/\\/([^/:]+)(:\\d*)?([^# ]*)");
        Matcher matcher = pattern.matcher(uri);
        matcher.find();
        this.scheme = matcher.group(1);
        this.host = matcher.group(2);
        if (matcher.group(3) != null){
            this.port = Integer.parseInt(matcher.group(3).substring(1));
        }
    }

    public boolean netIsReachable() {
        try {
            InetAddress inetAddress = InetAddress.getByName(this.host);
            return inetAddress.isReachable(5000);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean schemeIsReachable() {
        if (scheme.equals("ping")){
            return netIsReachable();
        }
        if (!netIsReachable()) {
            return false;
        }
        if (scheme.startsWith("http")) {
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .connectTimeout(Duration.ofSeconds(30))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .header("User-Agent", Constants.UA)
                        .uri(new URI(uri))
                        .GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.body().contains(characteristic)){
                    return true;
                }
            } catch (URISyntaxException | IOException | InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }
}
