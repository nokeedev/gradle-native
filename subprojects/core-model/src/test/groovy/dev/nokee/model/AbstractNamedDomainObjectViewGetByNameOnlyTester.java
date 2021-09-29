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
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import lombok.val;
import org.gradle.api.InvalidUserDataException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AbstractNamedDomainObjectViewGetByNameOnlyTester<T> extends AbstractNamedDomainObjectViewTester<T> {
	private final NamedDomainObjectView<T> subject = createSubject();

	protected abstract DomainObjectProvider<T> get(NamedDomainObjectView<T> subject, String name);

	@Test
	void canGetProviderForKnownElement() {
		element("e0");
		val provider = get(subject, "e0");
		assertThat(provider.getIdentifier(), equalTo(ModelIdentifier.of("myTypes.e0", getElementType())));
	}

	@Test
	void canGetProjectionValueFromProvider() {
		element("e1");
		assertThat(get(subject, "e1").get(), equalTo(e("e1")));
	}

	@Test
	void doesNotRealizeNode() {
		element("e2");
		get(subject, "e2");
		assertThat(ModelNodeUtils.getState(node("e2")), lessThan(ModelNode.State.Realized));
	}

	@Test
	void canRealizeNodeViaProvider() {
		element("e3");
		get(subject, "e3").get();
		assertThat(ModelNodeUtils.getState(node("e3")), equalTo(ModelNode.State.Realized));
	}

	@Test
	void throwsExceptionWhenGettingUnknownElements() {
		try {
			get(subject, "e4");
			fail("Expecting method to throw exception");
		} catch (IllegalArgumentException ex) {
			assertThat(ex.getMessage(), equalTo("Element at 'myTypes.e4' wasn't found."));
		} catch (MissingMethodException ex) {
			assertThat(ex.getMessage(), startsWith("Could not find method e4() for arguments"));
		} catch (MissingPropertyException ex) {
			assertThat(ex.getMessage(), startsWith("Could not get unknown property 'e4' for object of type"));
		}
	}

	@Test
	void throwsExceptionWhenGettingKnownElementOfUnrelatedType() {
		element("e5", WrongType.class);
		val ex = assertThrows(InvalidUserDataException.class, () -> get(subject, "e5"));
		assertThat(ex.getMessage(), equalTo("The domain object 'e5' (interface dev.nokee.model.AbstractNamedDomainObjectViewGetByNameOnlyTester$WrongType) is not a subclass of the given type (dev.nokee.model.CustomDomainObjectContainerTypeIntegrationTest.MyType)."));
	}

	interface WrongType {}
}
