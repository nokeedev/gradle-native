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

import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractNamedDomainObjectViewGetByNameTypedTester<T> extends AbstractNamedDomainObjectViewTester<T> {
	private final NamedDomainObjectView<T> subject = createSubject();

	protected abstract <S extends T> DomainObjectProvider<S> get(NamedDomainObjectView<T> subject, String name, Class<S> type);

	@Test
	void canGetProviderForKnownElementUsingBaseType() {
		element("e0", getSubElementType());
		val provider = get(subject, "e0", getElementType());
		assertThat(provider.getIdentifier(), equalTo(ModelIdentifier.of("myTypes.e0", getSubElementType())));
	}

	@Test
	void canGetProviderForKnownElementUsingExactType() {
		element("e1", getSubElementType());
		val provider = get(subject, "e1", getSubElementType());
		assertThat(provider.getIdentifier(), equalTo(ModelIdentifier.of("myTypes.e1", getSubElementType())));
	}

	@Test
	void canGetProjectionValueFromProvider() {
		element("e1", getSubElementType());
		assertThat(get(subject, "e1", getSubElementType()).get(), equalTo(e("e1")));
	}

	@Test
	void doesNotRealizeNode() {
		element("e2", getSubElementType());
		get(subject, "e2", getSubElementType());
		assertThat(ModelStates.getState(node("e2")), lessThan(ModelState.Realized));
	}

	@Test
	void canRealizeNodeViaProvider() {
		element("e3", getSubElementType());
		get(subject, "e3", getSubElementType()).get();
		assertThat(ModelStates.getState(node("e3")), equalTo(ModelState.Realized));
	}

	@Test
	void throwsExceptionWhenGettingUnknownElements() {
		val ex = assertThrows(IllegalArgumentException.class, () -> get(subject, "e4", getSubElementType()));
		assertThat(ex.getMessage(), equalTo("Element at 'myTypes.e4' wasn't found."));
	}

//	@Test
//	void throwsExceptionWhenGettingKnownElementOfUnrelatedType() {
//		element("e5", WrongType.class);
//		val ex = assertThrows(InvalidUserDataException.class, () -> get(subject, "e5", getSubElementType()));
//		assertThat(ex.getMessage(), equalTo("The domain object 'e5' (interface dev.nokee.model.AbstractNamedDomainObjectViewGetByNameTypedTester$WrongType) is not a subclass of the given type (dev.nokee.model.CustomDomainObjectContainerTypeIntegrationTest.MyChildType)."));
//	}
//
//	interface WrongType {}
}
