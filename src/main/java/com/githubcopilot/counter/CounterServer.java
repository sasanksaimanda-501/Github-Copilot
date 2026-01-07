package com.githubcopilot.counter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple HTTP server that serves a static HTML UI and provides API endpoints:
 *  GET  /api/count   -> {"count": <n>}
 *  POST /api/inc     -> {"count": <n>}
 *  POST /api/dec     -> {"count": <n>}
 *  POST /api/reset   -> {"count": 0}
 *
 * Static files served from ./static (index at /).
 *
 * To compile and run (from project root):
 *   mkdir -p out
 *   javac -d out src/main/java/com/githubcopilot/counter/CounterServer.java
 *   cp -r static out/static
 *   java -cp out com.githubcopilot.counter.CounterServer
 *
 * Then open: http://localhost:8080
 */
public class CounterServer {
    private static final AtomicInteger COUNT = new AtomicInteger(0);
    private static final int PORT = 8080;
    private static final String STATIC_DIR = "static";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/static", new StaticHandler());
        server.createContext("/api/count", new CountHandler());
        server.createContext("/api/inc", new IncHandler());
        server.createContext("/api/dec", new DecHandler());
        server.createContext("/api/reset", new ResetHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        System.out.println("Starting CounterServer on http://localhost:" + PORT);
        server.start();
    }

    // Serve index.html at root
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            Path indexPath = Path.of(STATIC_DIR, "index.html");
            if (!Files.exists(indexPath)) {
                String msg = "<html><body><h1>index.html not found in ./static</h1></body></html>";
                byte[] b = msg.getBytes();
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(500, b.length);
                exchange.getResponseBody().write(b);
                exchange.close();
                return;
            }
            sendFile(exchange, indexPath, "text/html; charset=UTF-8");
        }
    }

    // Serve other static files under ./static
    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath(); // e.g. /static/style.css
            String rel = uri.substring("/static/".length()); // e.g. style.css or assets/foo.js
            Path p = Path.of(STATIC_DIR, rel);
            if (!Files.exists(p) || Files.isDirectory(p)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String contentType = guessContentType(p);
            sendFile(exchange, p, contentType);
        }
    }

    static class CountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            sendJson(exchange, COUNT.get());
        }
    }

    static class IncHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            int v = COUNT.incrementAndGet();
            sendJson(exchange, v);
        }
    }

    static class DecHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            int v = COUNT.decrementAndGet();
            sendJson(exchange, v);
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            COUNT.set(0);
            sendJson(exchange, 0);
        }
    }

    private static void sendJson(HttpExchange exchange, int value) throws IOException {
        String resp = "{\"count\":" + value + "}";
        byte[] bytes = resp.getBytes("UTF-8");
        Headers h = exchange.getResponseHeaders();
        h.add("Content-Type", "application/json; charset=UTF-8");
        addCommonHeaders(h);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendFile(HttpExchange exchange, Path path, String contentType) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        Headers h = exchange.getResponseHeaders();
        h.add("Content-Type", contentType);
        addCommonHeaders(h);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // Basic content-type guesses for common static assets
    private static String guessContentType(Path p) {
        String name = p.getFileName().toString().toLowerCase();
        try {
            String probe = Files.probeContentType(p);
            if (probe != null) return probe;
        } catch (IOException ignored) {}
        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css")) return "text/css; charset=UTF-8";
        if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (name.endsWith(".json")) return "application/json; charset=UTF-8";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    // Allow simple cross-origin requests if needed by browser tooling (safe for local dev)
    private static void addCommonHeaders(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
    }
}
