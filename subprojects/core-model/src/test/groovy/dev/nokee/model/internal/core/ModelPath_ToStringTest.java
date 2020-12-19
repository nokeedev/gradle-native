package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;

public class ModelPath_ToStringTest {
    @Test
    void toStringReturnsPathForNonRootPath() {
        assertAll(() -> {
            assertThat(path("k.l"), hasToString("k.l"));
            assertThat(path("o.u.i"), hasToString("o.u.i"));
            assertThat(path("r"), hasToString("r"));
        });
    }

    @Test
    void toStringReturnsSpecialValueForRootPath() {
        assertThat(root(), hasToString("<root>"));
    }

    @Test
    void toStringOfParentPathReturnsPath() {
        assertAll(() -> {
            assertThat(path("e.b").getParent(), optionalWithValue(hasToString("e")));
            assertThat(path("n.o.n").getParent(), optionalWithValue(hasToString("n.o")));
            assertThat(path("a").getParent(), optionalWithValue(hasToString("<root>")));
        });
    }
}
