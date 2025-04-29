package com.lcmonitor;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PoolMain {
    public static void main(String[] args) {
        int port = 25565;
        int maxThreads = 20; // 最大线程数
        System.out.println("服务器启动，等待客户端连接...");
        ServerLogger.log("服务器启动，等待客户端连接...");
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            int threadCount = 0;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadCount++;
                System.out.println("客户端已连接，分配线程编号：" + threadCount);
                ServerLogger.log("客户端已连接，分配线程编号：" + threadCount);

                // 将任务提交到线程池
                ConnectThread connectThread = new ConnectThread(clientSocket, threadCount);
                threadPool.execute(connectThread);
                System.out.println("线程 " + threadCount + " 已提交到线程池，处理客户端请求。");
                ServerLogger.log("线程 " + threadCount + " 已提交到线程池，处理客户端请求。");
            }
        } catch (Exception e) {
            System.err.println("服务器发生错误：" + e.getMessage());
            ServerLogger.log("服务器发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool.shutdown();
            System.out.println("线程池已关闭。");
            ServerLogger.log("线程池已关闭");
        }
    }
}
