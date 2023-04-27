package com.technovision.homegui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TryBuilder<T> {

    @SafeVarargs
    public static <Z> TryBuilder<Z> of(Class<Z> outClass, Class<? extends Z>... impls) {
        List<Callable<Class<? extends Z>>> list = new ArrayList<>();
        for (Class<? extends Z> impl : impls) list.add(() -> impl);
        return new TryBuilder<>(list);
    }

    public static <Z> TryBuilder<Z> of(Class<Z> outClass, String... impls) {
        List<Callable<Class<? extends Z>>> list = new ArrayList<>();
        for (String impl : impls) list.add(() -> Class.forName(impl).asSubclass(outClass));
        return new TryBuilder<>(list);
    }

    private final List<Callable<Class<? extends T>>> impls;
    private final LogState logState = new LogState();
    private T value;
    private boolean valueComputed = false;

    public TryBuilder(List<Callable<Class<? extends T>>> impls) {
        this.impls = impls;
        assert impls.size() > 0;
    }

    public void clear() {
        this.valueComputed = false;
        this.value = null;
    }

    public T build() {
        T out = null;
        this.logState.begin(this.impls.size());
        for (Callable<Class<? extends T>> callable : this.impls) {
            try {
                Class<? extends T> clazz = callable.call();
                out = clazz.getConstructor().newInstance();
                break;
            } catch (Throwable t) {
                this.logState.add(t);
            }
        }
        this.logState.end();

        this.value = out;
        this.valueComputed = true;
        return out;
    }

    public T get() {
        if (!this.valueComputed) return this.build();
        return this.value;
    }

    public TryBuilder<T> logger(Logger logger) {
        this.logState.logger = logger;
        this.logState.hasLogger = logger != null;
        return this;
    }

    public Logger logger() {
        return this.logState.logger;
    }

    public TryBuilder<T> logType(LogType logType) {
        this.logState.type = logType;
        return this;
    }

    public LogType logType() {
        return this.logState.type;
    }

    public TryBuilder<T> logCondition(LogCondition condition) {
        this.logState.condition = condition;
        return this;
    }

    public LogCondition logCondition() {
        return this.logState.condition;
    }

    public enum LogType {
        ALL,
        LAST,
        NONE
    }

    public enum LogCondition {
        ALWAYS,
        ALL_FAILED,
        NEVER
    }

    private static class LogState {

        Logger logger = null;
        boolean hasLogger = false;
        LogType type = LogType.LAST;
        LogCondition condition = LogCondition.ALL_FAILED;

        private Throwable[] errors;
        private int errorHead = 0;

        void begin(int count) {
            errors = new Throwable[count];
            errorHead = 0;
        }

        void add(Throwable t) {
            if (errorHead >= errors.length) return;
            errors[errorHead] = t;
            errorHead++;
        }

        void add(int index, Throwable t) {
            if (index >= errors.length || index < 0) return;
            errors[index] = t;
            errorHead = index + 1;
        }

        void end() {
            if (!hasLogger || type == LogType.NONE || condition == LogCondition.NEVER) return;

            int len = errors.length;
            if (len < 1) return;

            if (condition == LogCondition.ALL_FAILED) {
                for (Throwable error : errors) {
                    if (Objects.isNull(error)) {
                        return;
                    }
                }
            }

            if (type == LogType.ALL) {
                for (int z = 0; z < (len - 1); z++) {
                    Throwable t = errors[z];
                    if (t != null) t.printStackTrace();
                }
            }

            if (type == LogType.LAST || type == LogType.ALL) {
                Throwable t = errors[len - 1];
                if (t != null) {
                    this.logger.log(Level.WARNING, "Failed to build TryBuilder", t);
                } else {
                    this.logger.log(Level.WARNING, "Failed to build TryBuilder");
                }
            }

            this.begin(0);
        }

    }

}
