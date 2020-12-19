package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelPath_PathTest {
    @Test
    void canAccessPathAsString() {
        assertAll(() -> {
            assertEquals("a.b", path("a.b").get());
            assertEquals("c.d.e", path("c.d.e").get());
            assertEquals("h", path("h").get());
        });
    }

    @Test
    void rootStringPathIsEmptyString() {
        assertEquals("", root().get());
    }

    @Test
    void parentPathDoesNotIncludeDescendantName() {
        assertAll(() -> {
            assertThat(path("b.c").getParent().map(ModelPath::get), optionalWithValue(equalTo("b")));
            assertThat(path("x.y.z").getParent().map(ModelPath::get), optionalWithValue(equalTo("x.y")));
            assertThat(path("w.x").getParent().map(ModelPath::get), optionalWithValue(equalTo("w")));
        });
    }
}
