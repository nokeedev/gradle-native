package dev.nokee.model.internal.type;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelTypeTest {
    @Test
    @SuppressWarnings("UnstableApiUsage")
    void canEquals() {
        new EqualsTester()
                .addEqualityGroup(of(String.class), of(String.class))
                .addEqualityGroup(of(Integer.class))
                .testEquals();
    }

    @Test
    void canAccessRawType() {
        assertAll(() -> {
            assertEquals(String.class, of(String.class).getRawType());
            assertEquals(Integer.class, of(Integer.class).getRawType());
            assertEquals(MyType.class, of(MyType.class).getRawType());
        });
    }

    @Test
    void canAccessConcreteType() {
        assertAll(() -> {
            assertEquals(String.class, of(String.class).getConcreteType());
            assertEquals(Integer.class, of(Integer.class).getConcreteType());
            assertEquals(MyType.class, of(MyType.class).getConcreteType());
        });
    }

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void checkNulls() {
        new NullPointerTester().testAllPublicStaticMethods(ModelType.class);
    }

    interface MyType {}
}
