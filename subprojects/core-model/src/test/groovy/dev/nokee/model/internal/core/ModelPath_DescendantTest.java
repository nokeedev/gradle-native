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
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ModelPath.class)
class ModelPath_DescendantTest {
	@Test
	void directDescendantAreDetectedAsDescendant() {
		assertTrue(path("a.b").isDescendant(path("a.b.c")), "direct descendant should be a descendant of the current path");
		assertTrue(root().isDescendant(path("b")), "direct descendant should be a descendant of the current path");
	}

	@Test
	void indirectDescendantAreDetectedAsDescendant() {
		assertTrue(path("a").isDescendant(path("a.b.c")), "should be a descendant");
		assertTrue(root().isDescendant(path("x.y")), "should be a descendant");
		assertTrue(path("f.i").isDescendant(path("f.i.j.i")), "should be a descendant");
	}

	@Test
	void ancestorPathAreNotDescendant() {
		assertFalse(path("a.b.c").isDescendant(path("a")), "should not be a descendant");
		assertFalse(path("x.y.z").isDescendant(path("x.y")), "should not be a descendant");
		assertFalse(path("b.c").isDescendant(root()), "should not be a descendant");
	}
}
