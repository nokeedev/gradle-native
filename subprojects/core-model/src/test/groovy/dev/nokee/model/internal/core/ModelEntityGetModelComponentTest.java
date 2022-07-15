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

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelEntityGetModelComponentTest {
	private final ModelNode subject = new ModelNode();
	private final ModelComponent component = new MyComponent();

	@BeforeEach
	void setUp() {
		subject.addComponent(component);
	}

	@Test
	void canGetModelComponentByClass() {
		assertThat(subject.get(MyComponent.class), is(component));
	}

	@Test
	void canGetModelComponentByType() {
		assertThat(subject.getComponent(ModelComponentType.componentOf(MyComponent.class)), is(component));
	}

	@Test
	void throwsExceptionWhenModelComponentByTypeDoesNotExistsOnEntity() {
		val ex = assertThrows(RuntimeException.class, () -> subject.getComponent(ModelComponentType.componentOf(MyOtherComponent.class)));
		assertThat(ex.getMessage(), equalTo("No components of type 'ModelEntityGetModelComponentTest.MyOtherComponent'. Available: ModelEntityGetModelComponentTest.MyComponent"));
	}

	@Test
	void throwsExceptionWhenModelComponentByClassDoesNotExistsOnEntity() {
		val ex = assertThrows(RuntimeException.class, () -> subject.get(MyOtherComponent.class));
		assertThat(ex.getMessage(), equalTo("No components of type 'ModelEntityGetModelComponentTest.MyOtherComponent'. Available: ModelEntityGetModelComponentTest.MyComponent"));
	}

	private static final class MyComponent implements ModelComponent {}
	private interface MyOtherComponent extends ModelComponent {}
}
