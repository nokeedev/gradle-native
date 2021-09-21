/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal.core;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelIdentifierTest {
    @Test
    void canCreateModelIdentifierWithRawPathAndRawType() {
        val identifier = ModelIdentifier.of("foo.bar", MyType.class);
        assertAll(() -> {
            assertEquals(path("foo.bar"), identifier.getPath());
            assertEquals(of(MyType.class), identifier.getType());
        });
    }

    @Test
    void canCreateModelIdentifierWithPathAndType() {
        val identifier = ModelIdentifier.of(path("foo.bar"), of(MyType.class));
        assertAll(() -> {
            assertEquals(path("foo.bar"), identifier.getPath());
            assertEquals(of(MyType.class), identifier.getType());
        });
    }

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void canEquals() {
        new EqualsTester()
                .addEqualityGroup(ModelIdentifier.of("foo.bar", MyType.class), ModelIdentifier.of(path("foo.bar"), of(MyType.class)))
                .addEqualityGroup(ModelIdentifier.of("far.bar", MyType.class))
                .addEqualityGroup(ModelIdentifier.of("far.bar", String.class))
                .testEquals();
    }

    @Test
    @SuppressWarnings("UnstableApiUsage")
    void checkNulls() {
        new NullPointerTester()
                .setDefault(ModelType.class, ModelType.of(MyType.class))
                .setDefault(ModelPath.class, ModelPath.path("foo.bar"))
                .testAllPublicStaticMethods(ModelIdentifier.class);
    }

    interface MyType {}
}
