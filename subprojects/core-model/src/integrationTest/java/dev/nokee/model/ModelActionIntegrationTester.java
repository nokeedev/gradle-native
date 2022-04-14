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
package dev.nokee.model;

import dev.nokee.internal.Factories;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedName;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.actions.ModelSpec;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import org.gradle.api.Project;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.model.internal.actions.ModelAction.configureMatching;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
abstract class ModelActionIntegrationTester {
	@Mock private ModelAction firstAction;
	private final Project project = ProjectTestUtils.rootProject();
	private ModelRegistry registry;

	public abstract ModelSpec spec();
	public abstract ModelRegistration newEntityMatchingSpec();
	public abstract ModelRegistration newEntityNotMatchingSpec();

	public DefaultModelRegistry registry() {
		return (DefaultModelRegistry) registry;
	}

	@BeforeEach
	void setUp() {
		project.getPluginManager().apply(ModelBasePlugin.class);
		registry = project.getExtensions().getByType(ModelRegistry.class);
		registry.instantiate(builder().build()); // empty, non-matching, entity
		registry.instantiate(configureMatching(spec(), firstAction));
	}

	@Test
	void doesNotExecuteActionWhenNoEntityMatch() {
		verify(firstAction, never()).execute(any());
	}

	@Test
	void doesNotExecuteActionWhenEntityHasCompatibleIdentityWithSpecButDoesNotMatchSpec() {
		registry.instantiate(newEntityNotMatchingSpec());
		verify(firstAction, never()).execute(any());
	}

	@Nested
	class OnMatchingEntityTest {
		@Mock private ModelAction secondAction;
		private ModelNode entity;

		@BeforeEach
		void createMatchingEntity() {
			entity = registry.instantiate(newEntityMatchingSpec());
		}

		@Test
		void executesActionForEntity() {
			verify(firstAction).execute(entity);
		}

		@Test
		void executesNewActionForEntity() {
			registry.instantiate(configureMatching(spec(), secondAction));
			verify(secondAction).execute(entity);
		}

		@Test
		void doesNotExecuteCurrentActionAfterFirstExecution() {
			reset(firstAction); // ignore expected callback

			// force the modification of the entity identity with no-op selector
			entity.addComponent(createdUsing(of(MyOtherType.class), Factories.alwaysThrow()));

			// should not call back again as it should already have called back
			verify(firstAction, never()).execute(any());
		}

		@Test
		void doesNotExecuteNewActionAfterFirstExecution() {
			registry.instantiate(configureMatching(spec(), secondAction));
			reset(secondAction); // ignore expected callback

			// force the modification of the entity identity with no-op selector
			entity.addComponent(createdUsing(of(MyOtherType.class), Factories.alwaysThrow()));

			// should not call back again as it should already have called back
			verify(secondAction, never()).execute(any());
		}
	}

	@ParameterizedTest
	@EnumSource(B.class)
	void throwsExceptionIfEntityChangeWhileExecutingAction(B provider) {
		doAnswer(args -> {
			args.getArgument(0, ModelNode.class).addComponent(provider.g());
			return null;
		}).when(firstAction).execute(any());
		Assertions.assertThrows(IllegalStateException.class, () -> registry.instantiate(newEntityMatchingSpec()));
	}

	enum B {
		PROJECTION {
			@Override
			public ModelComponent g() {
				return createdUsing(of(MyOtherType.class), Factories.alwaysThrow());
			}
		},
		OWNER {
			@Override
			public ModelComponent g() {
				return new ParentComponent(new ModelNode());
			}
		},
		STATE {
			@Override
			public ModelComponent g() {
				return ModelState.Registered;
			}
		},
		ANCESTOR {
			@Override
			public ModelComponent g() {
				final ModelNode g = new ModelNode();
				g.addComponent(new ParentComponent(new ModelNode()));
				return new ParentComponent(g);
			}
		},
		ELEMENT_NAME {
			@Override
			public ModelComponent g() {
				return new ElementNameComponent(ElementName.of("lkew"));
			}
		},
		FULLY_QUALIFIED_NAME {
			@Override
			public ModelComponent g() {
				return new FullyQualifiedNameComponent(FullyQualifiedName.of("peor"));
			}
		}
		;

		public abstract ModelComponent g();
	}

	@Test
	void executeSelfMutatingActionAfterCurrentActions() {
		final ModelAction secondAction = Mockito.mock(ModelAction.class, "secondAction");
		final ModelAction thirdAction = Mockito.mock(ModelAction.class, "thirdAction");
		registry.instantiate(configureMatching(spec(), secondAction));
		doAnswer(args -> {
			registry.instantiate(configureMatching(spec(), thirdAction));
			return null;
		}).when(firstAction).execute(any());

		final ModelNode entity = registry.instantiate(newEntityMatchingSpec());
		final InOrder inOrder = Mockito.inOrder(firstAction, secondAction, thirdAction);
		inOrder.verify(firstAction).execute(entity);
		inOrder.verify(secondAction).execute(entity);
		inOrder.verify(thirdAction).execute(entity);
	}


	private interface MyOtherType {}
}
