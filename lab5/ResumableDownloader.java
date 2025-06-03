import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ResumableDownloader {

    private String fileURL;
    private String savePath;
    private long totalsize = 100 * 1024 * 1024; // 假设文件大小为100MB

    public ResumableDownloader(String fileURL, String savePath) {
        this.fileURL = fileURL;
        this.savePath = savePath;
    }

    public void download() {
        try {
            long totalSize;
            {
                HttpURLConnection sizeConn = (HttpURLConnection) new URI(fileURL).toURL().openConnection();
                sizeConn.setRequestMethod("HEAD");
                totalSize = sizeConn.getContentLengthLong();
                sizeConn.disconnect();
                System.out.printf("文件总大小：%.2f MB\n", totalsize / (1024.0 * 1024.0));
            }
            while (true) {
                File file = new File(savePath);
                long existingFileSize = file.exists() ? file.length() : 0;

                HttpURLConnection conn = (HttpURLConnection) new URI(fileURL).toURL().openConnection();
                conn.setRequestProperty("Range", "bytes=" + existingFileSize + "-");
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode == 206 || responseCode == 200) {
                    try (InputStream inputStream = conn.getInputStream();
                            RandomAccessFile raf = new RandomAccessFile(file, "rw")) {

                        raf.seek(existingFileSize);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalDownloaded = existingFileSize;
                        long pauseAfter = existingFileSize + 30L * 1024 * 1024; // 暂停下载前下载30MB

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            raf.write(buffer, 0, bytesRead);
                            totalDownloaded += bytesRead;
                            if (totalDownloaded >= totalsize) {
                                System.out.printf("下载完成，总大小：%.2f MB\n", totalDownloaded / (1024.0 * 1024.0));
                                return;
                            }
                            if (totalDownloaded >= pauseAfter) {
                                double fileSizeMB = totalDownloaded / (1024.0 * 1024.0);
                                System.out.printf("当前下载进度：%.2f MB，准备暂停...\n", fileSizeMB);
                                break;
                            }
                        }
                    }
                    System.out.println("模拟下载中断，暂停3秒...\n");
                    Thread.sleep(3000);
                    System.out.println("恢复下载...\n");
                } else {
                    System.out.println("服务器不支持断点续传，响应码：" + responseCode);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("下载失败：" + e.getMessage());
        }

        System.out.println(totalSize);
    }

    public static void main(String[] args) {
        String url = "https://speed.cloudflare.com/__down?during=download&bytes=104857600"; // 你可换为合适的测试文件地址
        String localPath = "100MB.txt";

        ResumableDownloader downloader = new ResumableDownloader(url, localPath);
        downloader.download();
    }
}
