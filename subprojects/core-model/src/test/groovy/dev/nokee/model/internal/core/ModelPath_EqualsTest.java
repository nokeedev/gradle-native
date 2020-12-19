package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;

class ModelPath_EqualsTest {
    @Test
	@SuppressWarnings("UnstableApiUsage")
    void canEquals() {
        new EqualsTester()
                .addEqualityGroup(path("a.b.c"), path("a.b.c"))
                .addEqualityGroup(path("z.y.z"))
                .addEqualityGroup(root(), path("a").getParent().get())
                .addEqualityGroup(path("z.y.q").getParent().get(), path("z.y"))
                .testEquals();
    }
}
