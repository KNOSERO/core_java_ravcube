package com.ravcube.lib.faulttolerance.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public final class TestHttpServer implements AutoCloseable {

    private final HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();

    private TestHttpServer(HttpServer server) {
        this.server = server;
    }

    public static TestHttpServer start(int port, TestResponse response) throws IOException {
        TestHttpServer testServer = new TestHttpServer(HttpServer.create(new InetSocketAddress(port), 0));
        testServer.server.createContext("/downstream/status", exchange -> testServer.handle(exchange, response));
        testServer.server.start();
        return testServer;
    }

    public int requests() {
        return requests.get();
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange, TestResponse response) throws IOException {
        requests.incrementAndGet();
        response.pause();
        byte[] bytes = response.body().getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(response.status(), bytes.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(bytes);
        }
    }
}
