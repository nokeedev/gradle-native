/*
 * Copyright 2020 the original author or authors.
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

import lombok.val;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewElementProcessTester<T> extends AbstractDomainObjectViewTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract Provider<? extends Iterable<?>> process(DomainObjectView<T> subject, Consumer<? super T> captor);

	private static <T> Consumer<T> noProcessingOfElements() {
		return t -> { throw new UnsupportedOperationException(); };
	}

	private ArgumentCaptor<T> capturesAllElements(Consumer<Consumer<? super T>> action) {
		Consumer<T> capturingAction = Cast.uncheckedCast(mock(Consumer.class));
		val captor = ArgumentCaptor.forClass(getElementType());
		Mockito.doNothing().when(capturingAction).accept(captor.capture());
		action.accept(capturingAction);
		return captor;
	}

	@BeforeEach
	void addElements() {
		elements("e1", "e2");
	}

	// Processing of element assertion
	@Test
	void doesNotEagerlyProcessElements() {
		process(subject, noProcessingOfElements());
	}

	@Test
	void callsTransformationForWithEachElements() {
		val captor = capturesAllElements(action -> {
			process(subject, action).get();
		});
		assertThat("invoke mapper transformer with each registered elements",
			captor.getAllValues(), contains(e("e1"), e("e2")));
	}

	@Test
	void wrongElementTypesAreNotTransformed() {
		element("foo", WrongType.class);
		val captor = capturesAllElements(action -> {
			process(subject, action).get();
		});
		assertThat("unscoped elements should not be mapped",
			(Iterable<Object>)captor.getAllValues(), not(hasItem(e("foo", WrongType.class)))); // TODO: Remove cast
	}

	interface WrongType {}
}
