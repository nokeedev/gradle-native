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
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import dev.nokee.platform.base.View;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.SpecTestUtils;
import dev.nokee.utils.TransformerTestUtils;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public interface ViewDelegateTester<T> {
	View<T> subject();

	View<T> delegate();

	default Class<T> elementType() {
		return (Class<T>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	Class<? extends T> subElementType();

	@Test
	default void forwardsConfigureEachActionToDelegate() {
		val action = ActionTestUtils.doSomething();
		subject().configureEach(action);
		verify(delegate()).configureEach(action);
	}

	@Test
	default void forwardsConfigureEachClosureToDelegate() {
		val closure = ClosureTestUtils.doSomething(elementType());
		subject().configureEach(closure);
		verify(delegate()).configureEach(closure);
	}

	@Test
	default void forwardsConfigureEachActionByTypeToDelegate() {
		val action = ActionTestUtils.doSomething();
		subject().configureEach(subElementType(), action);
		verify(delegate()).configureEach(subElementType(), action);
	}

	@Test
	default void forwardsConfigureEachClosureByTypeToDelegate() {
		val closure = ClosureTestUtils.doSomething(subElementType());
		subject().configureEach(subElementType(), closure);
		verify(delegate()).configureEach(subElementType(), closure);
	}

	@Test
	default void forwardsConfigureEachActionBySpecToDelegate() {
		val action = ActionTestUtils.doSomething();
		val spec = SpecTestUtils.aSpec();
		subject().configureEach(spec, action);
		verify(delegate()).configureEach(spec, action);
	}

	@Test
	default void forwardsConfigureEachClosureBySpecToDelegate() {
		val closure = ClosureTestUtils.doSomething(elementType());
		val spec = SpecTestUtils.aSpec();
		subject().configureEach(spec, closure);
		verify(delegate()).configureEach(spec, closure);
	}

	@Test
	default void forwardsWithTypeToDelegate() {
		when(subject().withType(subElementType())).thenReturn(mock(View.class));
		assertNotNull(subject().withType(subElementType()));
		verify(delegate()).withType(subElementType());
	}

	@Test
	default void forwardsGetElementsToDelegate() {
		@SuppressWarnings("unchecked") val result = (Provider<Set<T>>) mock(Provider.class);
		when(delegate().getElements()).thenReturn(result);
		assertEquals(result, subject().getElements());
		verify(delegate()).getElements();
	}

	@Test
	default void forwardsGetToDelegate() {
		val result = ImmutableSet.of(mock(elementType()), mock(subElementType()));
		when(delegate().get()).thenReturn(result);
		assertEquals(result, subject().get());
		verify(delegate()).get();
	}

	@Test
	default void forwardsMapToDelegate() {
		val mapper = TransformerTestUtils.aTransformer();
		@SuppressWarnings("unchecked") val result = (Provider<List<Object>>) mock(Provider.class);
		when(delegate().map(mapper)).thenReturn(result);
		assertEquals(result, subject().map(mapper));
		verify(delegate()).map(mapper);
	}

	@Test
	default void forwardsFlatMapToDelegate() {
		val mapper = TransformerTestUtils.<Iterable<T>, T>aTransformer();
		@SuppressWarnings("unchecked") val result = (Provider<List<T>>) mock(Provider.class);
		when(delegate().flatMap(mapper)).thenReturn(result);
		assertEquals(result, subject().flatMap(mapper));
		verify(delegate()).flatMap(mapper);
	}

	@Test
	default void forwardsFilterToDelegate() {
		val spec = SpecTestUtils.aSpec();
		@SuppressWarnings("unchecked") val result = (Provider<List<T>>) mock(Provider.class);
		when(delegate().filter(spec)).thenReturn(result);
		assertEquals(result, subject().filter(spec));
		verify(delegate()).filter(spec);
	}
}
