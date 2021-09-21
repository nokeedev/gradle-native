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
