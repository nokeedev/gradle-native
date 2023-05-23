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

import dev.nokee.model.internal.registry.DefaultModelRegistry;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.BiConsumer;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

class ModelNodeCallbackTest {
	private final DefaultModelRegistry registry = new DefaultModelRegistry(objectFactory()::newInstance);
	private final ModelNode subject = ModelNodes.of(registry.register(ModelRegistration.of("foo", MyProjection.class)));
	private final BiConsumer<ModelNode, MyComponent> action = Mockito.mock(BiConsumer.class);

	@BeforeEach
	void registerListener() {
		registry.configure(new ModelActionWithInputs.ModelAction1<MyComponent>() {
			protected void execute(ModelNode entity, MyComponent myComponent) {
				action.accept(entity, myComponent);
			}
		});
	}

	@Nested
	class ComponentTest {
		private final MyComponent component = new MyComponent();

		@BeforeEach
		void addComponent() {
			subject.addComponent(component);
		}

		@Test
		void callsBackWhenInputMatches() {
			Mockito.verify(action).accept(subject, component);
		}

		@Test
		void callsBackWhenInputChanges() {
			val newComponent = new MyComponent();
			Mockito.reset(action);
			subject.setComponent(MyComponent.class, newComponent);
			Mockito.verify(action).accept(subject, newComponent);
		}

		@Nested
		class WhenNewEntity {
			private ModelNode newEntity;

			@BeforeEach
			void newEntity() {
				Mockito.reset(action);
				newEntity = ModelNodes.of(registry.register(ModelRegistration.of("bar", MyProjection.class)));
			}

			@Test
			void doesNotCallbackOnNewEntity() {
				Mockito.verify(action, never()).accept(any(), any());
			}

			@Test
			void callsBackOnNewEntityWhenInputMatches() {
				val component = new MyComponent();
				newEntity.addComponent(component);
				Mockito.verify(action).accept(newEntity, component);
			}
		}
	}

	public interface MyProjection {}
	public static final class MyComponent implements ModelComponent {}
}
