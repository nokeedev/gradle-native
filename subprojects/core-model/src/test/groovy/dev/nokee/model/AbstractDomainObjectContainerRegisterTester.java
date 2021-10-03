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

import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;

public abstract class AbstractDomainObjectContainerRegisterTester<T> extends AbstractDomainObjectContainerTester<T> {
	private final DomainObjectContainer<T> subject = createSubject();

	protected abstract DomainObjectProvider<T> register(DomainObjectContainer<T> subject, String name, Class<T> type);

	// TODO: getRegistrableManagedType()
	// TODO: getRegistrableUnmanagedType()
	// TODO: getUnregistrableType() // newspeak

	@Test
	void canRegisterManagedType() {
		val provider = register(subject, "foo", getElementType());
		assertThat(ModelStates.getState(node("foo")), equalTo(ModelState.Registered));
		assertThat(e("foo"), isA(getElementType()));

		// Realize through provider
		assertThat(provider.get(), isA(getElementType()));
		assertThat(ModelStates.getState(node("foo")), equalTo(ModelState.Realized));
	}

	// TODO: Can create element without factory that is creatable via ObjectFactory (managed)
	// TODO: Throws exception if not creatable via ObjectFactory (unmanaged)
	// TODO: Can create element with factory (unmanaged) -> support the other tests as well
	// TODO: Can bind element to a factory (unmanaged) -> support the other tests as well
	// TODO: Can create element that creates a NodeRegistration



	// TODO: Add support for Groovy DSL for registering here
}
