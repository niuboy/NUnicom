package com.xoes.nunicom;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class MC {

    private static final Logger logger = LogFactory.getLogger();
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36";

    private static final int NL_AUTH_LOGIN = 1;
    private static final int NL_AUTH_LOGOUT = 1 << 1;
    private static final int VPN_CONNECT = 1 << 2;
    private static final int VPN_DISCONNECT = 1 << 3;

    private static final String URI_NET_LOGIN = "https://int.newland.com.cn/pts/portalLogin/loginPw";
    private static final String URI_NET_LOGOUT = "https://int.newland.com.cn/pts/portalLogin/loginOut";
    private static final String URI_NET_STATUS = "https://www.baidu.com";
    private static final String URI_VPN_STATUS = "https://www.taobao.com";

    public static void main(String[] args) {
        if (args == null || "help".equalsIgnoreCase(args[0]) || "-help".equalsIgnoreCase(args[0]) || "-h".equalsIgnoreCase(args[0])) {
            logger.info("java -jar 文件名.jar 参数");
            logger.info("参数说明 可多项");
            logger.info(NL_AUTH_LOGIN + "\t新大陆网络认证登录操作 后接参数 用户名 密码");
            logger.info(NL_AUTH_LOGOUT + "\t新大陆网络认证注销操作");
            logger.info(VPN_CONNECT + "\tVPN拨号 后接参数 VPN连接名 VPN用户名 VPN密码 [电话簿名称]");
            logger.info(VPN_DISCONNECT + "\tVPN断开 后接参数 [VPN连接名]");
            return;
        }
        int command = getCommand(args);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logger.info("操作类型：" + command);
                logger.info("操作前网络状态：" + getNetStatus(URI_NET_STATUS));
                logger.info("操作前VPN状态：" + getNetStatus(URI_VPN_STATUS));
                if ((NL_AUTH_LOGIN & command) == NL_AUTH_LOGIN && getNetStatus(URI_NET_STATUS) != 200) {
                    Result result = login(args[1], args[2]);
                    resultPrint(result);
                }
                if ((NL_AUTH_LOGOUT & command) == NL_AUTH_LOGOUT && getNetStatus(URI_NET_STATUS) == 200) {
                    Result result = logout();
                    resultPrint(result);
                    timer.cancel();
                }
                if ((VPN_CONNECT & command) == VPN_CONNECT && getNetStatus(URI_NET_STATUS) == 200 && getNetStatus(URI_VPN_STATUS) != 200) {
                    if (args.length < 6)
                        dialVpn(args[1], args[2], args[3], args.length == 5 ? args[4] : null);
                    else
                        dialVpn(args[3], args[4], args[5], args.length == 7 ? args[6] : null);
                }
                if ((VPN_DISCONNECT & command) == VPN_DISCONNECT && getNetStatus(URI_VPN_STATUS) == 200) {
                    disconnectVpn(args.length == 1 ? null : args[1]);
                    timer.cancel();
                }
                logger.info("操作后网络状态：" + getNetStatus(URI_NET_STATUS));
                logger.info("操作后VPN状态：" + getNetStatus(URI_VPN_STATUS));
            }
        }, 0, 5 * 60 * 1000);
    }

    private static int getCommand(String[] args) {
        try {
            return Integer.parseInt(args[0]);
        } catch (Exception ignored) {
        }
        return -1;
    }

    private static void resultPrint(Result result) {
        if (result == null) {
            logger.info("操作失败");
        } else {
            logger.info("操作结果[" + result.getSuccess() + "]：" + result.getMessage());
        }
    }

    private static void dialVpn(String entryname, String username, String password, String phonebookfile) {
        if (phonebookfile == null || phonebookfile.isBlank())
            phonebookfile = "rasphone.pbk";
        int status = executeCMD("rasdial " + entryname + " " + username + " " + password + " /PHONEBOOK:" + phonebookfile);
        logger.info(status == 0 ? "VPN连接成功" : "VPN连接失败");
    }

    private static void disconnectVpn(String entryname) {
        if (entryname == null)
            entryname = "";
        int status = executeCMD("rasdial " + entryname + " /DISCONNECT");
        logger.info(status == 0 ? "VPN断开成功" : "VPN断开失败");
    }

    private static int getNetStatus(String url) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        int status = -1;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header("User-Agent", UA)
                    .uri(new URI(url))
                    .GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            status = response.statusCode();
        } catch (URISyntaxException e) {
            logger.severe(e.getMessage());
        } catch (IOException e) {
            logger.severe(e.getMessage());
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
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
            logger.severe(e.getMessage());
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
        } catch (URISyntaxException e) {
            logger.severe(e.getMessage());
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
            logger.severe(e.getMessage());
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
        } catch (URISyntaxException e) {
            logger.severe(e.getMessage());
        }
        return null;
    }

    public static int executeCMD(String cmdStr) {
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(cmdStr);
            InputStream in = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = br.readLine();
            while (line != null) {
                logger.info(line);
                line = br.readLine();
            }
            in.close();
            return process.waitFor();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return -1;
    }
}
