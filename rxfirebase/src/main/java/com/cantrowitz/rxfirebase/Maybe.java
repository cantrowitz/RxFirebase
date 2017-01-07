package com.cantrowitz.rxfirebase;

/**
 * Since RxJava2 doesn't support null, this is a poorman's Java Optional. The intention was to
 * keep the dependency graph as small as possible.
 */

public class Maybe<T> {
    private static final Maybe<?> EMPTY = new Maybe<>();

    private final T value;

    private Maybe() {
        this.value = null;
    }

    private Maybe(T value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    static <T> Maybe<T> absent() {
        return (Maybe<T>) EMPTY;
    }

    static <T> Maybe<T> of(T value) {
        if (value == null) {
            throw new NullPointerException("Value must be non-null");
        }
        return new Maybe<>(value);
    }

    @SuppressWarnings("unchecked")
    static <T> Maybe<T> fromNullable(T value) {
        if (value != null) {
            return Maybe.of(value);
        }
        return (Maybe<T>) EMPTY;
    }

    /**
     * Tests the {@link Maybe} for having a value
     *
     * @return true if there is a value, false otherwise
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Gets the value from the {@link Maybe}. Always make sure that it {@link #isPresent()} first
     *
     * @return the value {@link T}
     */
    public T get() {
        if (isPresent()) {
            return value;
        }
        throw new IllegalStateException("Null value, check isPresent() before calling this.");
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Maybe[%s]", value)
                : "Maybe.empty";
    }
}
