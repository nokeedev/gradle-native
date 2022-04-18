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
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjections;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class DefaultModelObjectBackedByModelEntityIntegrationTest implements ModelObjectTester<DefaultModelObjectBackedByModelEntityIntegrationTest.MyType> {
	private static final MyType myTypeInstance = Mockito.mock(MyType.class);
	private final ModelRegistry modelRegistry = Mockito.mock(ModelRegistry.class);
	private final ModelNode node = newEntity(modelRegistry);
	private final ModelElementFactory factory = new ModelElementFactory(objectFactory()::newInstance);
	private final DomainObjectProvider<MyType> subject = factory.createObject(node, of(MyType.class));

	private static ModelNode newEntity(ModelRegistry modelRegistry) {
		val entity = node("qibe", ModelProjections.createdUsing(of(MyType.class), () -> myTypeInstance), builder -> builder.withRegistry(modelRegistry));
		entity.addComponent(new IdentifierComponent(ModelIdentifier.of("qibe", Object.class)));
		entity.addComponent(new FullyQualifiedNameComponent("testQibe"));
		return entity;
	}

	@Override
	public DomainObjectProvider<MyType> subject() {
		return subject;
	}

	@Override
	public ModelType<?> aKnownType() {
		return of(MyType.class);
	}

	private static void assertDoesNotChangeEntityState(ModelNode entity, Executable executable) {
		val expectedState = ModelStates.getState(entity);
		assertDoesNotThrow(executable);
		assertEquals(expectedState, ModelStates.getState(entity));
	}

	@Test
	void doesNotChangeEntityStateOnFlatMap() {
		assertDoesNotChangeEntityState(node, () -> subject.flatMap(aTransformer()));
	}

	@Test
	void realizesEntityWhenFlatMappedProviderQueried() {
		subject.flatMap(ProviderUtils::fixed).getOrNull();
		assertThat(ModelStates.getState(node), equalTo(ModelState.Realized));
	}

	@Test
	void doesNotChangeEntityStateOnMap() {
		assertDoesNotChangeEntityState(node, () -> subject.map(aTransformer()));
	}

	@Test
	void realizesEntityWhenMappedProviderQueried() {
		subject.map(it -> it).getOrNull();
		assertThat(ModelStates.getState(node), equalTo(ModelState.Realized));
	}

	@Test
	void realizesEntityWhenObjectQueried() {
		subject.get();
		assertThat(ModelStates.getState(node), equalTo(ModelState.Realized));
	}

	@Test
	void throwExceptionWhenCreatingKnownObjectWithWrongProjectionType() {
		val ex = assertThrows(IllegalArgumentException.class, () -> factory.createObject(node, of(WrongType.class)));
		assertThat(ex.getMessage(),
			equalTo("node 'qibe' cannot be viewed as interface dev.nokee.model.internal.DefaultModelObjectBackedByModelEntityIntegrationTest$WrongType"));
	}

	@Test
	void throwsNullPointerExceptionWhenProjectionTypeIsNull() {
		assertThrows(NullPointerException.class, () -> factory.createObject(node, null));
	}

	@Test
	void throwsNullPointerExceptionWhenEntityIsNull() {
		assertThrows(NullPointerException.class, () -> factory.createObject(null, of(Object.class)));
	}

	@Test
	void forwardsConfigureUsingActionAsModelActionRegistration() {
		subject.configure(ActionTestUtils.doSomething());
		verify(modelRegistry).instantiate(any());
	}

	@Test
	void forwardsConfigureUsingClosureAsModelActionRegistration() {
		subject.configure(ClosureTestUtils.doSomething(Object.class));
		verify(modelRegistry).instantiate(any());
	}

	@Test
	void returnsIdentifierFromEntity() {
		assertEquals(node.get(IdentifierComponent.class).get(), subject.getIdentifier());
	}

	@Test
	void usesFullyQualifiedNamedComponentAsConfigurableProviderName() {
		assertEquals("testQibe", subject.asProvider().getName());
	}

	@Test
	void forwardsConfigureUsingActionOnConfigurableProviderAsModelActionRegistration() {
		subject.asProvider().configure(ActionTestUtils.doSomething());
		verify(modelRegistry).instantiate(any());
	}

	@Nested
	class MixedInModelElementTest implements ModelObjectTester<MyOtherTypeMixIn> {
		private final DomainObjectProvider<MyOtherTypeMixIn> subject = DefaultModelObjectBackedByModelEntityIntegrationTest.this.subject.mixin(of(MyOtherTypeMixIn.class));

		@Override
		public DomainObjectProvider<MyOtherTypeMixIn> subject() {
			return subject;
		}

		@Override
		public ModelType<?> aKnownType() {
			return of(MyOtherTypeMixIn.class);
		}

		@Override
		public void isAutoConversionToProviderViaCallable() {
			// TODO: Provide implementation that returns a stable provider instance
		}

		@Test
		void isInstanceOfOtherProjection() {
			assertTrue(subject.instanceOf(of(MyType.class)));
		}
	}

	@Test
	void doesNotReturnTaskProviderForNonTaskEntity() {
		assertThat(subject.asProvider(), not(isA(TaskProvider.class)));
	}

	@Override
	public void isAutoConversionToProviderViaCallable() {
		// TODO: Provide implementation that returns a stable provider instance
	}

	interface MyType {}
	interface MyOtherTypeMixIn {}
	interface WrongType {}
}
