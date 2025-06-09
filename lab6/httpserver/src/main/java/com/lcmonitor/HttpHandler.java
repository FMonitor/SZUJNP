package com.lcmonitor;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpHandler implements Runnable {
    private final Socket clientSocket;
    private final HttpServer server;
    private BufferedReader in;
    private PrintWriter out;
    private OutputStream outputStream;

    public HttpHandler(Socket socket, HttpServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        server.onConnectionStart();
        
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream());
            outputStream = clientSocket.getOutputStream();

            HttpRequest request = parseRequest();
            if (request != null) {
                handleRequest(request);
            }
        } catch (IOException e) {
            System.err.println("处理请求时出错: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接时出错: " + e.getMessage());
            }
            server.onConnectionEnd();
        }
    }

    // ...existing parseRequest method...
    private HttpRequest parseRequest() throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            return null;
        }

        String method = parts[0];
        String path = parts[1];
        String version = parts[2];

        Map<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0].toLowerCase(), headerParts[1]);
            }
        }

        String body = "";
        if ("POST".equals(method) && headers.containsKey("content-length")) {
            int contentLength = Integer.parseInt(headers.get("content-length"));
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars, 0, contentLength);
            body = new String(bodyChars);
        }

        return new HttpRequest(method, path, version, headers, body);
    }

    private void handleRequest(HttpRequest request) throws IOException {
        System.out.println("[" + Thread.currentThread().getName() + "] " + 
                          request.getMethod() + " " + request.getPath());

        switch (request.getMethod()) {
            case "GET":
                handleGet(request);
                break;
            case "HEAD":
                handleHead(request);
                break;
            case "POST":
                handlePost(request);
                break;
            default:
                sendError(405, "Method Not Allowed");
        }
    }

    private void handleGet(HttpRequest request) throws IOException {
        String path = request.getPath();
        
        // API端点处理
        if (path.startsWith("/api/")) {
            handleApiRequest(request, path);
            return;
        }
        
        // 默认页面
        if (path.equals("/")) {
            path = "/index.html";
        }

        // 增强的Cookie处理
        String existingSessionId = getSessionId(request);
        CookieManager.SessionInfo session = CookieManager.createOrUpdateSession(existingSessionId);

        // 使用类路径资源
        String resourcePath = "webapp" + path;
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        
        if (resourceStream != null) {
            try {
                byte[] content = resourceStream.readAllBytes();
                String contentType = getContentType(path);
                
                // 如果是HTML页面，注入会话信息
                if (contentType.contains("text/html") && path.equals("/index.html")) {
                    content = injectSessionInfo(content, session);
                }
                
                sendResponse(200, "OK", contentType, content, session);
            } catch (IOException e) {
                sendError(500, "Internal Server Error");
            } finally {
                resourceStream.close();
            }
        } else {
            sendError(404, "Not Found");
        }
    }

    private void handleHead(HttpRequest request) throws IOException {
        String path = request.getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        String resourcePath = "webapp" + path;
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        
        if (resourceStream != null) {
            try {
                int contentLength = resourceStream.available();
                String contentType = getContentType(path);
                
                out.print("HTTP/1.1 200 OK\r\n");
                out.print("Content-Type: " + contentType + "\r\n");
                out.print("Content-Length: " + contentLength + "\r\n");
                out.print("Server: JavaHttpServer/1.0\r\n");
                out.print("\r\n");
                out.flush();
            } finally {
                resourceStream.close();
            }
        } else {
            sendError(404, "Not Found");
        }
    }

    private void handlePost(HttpRequest request) throws IOException {
        // 处理Cookie会话
        String existingSessionId = getSessionId(request);
        CookieManager.SessionInfo session = CookieManager.createOrUpdateSession(existingSessionId);
        
        String response = "<!DOCTYPE html><html><head><title>POST响应</title></head><body>" +
                         "<h1>POST请求处理成功</h1>" +
                         "<h2>会话信息:</h2>" +
                         "<p>访问者ID: " + session.getVisitorId() + "</p>" +
                         "<p>会话ID: " + session.getSessionId() + "</p>" +
                         "<p>首次访问: " + CookieManager.formatDateTime(session.getFirstVisit()) + "</p>" +
                         "<p>最后访问: " + CookieManager.formatDateTime(session.getLastVisit()) + "</p>" +
                         "<p>访问次数: " + session.getVisitCount() + "</p>" +
                         "<h2>接收到的数据:</h2>" +
                         "<p>" + request.getBody() + "</p>" +
                         "<p>处理线程: " + Thread.currentThread().getName() + "</p>" +
                         "<p><a href='/'>返回首页</a></p>" +
                         "</body></html>";
        
        sendResponse(200, "OK", "text/html; charset=utf-8", response.getBytes(), session);
    }

    // 新增API处理方法
    private void handleApiRequest(HttpRequest request, String path) throws IOException {
        if (path.equals("/api/test")) {
            String existingSessionId = getSessionId(request);
            CookieManager.SessionInfo session = CookieManager.createOrUpdateSession(existingSessionId);
            
            String jsonResponse = "{\n" +
                "  \"status\": \"success\",\n" +
                "  \"message\": \"API测试成功\",\n" +
                "  \"timestamp\": \"" + CookieManager.formatDateTime(session.getLastVisit()) + "\",\n" +
                "  \"visitorId\": " + session.getVisitorId() + ",\n" +
                "  \"visitCount\": " + session.getVisitCount() + ",\n" +
                "  \"thread\": \"" + Thread.currentThread().getName() + "\",\n" +
                "  \"totalVisitors\": " + CookieManager.getTotalVisitors() + ",\n" +
                "  \"activeSessions\": " + CookieManager.getActiveSessions() + "\n" +
                "}";
            sendResponse(200, "OK", "application/json", jsonResponse.getBytes(), session);
        } else if (path.equals("/api/stats")) {
            String jsonResponse = "{\n" +
                "  \"totalVisitors\": " + CookieManager.getTotalVisitors() + ",\n" +
                "  \"activeSessions\": " + CookieManager.getActiveSessions() + ",\n" +
                "  \"activeConnections\": " + server.getActiveConnections() + ",\n" +
                "  \"totalConnections\": " + server.getTotalConnections() + "\n" +
                "}";
            sendResponse(200, "OK", "application/json", jsonResponse.getBytes(), null);
        } else {
            sendError(404, "API Not Found");
        }
    }

    // 向HTML页面注入会话信息
    private byte[] injectSessionInfo(byte[] htmlContent, CookieManager.SessionInfo session) {
        String html = new String(htmlContent);
        
        String sessionInfo = String.format(
            "<script>\n" +
            "window.sessionInfo = {\n" +
            "  visitorId: %d,\n" +
            "  sessionId: '%s',\n" +
            "  firstVisit: '%s',\n" +
            "  lastVisit: '%s',\n" +
            "  visitCount: %d,\n" +
            "  totalVisitors: %d\n" +
            "};\n" +
            "</script>",
            session.getVisitorId(),
            session.getSessionId(),
            CookieManager.formatDateTime(session.getFirstVisit()),
            CookieManager.formatDateTime(session.getLastVisit()),
            session.getVisitCount(),
            CookieManager.getTotalVisitors()
        );
        
        // 在</head>之前插入会话信息
        html = html.replace("</head>", sessionInfo + "\n</head>");
        
        return html.getBytes();
    }

    private void sendResponse(int statusCode, String statusText, String contentType, 
                            byte[] content, CookieManager.SessionInfo session) throws IOException {
        out.print("HTTP/1.1 " + statusCode + " " + statusText + "\r\n");
        out.print("Content-Type: " + contentType + "\r\n");
        out.print("Content-Length: " + content.length + "\r\n");
        out.print("Server: JavaHttpServer/1.0\r\n");
        
        if (session != null) {
            // 设置多个Cookie
            out.print("Set-Cookie: SESSIONID=" + session.getSessionId() + "; Path=/; HttpOnly\r\n");
            out.print("Set-Cookie: VISITOR_ID=" + session.getVisitorId() + "; Path=/; HttpOnly\r\n");
            out.print("Set-Cookie: FIRST_VISIT=" + CookieManager.formatDateTime(session.getFirstVisit()).replace(" ", "_") + "; Path=/; HttpOnly\r\n");
            out.print("Set-Cookie: VISIT_COUNT=" + session.getVisitCount() + "; Path=/; HttpOnly\r\n");
        }
        
        out.print("\r\n");
        out.flush();

        outputStream.write(content);
        outputStream.flush();
    }

    private void sendError(int statusCode, String statusText) throws IOException {
        String errorPage = "<!DOCTYPE html>\n" +
            "<html><head><title>" + statusCode + " " + statusText + "</title></head>\n" +
            "<body style='font-family: Arial, sans-serif; text-align: center; padding: 50px;'>\n" +
            "<h1>" + statusCode + " " + statusText + "</h1>\n" +
            "<p>处理线程: " + Thread.currentThread().getName() + "</p>\n" +
            "<p>服务器: Java HTTP Server 1.0</p>\n" +
            "<hr><a href='/'>返回首页</a></body></html>";
        sendResponse(statusCode, statusText, "text/html; charset=utf-8", errorPage.getBytes(), null);
    }

    private String getContentType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".html")) return "text/html; charset=utf-8";
        if (lowerName.endsWith(".css")) return "text/css; charset=utf-8";
        if (lowerName.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
        if (lowerName.endsWith(".gif")) return "image/gif";
        if (lowerName.endsWith(".ico")) return "image/x-icon";
        if (lowerName.endsWith(".svg")) return "image/svg+xml";
        if (lowerName.endsWith(".json")) return "application/json";
        if (lowerName.endsWith(".txt")) return "text/plain; charset=utf-8";
        return "application/octet-stream";
    }

    private String getSessionId(HttpRequest request) {
        String cookieHeader = request.getHeaders().get("cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=");
                if (parts.length == 2 && "SESSIONID".equals(parts[0])) {
                    return parts[1];
                }
            }
        }
        return null;
    }
}