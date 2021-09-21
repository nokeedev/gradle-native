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

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class ModelPath_ParentTest {
    @Test
    void canAccessParentPathIndirectDescendantOfRoot() {
        assertAll(() -> {
            assertThat("the parent path should be path 'a.b'", path("a.b.c").getParent(), optionalWithValue(equalTo(path("a.b"))));
            assertThat("the parent path should be path 'c'", path("c.d").getParent(), optionalWithValue(equalTo(path("c"))));
        });
    }

    @Test
    void theParentOfDirectDescendantOfRootIsRootPath() {
        assertAll(() -> {
            assertThat("the parent path should be path '<root>'", path("g").getParent(), optionalWithValue(equalTo(root())));
            assertThat("the parent path should be path '<root>'", path("y").getParent(), optionalWithValue(equalTo(root())));
        });
    }

    @Test
    void theParentOfTheRootPathIsAbsent() {
        assertThat("the parent path should be absent", root().getParent(), emptyOptional());
    }
}
