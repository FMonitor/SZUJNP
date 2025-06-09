package client.com.lcmonitor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HttpClient {
    private String host;
    private int port;
    private Map<String, String> cookies;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.cookies = new HashMap<>();
    }

    public HttpResponse sendGet(String path) throws IOException {
        return sendRequest("GET", path, null);
    }

    public HttpResponse sendHead(String path) throws IOException {
        return sendRequest("HEAD", path, null);
    }

    public HttpResponse sendPost(String path, String data) throws IOException {
        return sendRequest("POST", path, data);
    }

    private HttpResponse sendRequest(String method, String path, String data) throws IOException {
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        InputStream inputStream = socket.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        // 发送请求行
        out.print(method + " " + path + " HTTP/1.1\r\n");
        out.print("Host: " + host + ":" + port + "\r\n");
        out.print("User-Agent: JavaHttpClient/1.0\r\n");
        out.print("Accept: */*\r\n");
        
        // 发送Cookie
        if (!cookies.isEmpty()) {
            StringBuilder cookieHeader = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                if (cookieHeader.length() > 0) cookieHeader.append("; ");
                cookieHeader.append(entry.getKey()).append("=").append(entry.getValue());
            }
            out.print("Cookie: " + cookieHeader.toString() + "\r\n");
        }

        // POST请求发送数据
        if ("POST".equals(method) && data != null) {
            out.print("Content-Type: application/x-www-form-urlencoded\r\n");
            out.print("Content-Length: " + data.length() + "\r\n");
            out.print("\r\n");
            out.print(data);
        } else {
            out.print("\r\n");
        }
        out.flush();

        // 读取响应
        String statusLine = in.readLine();
        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] parts = line.split(": ", 2);
            if (parts.length == 2) {
                headers.put(parts[0].toLowerCase(), parts[1]);
                
                // 处理Set-Cookie
                if ("set-cookie".equals(parts[0].toLowerCase())) {
                    parseCookie(parts[1]);
                }
            }
        }

        // 读取响应体
        String body = "";
        String contentType = headers.getOrDefault("content-type", "");
        
        if (!"HEAD".equals(method)) {
            if (contentType.startsWith("text/") || contentType.contains("json") || contentType.contains("html")) {
                // 文本内容
                StringBuilder bodyBuilder = new StringBuilder();
                char[] buffer = new char[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    bodyBuilder.append(buffer, 0, bytesRead);
                }
                body = bodyBuilder.toString();
            } else if (contentType.startsWith("image/")) {
                // 图片内容，只记录大小
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                body = "[图片数据, 大小: " + baos.size() + " 字节]";
            } else {
                // 其他二进制内容
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                body = "[二进制数据, 大小: " + baos.size() + " 字节]";
            }
        }

        socket.close();
        return new HttpResponse(statusLine, headers, body);
    }

    private void parseCookie(String setCookieHeader) {
        String[] parts = setCookieHeader.split(";");
        if (parts.length > 0) {
            String[] cookieParts = parts[0].split("=", 2);
            if (cookieParts.length == 2) {
                cookies.put(cookieParts[0], cookieParts[1]);
            }
        }
    }

    // ========== 功能测试方法 ==========
    
    public void testFileTypes() {
        System.out.println("\n=== 文件类型测试 ===");
        
        String[] testFiles = {
            "/",                    // HTML页面
            "/index.html",          // HTML文件
            "/test.css",           // CSS文件
            "/test.js",            // JavaScript文件
            "/images/test.png"     // 图片文件
        };
        
        for (String file : testFiles) {
            try {
                System.out.println("\n测试文件: " + file);
                HttpResponse response = sendGet(file);
                
                String[] statusParts = response.getStatusLine().split(" ");
                String statusCode = statusParts.length > 1 ? statusParts[1] : "未知";
                String contentType = response.getHeaders().getOrDefault("content-type", "未知");
                
                System.out.println("  状态码: " + statusCode);
                System.out.println("  内容类型: " + contentType);
                System.out.println("  响应大小: " + response.getBody().length() + " 字符");
                
                if (response.getBody().length() < 200) {
                    System.out.println("  响应内容预览: " + response.getBody().substring(0, 
                        Math.min(100, response.getBody().length())) + "...");
                } else {
                    System.out.println("  响应内容: " + response.getBody().substring(0, 100) + "...");
                }
                
            } catch (IOException e) {
                System.err.println("  请求失败: " + e.getMessage());
            }
        }
    }

    public void testHttpMethods() {
        System.out.println("\n=== HTTP方法测试 ===");
        
        try {
            // GET测试
            System.out.println("\n1. GET 请求测试:");
            HttpResponse getResponse = sendGet("/api/test");
            System.out.println("   状态: " + getResponse.getStatusLine());
            System.out.println("   内容: " + getResponse.getBody().substring(0, 
                Math.min(100, getResponse.getBody().length())));
            
            // HEAD测试
            System.out.println("\n2. HEAD 请求测试:");
            HttpResponse headResponse = sendHead("/api/test");
            System.out.println("   状态: " + headResponse.getStatusLine());
            System.out.println("   Content-Type: " + headResponse.getHeaders().get("content-type"));
            
            // POST测试
            System.out.println("\n3. POST 请求测试:");
            HttpResponse postResponse = sendPost("/", "username=testuser&message=hello world");
            System.out.println("   状态: " + postResponse.getStatusLine());
            System.out.println("   内容: " + postResponse.getBody().substring(0, 
                Math.min(200, postResponse.getBody().length())));
            
        } catch (IOException e) {
            System.err.println("HTTP方法测试失败: " + e.getMessage());
        }
    }

    public void testCookieSession() {
        System.out.println("\n=== Cookie会话测试 ===");
        
        try {
            // 第一次请求，获取Cookie
            System.out.println("1. 第一次访问，获取会话Cookie:");
            HttpResponse firstResponse = sendGet("/");
            System.out.println("   Cookie数量: " + cookies.size());
            if (!cookies.isEmpty()) {
                System.out.println("   Session ID: " + cookies.get("SESSIONID"));
                System.out.println("   Visitor ID: " + cookies.get("VISITOR_ID"));
                System.out.println("   Visit Count: " + cookies.get("VISIT_COUNT"));
            }
            
            // 第二次请求，携带Cookie
            System.out.println("\n2. 第二次访问，携带Cookie:");
            HttpResponse secondResponse = sendGet("/");
            System.out.println("   状态: " + secondResponse.getStatusLine());
            System.out.println("   会话保持: " + (cookies.size() > 0 ? "成功" : "失败"));
            if (!cookies.isEmpty()) {
                System.out.println("   更新后访问次数: " + cookies.get("VISIT_COUNT"));
            }
            
        } catch (IOException e) {
            System.err.println("Cookie测试失败: " + e.getMessage());
        }
    }

    // ========== 性能和压力测试方法 ==========
    
    public void performanceTest() throws InterruptedException {
        System.out.println("\n=== Java HTTP服务器性能分析测试 ===");
        System.out.println("开始时间: " + LocalDateTime.now().format(formatter));
        System.out.println("=========================================");
        
        // 1. 基础功能验证
        basicFunctionTest();
        
        // 2. 并发连接测试
        concurrencyTest();
        
        // 3. 文件传输压力测试
        fileTransferTest();
        
        // 4. 长时间稳定性测试
        stabilityTest();
        
        System.out.println("\n=== 性能测试完成 ===");
    }
    
    // 基础功能测试
    private void basicFunctionTest() {
        System.out.println("\n1. 基础功能验证测试");
        System.out.println("------------------------");
        
        try {
            HttpClient client = new HttpClient("localhost", 8080);
            
            // Cookie会话测试
            System.out.println("测试Cookie会话管理...");
            HttpResponse response1 = client.sendGet("/");
            System.out.println("首次访问状态: " + response1.getStatusLine());
            
            Thread.sleep(1000);
            
            HttpResponse response2 = client.sendGet("/");
            System.out.println("二次访问状态: " + response2.getStatusLine());
            
            // API测试
            HttpResponse apiResponse = client.sendGet("/api/test");
            System.out.println("API测试状态: " + apiResponse.getStatusLine());
            System.out.println("API响应预览: " + apiResponse.getBody().substring(0, 
                Math.min(200, apiResponse.getBody().length())));
            
        } catch (Exception e) {
            System.err.println("基础功能测试失败: " + e.getMessage());
        }
    }
    
    // 并发连接测试
    private void concurrencyTest() throws InterruptedException {
        System.out.println("\n2. 并发连接压力测试");
        System.out.println("------------------------");
        
        int[] concurrencyLevels = {10, 25, 50, 100, 150, 200};
        
        for (int concurrency : concurrencyLevels) {
            System.out.println(String.format("\n测试并发数: %d 个客户端", concurrency));
            testConcurrentConnections(concurrency, 3);
            Thread.sleep(3000); // 等待服务器恢复
        }
    }
    
    private void testConcurrentConnections(int numClients, int requestsPerClient) 
            throws InterruptedException {
        
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(numClients);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    HttpClient client = new HttpClient("localhost", 8080);
                    
                    for (int j = 0; j < requestsPerClient; j++) {
                        try {
                            long requestStart = System.currentTimeMillis();
                            HttpResponse response = client.sendGet("/api/test");
                            long requestTime = System.currentTimeMillis() - requestStart;
                            
                            totalResponseTime.addAndGet(requestTime);
                            
                            if (response.getStatusLine().contains("200")) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            if (numClients <= 50) { // 只在低并发时显示详细错误
                                System.err.println("客户端 " + clientId + " 请求失败: " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        int totalRequests = numClients * requestsPerClient;
        double successRate = (successCount.get() * 100.0) / totalRequests;
        double avgResponseTime = successCount.get() > 0 ? 
            (totalResponseTime.get() * 1.0) / successCount.get() : 0;
        
        System.out.println(String.format(
            "  结果: 成功率 %.1f%% (%d/%d), 平均响应时间 %.2fms, QPS %.2f, 总耗时 %dms%s",
            successRate, successCount.get(), totalRequests, avgResponseTime,
            (successCount.get() * 1000.0 / duration), duration,
            completed ? "" : " [超时]"
        ));
        
        if (successRate < 90) {
            System.out.println("  ⚠️  警告: 成功率低于90%，服务器可能过载");
        }
        if (avgResponseTime > 1000) {
            System.out.println("  ⚠️  警告: 平均响应时间超过1秒");
        }
    }
    
    // 文件传输压力测试
    private void fileTransferTest() throws InterruptedException {
        System.out.println("\n3. 文件传输压力测试");
        System.out.println("------------------------");
        
        String[] testFiles = {
            "/index.html",      // HTML文件
            "/test.css",        // CSS文件  
            "/test.js",         // JS文件
            "/images/test.png"  // 图片文件
        };
        
        for (String file : testFiles) {
            System.out.println("\n测试文件: " + file);
            testFileTransfer(file, 50, 2); // 50个并发，每个请求2次
            Thread.sleep(2000);
        }
    }
    
    private void testFileTransfer(String filePath, int numClients, int requestsPerClient) 
            throws InterruptedException {
        
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalBytes = new AtomicLong(0);
        CountDownLatch latch = new CountDownLatch(numClients);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < numClients; i++) {
            executor.submit(() -> {
                try {
                    HttpClient client = new HttpClient("localhost", 8080);
                    
                    for (int j = 0; j < requestsPerClient; j++) {
                        try {
                            HttpResponse response = client.sendGet(filePath);
                            if (response.getStatusLine().contains("200")) {
                                successCount.incrementAndGet();
                                totalBytes.addAndGet(response.getBody().length());
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double mbTransferred = totalBytes.get() / (1024.0 * 1024.0);
        double transferRate = mbTransferred / (duration / 1000.0);
        
        System.out.println(String.format(
            "  成功: %d, 失败: %d, 传输量: %.2f MB, 传输速率: %.2f MB/s",
            successCount.get(), errorCount.get(), mbTransferred, transferRate
        ));
    }
    
    // 长时间稳定性测试
    private void stabilityTest() throws InterruptedException {
        System.out.println("\n4. 长时间稳定性测试 (2分钟)");
        System.out.println("------------------------");
        
        AtomicInteger totalSuccess = new AtomicInteger(0);
        AtomicInteger totalError = new AtomicInteger(0);
        AtomicBoolean running = new AtomicBoolean(true);
        
        // 启动监控线程
        Thread monitorThread = new Thread(() -> {
            int lastSuccess = 0;
            while (running.get()) {
                try {
                    Thread.sleep(10000); // 每10秒报告一次
                    int currentSuccess = totalSuccess.get();
                    int currentError = totalError.get();
                    int qps = (currentSuccess - lastSuccess);
                    System.out.println(String.format(
                        "  [%s] 成功: %d (+%d), 失败: %d, QPS: %.1f",
                        LocalDateTime.now().format(formatter),
                        currentSuccess, (currentSuccess - lastSuccess), currentError, qps / 10.0
                    ));
                    lastSuccess = currentSuccess;
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitorThread.start();
        
        // 启动持续负载
        ExecutorService executor = Executors.newFixedThreadPool(20);
        
        for (int i = 0; i < 20; i++) {
            executor.submit(() -> {
                HttpClient client = new HttpClient("localhost", 8080);
                while (running.get()) {
                    try {
                        HttpResponse response = client.sendGet("/api/stats");
                        if (response.getStatusLine().contains("200")) {
                            totalSuccess.incrementAndGet();
                        } else {
                            totalError.incrementAndGet();
                        }
                        Thread.sleep(100); // 100ms间隔
                    } catch (Exception e) {
                        totalError.incrementAndGet();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            });
        }
        
        // 运行2分钟
        Thread.sleep(120000);
        running.set(false);
        
        executor.shutdown();
        monitorThread.interrupt();
        
        System.out.println(String.format(
            "\n稳定性测试完成 - 总成功: %d, 总失败: %d, 成功率: %.2f%%",
            totalSuccess.get(), totalError.get(),
            (totalSuccess.get() * 100.0) / (totalSuccess.get() + totalError.get())
        ));
    }

    // ========== 主程序入口 ==========
    
    public static void main(String[] args) {
        System.out.println("=== Java HTTP客户端测试程序 ===");
        System.out.println("请选择测试模式:");
        System.out.println("1. 基础功能测试 (默认)");
        System.out.println("2. 性能压力测试");
        System.out.println("3. 完整测试 (功能 + 性能)");
        System.out.println("=====================================");
        
        Scanner scanner = new Scanner(System.in);
        int choice = 1; // 默认选择
        
        try {
            System.out.print("请输入选择 (1-3): ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                choice = Integer.parseInt(input);
            }
        } catch (Exception e) {
            System.out.println("输入无效，使用默认选择: 基础功能测试");
        }
        
        HttpClient client = new HttpClient("localhost", 8080);
        
        try {
            switch (choice) {
                case 1:
                    System.out.println("\n=== 执行基础功能测试 ===");
                    client.testFileTypes();
                    client.testHttpMethods();
                    client.testCookieSession();
                    break;
                    
                case 2:
                    System.out.println("\n=== 执行性能压力测试 ===");
                    client.performanceTest();
                    break;
                    
                case 3:
                    System.out.println("\n=== 执行完整测试 ===");
                    // 先进行基础功能测试
                    client.testFileTypes();
                    client.testHttpMethods();
                    client.testCookieSession();
                    
                    // 等待用户确认
                    System.out.println("\n基础功能测试完成！");
                    System.out.print("按回车键继续进行性能测试...");
                    scanner.nextLine();
                    
                    // 进行性能测试
                    client.performanceTest();
                    break;
                    
                default:
                    System.out.println("无效选择，执行基础功能测试");
                    client.testFileTypes();
                    client.testHttpMethods();
                    client.testCookieSession();
            }
        } catch (Exception e) {
            System.err.println("测试执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
        
        System.out.println("\n=== 所有测试完成！===");
    }
}