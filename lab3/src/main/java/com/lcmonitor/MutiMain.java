package com.lcmonitor;

import java.net.ServerSocket;
import java.net.Socket;


public class MutiMain {
    public static void main(String[] args) {
        int port = 25565;
        System.out.println("服务器启动，等待客户端连接...");
        ServerLogger.log("服务器启动，等待客户端连接...");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            int threadCount = 0;
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadCount++;
                System.out.println("客户端已连接，分配线程编号：" + threadCount);
                // 记录日志
                ServerLogger.log("客户端已连接，分配线程编号：" + threadCount);
                ConnectThread connectThread = new ConnectThread(clientSocket, threadCount);
                Thread thread = new Thread(connectThread);
                thread.start();
                System.out.println("线程 " + threadCount + " 已启动，处理客户端请求。");
                ServerLogger.log("线程 " + threadCount + " 已启动，处理客户端请求。");
            }
        } catch (Exception e) {
            ServerLogger.log("服务器发生错误：" + e.getMessage());
            System.err.println("服务器发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }
}

