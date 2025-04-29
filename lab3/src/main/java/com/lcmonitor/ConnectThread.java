package com.lcmonitor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectThread implements Runnable {
    private Socket clientSocket;
    private int threadId;
    public ConnectThread(Socket clientSocket, int threadId) {
        this.clientSocket = clientSocket;
        this.threadId = threadId;
    }
    @Override
    public void run() {
        System.out.println("线程 " + threadId + " 正在处理客户端连接...");
        try (OutputStream output = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)) {
            for (int i = 1; i <= 5; i++) {
                writer.println("线程编号：" + threadId + "，发送次数：" + i);
                System.out.println("线程 " + threadId + " 已发送第 " + i + " 次消息。");
                ServerLogger.log("线程 " + threadId + " 已发送第 " + i + " 次消息。");
                Thread.sleep(1000); // 每次发送后等待 1 秒
            }
        } catch (Exception e) {
            System.err.println("线程 " + threadId + " 处理客户端时发生错误：" + e.getMessage());
            ServerLogger.log("线程 " + threadId + " 处理客户端时发生错误：" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("线程 " + threadId + " 已关闭客户端连接。");
                ServerLogger.log("线程 " + threadId + " 已关闭客户端连接。");
            } catch (Exception e) {
                System.err.println("线程 " + threadId + " 关闭连接时发生错误：" + e.getMessage());
                ServerLogger.log("线程 " + threadId + " 关闭连接时发生错误：" + e.getMessage());
            }
        }
    }
}