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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.View;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.utils.ClosureTestUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ComponentContainerAdapaterDisallowTestComponentTest {
	@Mock private View<Component> delegate;
	@Mock private ModelRegistry registry;
	private ComponentContainerAdapter subject;

	@BeforeEach
	void createSubject() {
		subject = new ComponentContainerAdapter(delegate, registry, new ModelNode());
	}

	@Test
	void throwsExceptionOnRegisterTestSuiteComponents() {
		val ex = assertThrows(IllegalArgumentException.class, () -> subject.register("main", MyTestSuiteComponent.class));
		assertThat(ex.getMessage(), equalTo("Cannot register test suite components in this container, use a TestSuiteContainer instead."));
	}

	@Test
	void throwsExceptionOnConfigureEachTestSuiteComponentsWithAction() {
		val ex = assertThrows(IllegalArgumentException.class, () -> subject.configureEach(MyTestSuiteComponent.class, doSomething()));
		assertThat(ex.getMessage(), equalTo("Cannot configure test suite components in this container, use a TestSuiteContainer instead."));
	}

	@Test
	void throwsExceptionOnConfigureEachTestSuiteComponentsWithClosure() {
		val ex = assertThrows(IllegalArgumentException.class, () -> subject.configureEach(MyTestSuiteComponent.class, ClosureTestUtils.doSomething(MyTestSuiteComponent.class)));
		assertThat(ex.getMessage(), equalTo("Cannot configure test suite components in this container, use a TestSuiteContainer instead."));
	}

	@Test
	void throwsExceptionOnFilterTestSuiteComponents() {
		val ex = assertThrows(IllegalArgumentException.class, () -> subject.withType(MyTestSuiteComponent.class));
		assertThat(ex.getMessage(), equalTo("Cannot filter test suite components in this container, use a TestSuiteContainer instead."));
	}

	static class MyTestSuiteComponent implements TestSuiteComponent {
		@Override
		public TestSuiteComponent testedComponent(Object component) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}
	}
}
