package client.com.lcmonitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMain {
    // public static void main(String[] args) {
    // String host = "localhost";
    // int port = 25565;

    // try (Socket socket = new Socket(host, port);
    // BufferedReader reader = new BufferedReader(
    // new InputStreamReader(socket.getInputStream()))) {
    // for (int i = 0; i < 5; i++) {
    // String message = reader.readLine(); // 读取服务端发送的消息
    // System.out.println("收到服务端消息：" + message);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();

    // }
    // }

    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 25565;
        int clientCount = 5; // 模拟的客户端数量
        ExecutorService threadPool = Executors.newFixedThreadPool(clientCount);
        System.out.println("开始测试，模拟 " + clientCount + " 个客户端并发连接到服务器...");
        java.util.concurrent.atomic.AtomicInteger sum = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger cnt = new java.util.concurrent.atomic.AtomicInteger(0);
        for (int i = 1; i <= clientCount; i++) {
            int clientId = i;
            threadPool.execute(() -> {
                try (Socket socket = new Socket(serverHost, serverPort);
                        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    long startTime = System.currentTimeMillis(); // 记录开始时间
                    writer.println("客户端 " + clientId + " 的请求");
                    System.out.println("客户端 " + clientId + " 已发送请求。");
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println("客户端 " + clientId + " 收到响应：" + response);
                    }
                    long endTime = System.currentTimeMillis(); // 记录结束时间
                    System.out.println("客户端 " + clientId + " 完成，耗时：" + (endTime - startTime) + " 毫秒");
                    sum.addAndGet((int) (endTime - startTime));
                    cnt.addAndGet((int) 1);
                } catch (Exception e) {
                    System.err.println("客户端 " + clientId + " 发生错误：" + e.getMessage());
                }
                if(cnt.get()==20)
                    System.out.println("平均响应时间" + sum.get() / clientCount);
            });
        }
        threadPool.shutdown();
    }
}