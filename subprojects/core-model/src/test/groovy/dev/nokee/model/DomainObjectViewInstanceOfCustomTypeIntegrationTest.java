/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model;

import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;

/**
 * It is possible to create custom type of a DomainObjectView.
 */
class DomainObjectViewInstanceOfCustomTypeIntegrationTest {
	private final ModelRegistry modelRegistry = new DefaultModelRegistry(objectFactory()::newInstance);

	@Test
	@Disabled
	void canCreateCustomView() {
		val myTypes = modelRegistry.register(ModelRegistration.of("myTypes", CustomViewOfMyType.class));
		val myType = modelRegistry.register(ModelRegistration.of("myTypes.foo", MyType.class));
		val action = (Action<MyType>) Mockito.mock(Action.class);
		myTypes.get().configureEach(action);
		Mockito.verify(action, Mockito.never()).execute(isA(MyType.class));

		myType.get();

		Mockito.verify(action, times(1)).execute(isA(MyType.class));
	}

	interface MyType {}
	interface CustomViewOfMyType extends DomainObjectView<MyType> {}
}
