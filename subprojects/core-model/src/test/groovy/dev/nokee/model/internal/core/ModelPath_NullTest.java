package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;

public class ModelPath_NullTest {
    @Test
    @SuppressWarnings("UnstableApiUsage")
    void checkNulls() {
        new NullPointerTester().testAllPublicStaticMethods(ModelPath.class);
        new NullPointerTester().testAllPublicInstanceMethods(path("a.b.c"));
    }
}
