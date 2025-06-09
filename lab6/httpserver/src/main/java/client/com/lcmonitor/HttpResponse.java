package client.com.lcmonitor;

import java.util.Map;

public class HttpResponse {
    private final String statusLine;
    private final Map<String, String> headers;
    private final String body;

    public HttpResponse(String statusLine, Map<String, String> headers, String body) {
        this.statusLine = statusLine;
        this.headers = headers;
        this.body = body;
    }

    public String getStatusLine() { return statusLine; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
}