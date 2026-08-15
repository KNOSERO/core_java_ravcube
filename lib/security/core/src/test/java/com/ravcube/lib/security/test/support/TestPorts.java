package com.ravcube.lib.security.test.support;

import java.io.IOException;
import java.net.ServerSocket;

public final class TestPorts {

    private TestPorts() {
    }

    public static int randomAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot allocate random service port for test", exception);
        }
    }
}
