package com.xoes.nunicom;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MC {

    private final static String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";
    private final static String URI_NET_AUTH = "https://25y.newland.com.cn/pts/portalLogin/loginPw";
    private final static String URI_NET_STSTUS = "https://www.baidu.com";

    public static void main(String[] args) {
        if (args == null || !(args.length == 2 || args.length == 5)) {
            System.out.println("用法 java -jar NUnicom 用户名 密码 [VPN连接名 VPN用户名 VPN密码]");
            return;
        }
        if (getNetStatus() == 200) {
            System.out.println("网络正常");
            return;
        }
        Result result = login(args[0], args[1]);
        if (result == null) {
            System.out.println("重连失败");
            return;
        }
        System.out.println("重连结果[" + result.getSuccess() + "]：" + result.getMessage());
        if (result.getSuccess() && args.length == 5) {
            executeCMD("rasdial \"" + args[2] + "\" " + args[3] + " " + args[4]);
        }
    }

    private static int getNetStatus() {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        int status = -1;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("User-Agent", UA)
                    .uri(new URI(URI_NET_STSTUS))
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode();
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        return status;
    }

    private static Result login(String user, String pass) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Origin", "https://int.newland.com.cn")
                    .header("Referer", "https://int.newland.com.cn/")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("User-Agent", UA)
                    .uri(new URI(URI_NET_AUTH))
                    .POST(HttpRequest.BodyPublishers.ofString("loginid=" + user + "&password=" + pass))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return JSON.parseObject(response.body(), Result.class);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static void executeCMD(String cmdStr) {
        Runtime run = Runtime.getRuntime();
        try {
//			Process process = run.exec("cmd.exe /k start " + cmdStr);
            Process process = run.exec("cmd.exe /c " + cmdStr);
            InputStream in = process.getInputStream();
            while (in.read() != -1) {
                System.out.println(in.read());
            }
            in.close();
            process.waitFor();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
