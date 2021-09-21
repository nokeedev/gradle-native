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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewConfigureEachByTypeTester<T> extends AbstractDomainObjectViewConfigureEachTester<T> {
	private final DomainObjectView<T> subject = createSubject();
	// TODO: configureEach(type, Action)
	// TODO: configureEach(type::isInstance, Action)

	protected abstract <S extends T> void configureEach(DomainObjectView<T> subject, Class<S> type, Consumer<? super S> action);

	private <S extends T> ArgumentCaptor<S> configureEach(DomainObjectView<T> subject, Class<S> type) {
		@SuppressWarnings("unchecked")
		Consumer<S> action = (Consumer<S>) mock(Consumer.class);
		val captor = ArgumentCaptor.forClass(type);
		doNothing().when(action).accept(captor.capture());

		configureEach(subject, type, action);
		return captor;
	}

	@Test
	void callsConfigureEachByType() {
		val captor = when(() -> configureEach(subject, getSubElementType()));
		assertThat("can configure elements matching a type, e.g. e3 and e7",
			captor.getAllValues(), contains(e("e3"), e("e7")));
	}
}
