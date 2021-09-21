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
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractDomainObjectViewConfigureEachElementsTester<T> extends AbstractDomainObjectViewConfigureEachTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract void configureEach(DomainObjectView<T> subject, Consumer<? super T> action);

	private ArgumentCaptor<T> configureEach(DomainObjectView<T> subject) {
		Consumer<T> action = Cast.uncheckedCast(mock(Consumer.class));
		val captor = ArgumentCaptor.forClass(getElementType());
		doNothing().when(action).accept(captor.capture());

		configureEach(subject, action);
		return captor;
	}

	@Test
	void doesNotCallConfigureEachWhenEmpty() {
		assertThat("configure action is not called when view is empty",
			configureEach(subject).getAllValues(), empty());
	}

	@Test
	void doesNotCallConfigureEachWhenOnlyUnrealizedElements() {
		elements("e1", "e2");
		val captor = configureEach(subject);
		elements("e3", "e4");
		assertThat("configure action is not called for unrealized elements",
			captor.getAllValues(), empty());
	}

	@Test
	void callsConfigureEach() {
		val captor = when(() -> configureEach(subject));
		assertThat("can configure realized view elements, e.g. e1, e3, e5 and e7",
			captor.getAllValues(), contains(e("e1"), e("e3"), e("e5"), e("e7")));
	}

	@Test
	void doesNotCallConfigureEachForUnrelatedTypes() {
		element("e0", getElementType()).get();
		element("e1", getSubElementType()).get();
		element("e3", WrongType.class).get();
		val captor = configureEach(subject);
		assertThat("element of unrelated types are not configured",
			captor.getAllValues(), contains(e("e0"), e("e1")));
	}

	@Test
	void doesNotCallConfigureEachForUnrelatedPaths() {
		element("e0", getElementType()).get();
		element("e1", getSubElementType()).get();

		// Create same type element in same nesting level
		createSubject("myOtherView");
		register("myOtherView.e0", getElementType()).get();
		register("myOtherView.e1", getElementType()).get();

		val captor = configureEach(subject);
		assertThat("element of unrelated types are not configured",
			captor.getAllValues(), contains(e("e0"), e("e1")));
	}

	@Test
	void doesNotCallConfigureEachForNestedElements() {
		element("e0", getElementType()).get();
		element("e1", getSubElementType()).get();

		// Create same type element nested under view elements
		register("myTypes.e0.e3", getElementType()).get();
		register("myTypes.e1.e4", getElementType()).get();

		val captor = configureEach(subject);
		assertThat("element of unrelated types are not configured",
			captor.getAllValues(), contains(e("e0"), e("e1")));
	}

	interface WrongType {}
}
