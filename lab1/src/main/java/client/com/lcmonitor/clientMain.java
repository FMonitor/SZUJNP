package client.com.lcmonitor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class clientMain {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 12345;
    private static final String FILE_DIR = "recv\\";
    private static String FILE_PATH = "";

    public static void main(String[] args) {
        FILE_PATH = System.getProperty("java.class.path");
        FILE_PATH = FILE_PATH.substring(0, FILE_PATH.lastIndexOf(File.separator) + 1);
        System.out.println("文件保存路径: " + FILE_PATH + FILE_DIR);

        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             InputStream in = socket.getInputStream();
             DataInputStream dataIn = new DataInputStream(in)) {

            System.out.println("成功连接到服务器。");

            int fileCount = dataIn.readInt();
            System.out.println("即将接收 " + fileCount + " 个文件。");

            byte[] buffer = new byte[4096];

            // 创建文件夹
            File dir = new File(FILE_PATH + FILE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (int i = 0; i < fileCount; i++) {
                String fileName = dataIn.readUTF();
                long fileSize = dataIn.readLong();

                try (FileOutputStream fileOut = new FileOutputStream(FILE_PATH+FILE_DIR+fileName)) {
                    long remaining = fileSize;
                    int bytesRead;

                    while (remaining > 0 && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                        fileOut.write(buffer, 0, bytesRead);
                        remaining -= bytesRead;
                    }

                    System.out.println("文件接收完成，保存在: " + FILE_PATH + FILE_DIR + fileName);
                }
            }
        } catch (IOException e) {
            System.err.println("文件接收失败: " + e.getMessage());
        }
    }
}
