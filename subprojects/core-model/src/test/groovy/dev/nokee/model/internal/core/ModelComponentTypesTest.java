/*
 * Copyright 2021 the original author or authors.
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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelComponentType.projectionOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelComponentTypesTest {
	private final ModelComponentTypes subject = new ModelComponentTypes(ImmutableSet.of(componentOf(MyComponent.class), projectionOf(IMyProjection.class)));

	@Test
	void canCheckIfExactComponentTypeIsPresent() {
		assertTrue(subject.contains(componentOf(MyComponent.class)));
		assertTrue(subject.contains(projectionOf(IMyProjection.class)));

		assertFalse(subject.contains(componentOf(MyOtherComponent.class)));
		assertFalse(subject.contains(projectionOf(MyProjection.class)));
	}

	@Test
	void canCheckIfAnyComponentTypeMatches() {
		assertTrue(subject.anyMatch(componentOf(MyComponent.class)::isSupertypeOf));
		assertTrue(subject.anyMatch(projectionOf(IMyProjection.class)::isSupertypeOf));
		assertFalse(subject.anyMatch(projectionOf(MyProjection.class)::isSupertypeOf));
	}

	private interface MyComponent extends ModelComponent {}
	private interface MyOtherComponent extends ModelComponent {}
	private interface IMyProjection {}
	private interface MyProjection extends IMyProjection {}
}
