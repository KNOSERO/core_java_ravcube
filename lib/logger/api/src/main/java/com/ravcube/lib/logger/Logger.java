package com.ravcube.lib.logger;

public interface Logger {

    void debug(String message, Object... arguments);

    void info(String message, Object... arguments);

    void warn(String message, Object... arguments);

    void error(String message, Object... arguments);

    void error(String message, Throwable cause, Object... arguments);

    static Logger noop() {
        return NoOpLogger.INSTANCE;
    }

    final class NoOpLogger implements Logger {
        private static final Logger INSTANCE = new NoOpLogger();

        private NoOpLogger() {
        }

        @Override
        public void debug(String message, Object... arguments) {
        }

        @Override
        public void info(String message, Object... arguments) {
        }

        @Override
        public void warn(String message, Object... arguments) {
        }

        @Override
        public void error(String message, Object... arguments) {
        }

        @Override
        public void error(String message, Throwable cause, Object... arguments) {
        }
    }
}
