package com.example.web;

import com.example.gui.Instructor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Small HTTP server that provides a web UI to send NEWWORK and SHUTDOWN commands to the existing UDP Server.
 * Usage: java -jar app.jar web [httpPort]
 */
public class WebInterface {
    private static final String INDEX_HTML = "<!doctype html>\n" +
            "<html><head><meta charset=\"utf-8\"><title>Load Balancer Web UI</title></head><body>\n" +
            "<h1>Load Balancer - Web Interface</h1>\n" +
            "<form method=\"post\" action=\"/sendWork\">\n" +
            "Server IP: <input name=\"serverIp\" value=\"127.0.0.1\"/> <br/>\n" +
            "Server Port: <input name=\"serverPort\" value=\"5000\"/> <br/>\n" +
            "Duration (seconds): <input name=\"duration\" value=\"5\"/> <br/>\n" +
            "<button type=\"submit\">Send Work</button>\n" +
            "</form>\n" +
            "<form method=\"post\" action=\"/shutdown\">\n" +
            "Server IP: <input name=\"serverIp\" value=\"127.0.0.1\"/> <br/>\n" +
            "Server Port: <input name=\"serverPort\" value=\"5000\"/> <br/>\n" +
            "<button type=\"submit\">Shutdown Server</button>\n" +
            "</form>\n" +
            "</body></html>";

    public static void main(String[] args) throws Exception {
        int httpPort = 8080;
        if (args != null && args.length > 0) {
            try {
                httpPort = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(httpPort), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/sendWork", new SendWorkHandler());
        server.createContext("/shutdown", new ShutdownHandler());
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Web interface started on http://localhost:" + httpPort);
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] bytes = INDEX_HTML.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static Map<String,String> parseForm(InputStream is) throws IOException {
        byte[] body = is.readAllBytes();
        String s = new String(body, StandardCharsets.UTF_8);
        Map<String,String> out = new HashMap<>();
        if (s.isBlank()) return out;
        String[] pairs = s.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            out.put(key, val);
        }
        return out;
    }

    static class SendWorkHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Map<String,String> form = parseForm(exchange.getRequestBody());
            String serverIp = form.getOrDefault("serverIp", "127.0.0.1");
            int serverPort = Integer.parseInt(form.getOrDefault("serverPort", "5000"));
            int duration = Integer.parseInt(form.getOrDefault("duration", "5"));
            try {
                InetAddress addr = InetAddress.getByName(serverIp);
                Instructor instr = new Instructor(addr, serverPort);
                instr.sendNewWork(duration);
                String resp = "OK: Sent NEWWORK," + duration + " to " + serverIp + ":" + serverPort;
                byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            } catch (Exception e) {
                String msg = "ERROR: " + e.getMessage();
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            }
        }
    }

    static class ShutdownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Map<String,String> form = parseForm(exchange.getRequestBody());
            String serverIp = form.getOrDefault("serverIp", "127.0.0.1");
            int serverPort = Integer.parseInt(form.getOrDefault("serverPort", "5000"));
            try {
                InetAddress addr = InetAddress.getByName(serverIp);
                Instructor instr = new Instructor(addr, serverPort);
                instr.shutdown();
                String resp = "OK: Sent SHUTDOWN to " + serverIp + ":" + serverPort;
                byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            } catch (Exception e) {
                String msg = "ERROR: " + e.getMessage();
                byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(500, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            }
        }
    }
}

