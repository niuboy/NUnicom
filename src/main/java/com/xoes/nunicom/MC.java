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
    private final static String URI_NET_LOGIN = "https://int.newland.com.cn/pts/portalLogin/loginPw";
    private final static String URI_NET_LOGOUT = "https://int.newland.com.cn/pts/portalLogin/loginOut";
    private final static String URI_NET_STSTUS = "https://www.baidu.com";

    public static void main(String[] args) {
        if (args == null ||
                !((args.length == 1 && "logout".equalsIgnoreCase(args[0]))
                        || ("login".equalsIgnoreCase(args[0]) && (args.length == 3 || args.length == 6)))) {
            System.out.println("用法 java -jar NUnicom login|logout 用户名 密码 [VPN连接名 VPN用户名 VPN密码]");
            return;
        }
        Result result;
        if ("login".equalsIgnoreCase(args[0]) && getNetStatus() != 200) {
            result = login(args[1], args[2]);
            if (result != null && result.getSuccess() && args.length == 6) {
                executeCMD("rasdial \"" + args[3] + "\" " + args[4] + " " + args[5]);
            }
        } else if ("logout".equalsIgnoreCase(args[0]) && getNetStatus() == 200) {
            result = logout();
        } else {
            System.out.println("操作类型：" + args[0] + "，网络状态：" + getNetStatus());
            return;
        }
        if (result == null) {
            System.out.println("操作失败");
        } else {
            System.out.println("操作结果[" + result.getSuccess() + "]：" + result.getMessage());
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

    private static Result logout() {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Origin", "https://int.newland.com.cn")
                    .header("Referer", "https://int.newland.com.cn/")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", UA)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .uri(new URI(URI_NET_LOGOUT))
                    .GET()
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

    private static Result login(String user, String pass) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("Accept", "application/json, text/javascript, */*; q=0.01")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Origin", "https://int.newland.com.cn")
                    .header("Referer", "https://int.newland.com.cn/")
                    .header("Sec-Fetch-Mode", "cors")
                    .header("Sec-Fetch-Site", "same-origin")
                    .header("User-Agent", UA)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .uri(new URI(URI_NET_LOGIN))
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
