package com.lcmonitor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static final int PORT = 12345;
    private static final String FILE_DIR = "send";
    private static String FILE_PATH = "";

    public static void main(String[] args) {
        FILE_PATH = System.getProperty("java.class.path");
        FILE_PATH = FILE_PATH.substring(0, FILE_PATH.lastIndexOf(File.separator) + 1);
        System.out.println("文件路径: " + FILE_PATH + FILE_DIR);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("服务器已启动，等待客户端连接...");

            while (true) {
                try (Socket socket = serverSocket.accept();
                        OutputStream out = socket.getOutputStream();
                        DataOutputStream dataOut = new DataOutputStream(out)) {

                    System.out.println("客户端已连接: " + socket.getInetAddress());

                    File directory = new File(FILE_PATH+FILE_DIR);
                    File[] files = directory.listFiles();

                    if (files != null) {
                        // 发送文件数量
                        dataOut.writeInt(files.length);

                        for (File file : files) {
                            if (file.isFile()) {
                                // 发送文件名和长度
                                dataOut.writeUTF(file.getName());
                                dataOut.writeLong(file.length());

                                try (FileInputStream fileInput = new FileInputStream(file)) {
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;

                                    while ((bytesRead = fileInput.read(buffer)) != -1) {
                                        out.write(buffer, 0, bytesRead);
                                    }
                                }
                                System.out.println("文件发送完成: " + file.getName());
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("文件传输错误: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }
}