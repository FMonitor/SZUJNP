package com.lcmonitor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLogger {
    private static final String LOG_FILE = "server.log"; // 日志文件路径
    private static final Object lock = new Object(); // 用于线程同步

    // 记录日志
    public static void log(String message) {
        synchronized (lock) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
                String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                writer.write("[" + timestamp + "] " + message);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("日志记录失败：" + e.getMessage());
            }
        }
    }
}