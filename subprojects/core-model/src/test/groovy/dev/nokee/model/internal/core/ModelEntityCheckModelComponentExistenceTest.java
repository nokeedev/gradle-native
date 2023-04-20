/*
 * Copyright 2022 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ModelEntityCheckModelComponentExistenceTest {
	private final ModelNode subject = newEntity();

	@BeforeEach
	void setUp() {
		subject.addComponent(new MyComponent());
	}

	@Test
	void canCheckModelComponentByClass() {
		assertThat(subject.has(MyComponent.class), is(true));
		assertThat(subject.has(MyOtherComponent.class), is(false));
	}

	@Test
	void canCheckModelComponentByType() {
		assertThat(subject.hasComponent(ModelComponentType.componentOf(MyComponent.class)), is(true));
		assertThat(subject.hasComponent(ModelComponentType.componentOf(MyOtherComponent.class)), is(false));
	}

	private static final class MyComponent implements ModelComponent {}
	private interface MyOtherComponent extends ModelComponent {}
}
