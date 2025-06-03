import java.io.*;
import java.net.*;
import java.util.Scanner;

public class WebDownloader {
    public static boolean downloadFile(String urlString, String fileName) {
        try {
            URL url = new URI(urlString).toURL(); 

            InputStream inputStream = url.openStream();

            FileOutputStream outputStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
            
            System.out.println("下载成功: " + fileName);
            return true;
            
        } catch (Exception e) {
            System.out.println("下载失败: " + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("请输入URL: ");
        String url = scanner.nextLine();
        
        System.out.print("请输入保存文件名: ");
        String fileName = scanner.nextLine();
        
        // 执行下载
        downloadFile(url, fileName);
        
        scanner.close();
    }
}