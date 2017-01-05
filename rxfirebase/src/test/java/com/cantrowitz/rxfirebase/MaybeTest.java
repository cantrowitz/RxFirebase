package com.cantrowitz.rxfirebase;

import org.junit.Test;

import static junit.framework.Assert.assertSame;
import static junit.framework.TestCase.assertFalse;

/**
 * Tests for {@link Maybe}
 */
public class MaybeTest {

    Maybe<Object> target;

    @Test
    public void absent() throws Exception {
        target = Maybe.absent();
        assertFalse(target.isPresent());
    }

    @Test
    public void of() throws Exception {
        final Object value = "test";
        target = Maybe.of(value);
        assertSame(value, target.get());
    }

    @Test(expected = NullPointerException.class)
    public void of_whenNull() throws Exception {
        target = Maybe.of(null);
    }

    @Test
    public void fromNullable_whenNull() throws Exception {
        final Object value = null;
        target = Maybe.fromNullable(value);
        assertFalse(target.isPresent());
    }

    @Test
    public void fromNullable_whenPresent() throws Exception {
        final Object value = "test";
        target = Maybe.fromNullable(value);
        assertSame(value, target.get());
    }

    @Test(expected = IllegalStateException.class)
    public void get_whenAbsent() throws Exception {
        target = Maybe.absent();
        target.get();
    }
}