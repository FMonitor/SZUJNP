package com.lcmonitor;

public class Main {
    public static void main(String[] args) {
        // 默认启动服务器
        HttpServer server = new HttpServer(8080);
        try {
            server.start();
        } catch (Exception e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }
}