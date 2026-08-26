package com.ravcube.lib.logger.core;

import com.ravcube.lib.logger.Logger;
import com.ravcube.lib.logger.LoggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Objects;

public final class SpringLoggerFactory implements LoggerFactory {

    @Override
    public Logger getLogger(Class<?> type) {
        return new SpringLogger(LogFactory.getLog(Objects.requireNonNull(type, "type must not be null")));
    }

    private static final class SpringLogger implements Logger {

        private final Log delegate;

        private SpringLogger(Log delegate) {
            this.delegate = delegate;
        }

        @Override
        public void debug(String message, Object... arguments) {
            if (delegate.isDebugEnabled()) {
                delegate.debug(format(message, arguments));
            }
        }

        @Override
        public void info(String message, Object... arguments) {
            if (delegate.isInfoEnabled()) {
                delegate.info(format(message, arguments));
            }
        }

        @Override
        public void warn(String message, Object... arguments) {
            if (delegate.isWarnEnabled()) {
                delegate.warn(format(message, arguments));
            }
        }

        @Override
        public void error(String message, Object... arguments) {
            if (delegate.isErrorEnabled()) {
                delegate.error(format(message, arguments));
            }
        }

        @Override
        public void error(String message, Throwable cause, Object... arguments) {
            if (delegate.isErrorEnabled()) {
                delegate.error(format(message, arguments), cause);
            }
        }

        private static String format(String message, Object... arguments) {
            Objects.requireNonNull(message, "message must not be null");
            if (arguments == null || arguments.length == 0) {
                return message;
            }

            StringBuilder result = new StringBuilder();
            int argumentIndex = 0;
            int start = 0;
            int placeholder;
            while (argumentIndex < arguments.length
                    && (placeholder = message.indexOf("{}", start)) >= 0) {
                result.append(message, start, placeholder);
                result.append(arguments[argumentIndex++]);
                start = placeholder + 2;
            }
            result.append(message, start, message.length());

            while (argumentIndex < arguments.length) {
                result.append(" [").append(arguments[argumentIndex++]).append(']');
            }
            return result.toString();
        }
    }
}
