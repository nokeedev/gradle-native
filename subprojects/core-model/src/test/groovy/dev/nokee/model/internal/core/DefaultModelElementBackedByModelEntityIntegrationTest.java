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

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.ModelElementFactory;
import dev.nokee.model.internal.ModelObjectTester;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class DefaultModelElementBackedByModelEntityIntegrationTest implements ModelElementTester {
	private static final MyType myTypeInstance = Mockito.mock(MyType.class);
	private final ModelRegistry modelRegistry = Mockito.mock(ModelRegistry.class);
	private final ModelNode entity = newEntity(modelRegistry);
	private final ModelElementFactory factory = new ModelElementFactory(objectFactory()::newInstance);
	private final ModelElement subject = factory.createElement(entity);

	static ModelNode newEntity(ModelRegistry modelRegistry) {
		val entity = node("foru", ModelProjections.createdUsing(of(MyType.class), () -> myTypeInstance), builder -> builder.withRegistry(modelRegistry));
		entity.addComponent(new ElementNameComponent("tare"));
		entity.addComponent(new IdentifierComponent(Mockito.mock(DomainObjectIdentifier.class)));
		entity.addComponent(new DisplayNameComponent("entity 'foru.tare'"));
		return entity;
	}

	@Override
	public ModelElement subject() {
		return subject;
	}

	@Override
	public ModelType<?> aKnownType() {
		return of(MyType.class);
	}

	@Test
	void throwsExceptionIfEntityIsNull() {
		assertThrows(NullPointerException.class, () -> factory.createElement(null));
	}

	@Test
	void usesElementNameComponentAsName() {
		assertEquals("tare", subject.getName());
	}

	@Test
	void forwardsConfigureUsingActionOfKnownTypeAsModelActionRegistration() {
		subject.configure(of(MyType.class), doSomething());
		verify(modelRegistry).instantiate(any());
	}

	@Test
	void canAccessUnderlyingEntity() {
		assertSame(entity, ModelNodes.of(subject));
	}

	@Test
	void includesDisplayNameInTypeCastException() {
		val ex = assertThrows(ClassCastException.class, () -> subject.as(of(WrongType.class)));
		assertEquals("Could not cast entity 'foru.tare' to WrongType. Available instances: MyType.", ex.getMessage());
	}

	@Nested
	class MixedInModelElementTest implements ModelObjectTester<MyOtherTypeMixIn> {
		private final DomainObjectProvider<MyOtherTypeMixIn> subject = DefaultModelElementBackedByModelEntityIntegrationTest.this.subject.mixin(of(MyOtherTypeMixIn.class));

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

	@Nested
	class CastedModelElementTest implements ModelObjectTester<MyType> {
		private final DomainObjectProvider<MyType> subject = DefaultModelElementBackedByModelEntityIntegrationTest.this.subject.as(of(MyType.class));

		@Override
		public DomainObjectProvider<MyType> subject() {
			return subject;
		}

		@Override
		public ModelType<?> aKnownType() {
			return of(MyType.class);
		}

		@Test
		void doesNotReturnTaskProviderForNonTaskEntity() {
			assertThat(subject.asProvider(), not(isA(TaskProvider.class)));
		}

		@Override
		public void isAutoConversionToProviderViaCallable() {
			// TODO: Provide implementation that returns a stable provider instance
		}
	}

	interface MyType {}
	interface MyOtherTypeMixIn {}
}
