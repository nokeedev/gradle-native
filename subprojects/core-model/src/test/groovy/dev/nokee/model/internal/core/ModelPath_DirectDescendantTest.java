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

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.junit.jupiter.api.Assertions.*;

class ModelPath_DirectDescendantTest {
    @Test
    void canCheckDirectDescendant() {
        assertAll(() -> {
            assertTrue(path("a").isDirectDescendant(path("a.b")), "should be a direct descendant");
            assertTrue(path("x.y").isDirectDescendant(path("x.y.z")), "should be a direct descendant");
            assertTrue(root().isDirectDescendant(path("c")), "should be a direct descendant");
        });
    }

    @Test
    void indirectDescendantAreNotDetectedAsDirectDescendant() {
        assertAll(() -> {
            assertFalse(path("a").isDirectDescendant(path("a.b.c")), "should not be a direct descendant");
            assertFalse(root().isDirectDescendant(path("b.c.d")), "should not be a direct descendant");
            assertFalse(path("f.i").isDirectDescendant(path("f.i.j.i")), "should not be a direct descendant");
        });
    }

    @Test
    void unrelatedPathAreNotDetectedAsDirectDescendant() {
        assertAll(() -> {
            assertFalse(path("a").isDirectDescendant(path("c.d")), "should not be a direct descendant");
            assertFalse(path("d.e").isDirectDescendant(path("g.h.i")), "should not be a direct descendant");
        });
    }

    @Test
    void nullIsNotAValidDirectDescendantCheck() {
        assertAll(() -> {
            assertThrows(NullPointerException.class, () -> path("a").isDirectDescendant(null), "should not accept null");
            assertThrows(NullPointerException.class, () -> path("h.t").isDirectDescendant(null), "should not accept null");
            assertThrows(NullPointerException.class, () -> root().isDirectDescendant(null), "should not accept null");
        });
    }
}
