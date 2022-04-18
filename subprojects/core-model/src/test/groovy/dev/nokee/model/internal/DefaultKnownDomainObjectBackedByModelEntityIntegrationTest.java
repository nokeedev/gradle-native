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
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.KnownDomainObjectTester;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjections;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class DefaultKnownDomainObjectBackedByModelEntityIntegrationTest implements KnownDomainObjectTester<Object> {
	private static final Object myTypeInstance = new Object();
	private final ModelRegistry modelRegistry = Mockito.mock(ModelRegistry.class);
	private final ModelNode node = newEntity(modelRegistry);
	private final DefaultKnownDomainObject<Object> subject = DefaultKnownDomainObject.of(of(Object.class), node);

	private static ModelNode newEntity(ModelRegistry modelRegistry) {
		val entity = node("laqu", ModelProjections.ofInstance(myTypeInstance), builder -> builder.withRegistry(modelRegistry));
		entity.addComponent(new IdentifierComponent(ModelIdentifier.of("laqu", Object.class)));
		entity.addComponent(new FullyQualifiedNameComponent("testLaqu"));
		return entity;
	}

	@Override
	public KnownDomainObject<Object> subject() {
		return subject;
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
	void throwExceptionWhenCreatingKnownObjectWithWrongProjectionType() {
		val ex = assertThrows(IllegalArgumentException.class, () -> DefaultKnownDomainObject.of(of(WrongType.class), node));
		assertThat(ex.getMessage(),
			equalTo("node 'laqu' cannot be viewed as interface dev.nokee.model.internal.DefaultKnownDomainObjectBackedByModelEntityIntegrationTest$WrongType"));
	}

	@Test
	void throwsNullPointerExceptionWhenProjectionTypeIsNull() {
		assertThrows(NullPointerException.class, () -> DefaultKnownDomainObject.of(null, node));
	}

	@Test
	void throwsNullPointerExceptionWhenEntityIsNull() {
		assertThrows(NullPointerException.class, () -> DefaultKnownDomainObject.of(of(Object.class), null));
	}

	@Test
	void forwardsConfigureUsingActionAsModelActionRegistration() {
		subject.configure(ActionTestUtils.doSomething());
		verify(modelRegistry).instantiate(any());
	}

	@Test
	void forwardsConfigureUsingClosureAsModelRegistration() {
		subject.configure(ClosureTestUtils.doSomething(Object.class));
		verify(modelRegistry).instantiate(any());
	}

	@Test
	void returnsIdentifierFromEntity() {
		assertEquals(node.get(IdentifierComponent.class).get(), subject.getIdentifier());
	}

	@Test
	void usesFullyQualifiedNamedComponentAsConfigurableProviderName() {
		assertEquals("testLaqu", subject.asProvider().getName());
	}

	@Test
	void forwardsConfigureUsingActionOnConfigurableProviderAsModelActionRegistration() {
		subject.asProvider().configure(ActionTestUtils.doSomething());
		verify(modelRegistry).instantiate(any());
	}

	// TODO: If projection type is a Task, asProvider should return a TaskProvider instance

	interface WrongType {}
}
