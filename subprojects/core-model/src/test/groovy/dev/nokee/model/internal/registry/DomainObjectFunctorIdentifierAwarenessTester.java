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
package dev.nokee.model.internal.registry;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelIdentifier;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class DomainObjectFunctorIdentifierAwarenessTester<F> extends AbstractDomainObjectFunctorTester<F> {
	@Override
	protected <T> F createSubject(Class<T> type) {
		return createSubject(type, node("foo", new MyType(), new MyOtherType()));
	}

	protected DomainObjectIdentifier getIdentifier(F functor) {
		return invoke(functor, "getIdentifier", new Class[0], new Object[0]);
	}

	@Test
	void canQueryProviderIdentifier() {
		assertThat(getIdentifier(createSubject(MyType.class)), equalTo(ModelIdentifier.of("foo", MyType.class)));
		assertThat(getIdentifier(createSubject(MyOtherType.class)), equalTo(ModelIdentifier.of("foo", MyOtherType.class)));
	}
}
