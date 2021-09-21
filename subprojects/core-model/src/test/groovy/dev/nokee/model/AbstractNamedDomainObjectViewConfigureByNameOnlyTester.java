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

import dev.nokee.model.internal.core.ModelNode;
import groovy.lang.MissingMethodException;
import lombok.val;
import org.gradle.api.InvalidUserDataException;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

public abstract class AbstractNamedDomainObjectViewConfigureByNameOnlyTester<T> extends AbstractNamedDomainObjectViewTester<T> {
	private final NamedDomainObjectView<T> subject = createSubject();

	protected abstract void configure(NamedDomainObjectView<T> subject, String name, Consumer<? super T> action);

	private ArgumentCaptor<T> configure(NamedDomainObjectView<T> subject, String name) {
		Consumer<T> action = Cast.uncheckedCast(mock(Consumer.class));
		val captor = ArgumentCaptor.forClass(getElementType());
		doNothing().when(action).accept(captor.capture());

		configure(subject, name, action);
		return captor;
	}

	@Test
	void doesNotCallConfigureActionWhenNotRealized() {
		element("e0");
		val captor = configure(subject, "e0");
		assertThat(captor.getAllValues(), empty());
	}

	@Test
	void callConfigureActionWhenRealized() {
		element("e1").get();
		val captor = configure(subject, "e1");
		assertThat(captor.getAllValues(), contains(e("e1")));
	}

	@Test
	void configureDoesNotRealizeNode() {
		element("e2");
		configure(subject, "e2");
		assertThat(node("e2").getState(), lessThan(ModelNode.State.Realized));
	}

	@Test
	void throwsExceptionWhenConfiguringUnknownElement() {
		try {
			configure(subject, "e3");
			fail("Expecting method to throw exception");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), equalTo("Element at 'myTypes.e3' wasn't found."));
		} catch (MissingMethodException ex) {
			assertThat(ex.getMessage(), startsWith("Could not find method e3() for arguments"));
		}
	}

	@Test
	void throwsExceptionWhenConfiguringKnownElementOfUnrelatedType() {
		element("e4", WrongType.class);
		val ex = assertThrows(InvalidUserDataException.class, () -> configure(subject, "e4"));
		assertThat(ex.getMessage(), equalTo("The domain object 'e4' (interface dev.nokee.model.AbstractNamedDomainObjectViewConfigureByNameOnlyTester$WrongType) is not a subclass of the given type (dev.nokee.model.CustomDomainObjectContainerTypeIntegrationTest.MyType)."));
	}

	interface WrongType {}
}
