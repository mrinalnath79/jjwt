package io.jsonwebtoken.impl.lang;

import io.jsonwebtoken.lang.Assert;
import io.jsonwebtoken.lang.Classes;

import java.lang.reflect.Constructor;

public class PropagatingExceptionFunction<T, R, E extends RuntimeException> implements Function<T, R> {

    private final CheckedFunction<T, R> function;

    private final Function<T, String> msgFunction;
    private final Class<E> clazz;

    public PropagatingExceptionFunction(Function<T, R> f, Class<E> exceptionClass, String msg) {
        this(new DelegatingCheckedFunction<>(f), exceptionClass, new ConstantFunction<T, String>(msg));
    }

    public PropagatingExceptionFunction(CheckedFunction<T, R> fn, Class<E> exceptionClass, final Supplier<String> msgSupplier) {
        this(fn, exceptionClass, new Function<T, String>() {
            @Override
            public String apply(T t) {
                return msgSupplier.get();
            }
        });
    }

    public PropagatingExceptionFunction(CheckedFunction<T, R> f, Class<E> exceptionClass, Function<T, String> msgFunction) {
        this.clazz = Assert.notNull(exceptionClass, "Exception class cannot be null.");
        this.msgFunction = Assert.notNull(msgFunction, "msgFunction cannot be null.");
        this.function = Assert.notNull(f, "Function cannot be null");
    }

    @SuppressWarnings("unchecked")
    public R apply(T t) {
        try {
            return function.apply(t);
        } catch (Exception e) {
            if (clazz.isAssignableFrom(e.getClass())) {
                throw clazz.cast(e);
            }
            String msg = this.msgFunction.apply(t);
            if (!msg.endsWith(".")) {
                msg += ".";
            }
            msg += " Cause: " + e.getMessage();
            Class<RuntimeException> clazzz = (Class<RuntimeException>) clazz;
            Constructor<RuntimeException> ctor = Classes.getConstructor(clazzz, String.class, Throwable.class);
            throw Classes.instantiate(ctor, msg, e);
        }
    }
}
