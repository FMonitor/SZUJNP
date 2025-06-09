package com.lcmonitor;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServer {
    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    
    // 添加连接计数器
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    
    // 线程池配置
    private static final int CORE_POOL_SIZE = 20;
    private static final int MAX_POOL_SIZE = 100;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int QUEUE_CAPACITY = 200;

    public HttpServer(int port) {
        this.port = port;

        this.threadPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "HttpServer-Worker-" + threadNumber.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 当线程池满时的拒绝策略
        );
        
        // 启动监控线程
        startMonitorThread();
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("HTTP服务器启动在端口: " + port);
        System.out.println("线程池配置 - 核心线程: " + CORE_POOL_SIZE + ", 最大线程: " + MAX_POOL_SIZE);
        System.out.println("访问 http://localhost:" + port + " 来测试服务器");

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // 检查线程池状态
                ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPool;
                if (executor.getQueue().remainingCapacity() == 0) {
                    System.out.println("警告: 线程池队列已满，当前活跃线程: " + executor.getActiveCount());
                }
                
                totalConnections.incrementAndGet();
                threadPool.submit(new HttpHandler(clientSocket, this));
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("接受客户端连接时出错: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    // 连接管理方法
    public void onConnectionStart() {
        activeConnections.incrementAndGet();
    }
    
    public void onConnectionEnd() {
        activeConnections.decrementAndGet();
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public int getTotalConnections() {
        return totalConnections.get();
    }
    
    // 启动监控线程
    private void startMonitorThread() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000); // 每10秒输出一次状态
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) threadPool;
                    System.out.printf("服务器状态 - 活跃连接: %d, 总连接数: %d, 线程池活跃: %d/%d, 队列大小: %d%n",
                        activeConnections.get(),
                        totalConnections.get(),
                        executor.getActiveCount(),
                        executor.getPoolSize(),
                        executor.getQueue().size()
                    );
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "HttpServer-Monitor");
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("无效端口号，使用默认端口8080");
            }
        }

        HttpServer server = new HttpServer(port);
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭服务器...");
            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("关闭服务器时出错: " + e.getMessage());
            }
        }));
        
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }
}