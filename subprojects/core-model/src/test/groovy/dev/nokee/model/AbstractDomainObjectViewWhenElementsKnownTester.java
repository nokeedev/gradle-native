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

public abstract class AbstractDomainObjectViewWhenElementsKnownTester<T> extends AbstractDomainObjectViewConfigureEachTester<T> {
	private final DomainObjectView<T> subject = createSubject();

	protected abstract void whenElementKnown(DomainObjectView<T> subject, Consumer<? super KnownDomainObject<T>> action);

	private ArgumentCaptor<KnownDomainObject<T>> whenElementKnown(DomainObjectView<T> subject) {
		Consumer<KnownDomainObject<T>> action = Cast.uncheckedCast(mock(Consumer.class));
		ArgumentCaptor<KnownDomainObject<T>> captor = Cast.uncheckedCast(ArgumentCaptor.forClass(KnownDomainObject.class));
		doNothing().when(action).accept(captor.capture());

		whenElementKnown(subject, action);
		return captor;
	}

	@Test
	void doesNotCallWhenElementKnownWhenEmpty() {
		assertThat("when element known action is not called when view is empty",
			whenElementKnown(subject).getAllValues(), empty());
	}

	@Test
	void callsWhenElementKnownWhenOnlyUnrealizedElements() {
		elements("e1", "e2");
		val captor = whenElementKnown(subject);
		elements("e3", "e4");
		assertThat("when element known action is called for unrealized elements",
			captor.getAllValues(),
			contains(known("e1"), known("e2"), known("e3"), known("e4")));
	}

	@Test
	void callsWhenElementKnownOnlyWhenElementsAreFirstDiscovered() {
		val captor = when(() -> whenElementKnown(subject));
		assertThat("calls back for realized and unrealized view elements, e.g. e1 to e7",
			captor.getAllValues(),
			contains(
				known("e0"), known("e1"), known("e2"), known("e3"),
				known("e4"), known("e5"), known("e6"), known("e7")));
	}

	@Test
	void doesNotCallWhenElementKnownForUnrelatedTypes() {
		element("e0", getElementType());
		element("e1", getSubElementType());
		element("e3", WrongType.class);
		val captor = whenElementKnown(subject);
		assertThat("element of unrelated types are not configured",
			captor.getAllValues(),
			contains(known("e0"), known("e1")));
	}

	@Test
	void doesNotCallWhenElementKnownForUnrelatedPaths() {
		element("e0", getElementType());
		element("e1", getSubElementType());

		// Create same type element in same nesting level
		createSubject("myOtherView");
		register("myOtherView.e0", getElementType());
		register("myOtherView.e1", getElementType());

		val captor = whenElementKnown(subject);
		assertThat("element of unrelated types are not known",
			captor.getAllValues(),
			contains(known("e0"), known("e1")));
	}

	@Test
	void doesNotCallWhenElementKnownForNestedElements() {
		element("e0", getElementType());
		element("e1", getSubElementType());

		// Create same type element nested under view elements
		register("myTypes.e0.e3", getElementType());
		register("myTypes.e1.e4", getElementType());

		val captor = whenElementKnown(subject);
		assertThat("element of unrelated types are not known",
			captor.getAllValues(),
			contains(known("e0"), known("e1")));
	}

	interface WrongType {}
}
