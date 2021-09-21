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

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class DomainObjectFunctorIntegrityTester<F> extends AbstractDomainObjectFunctorTester<F> {
	@Override
	protected <T> F createSubject(Class<T> type) {
		throw new UnsupportedOperationException();
	}

	protected abstract Class<F> getFunctorType();

	@Test
	void throwExceptionWhenCreatingProviderWithWrongProjectionType() {
		val ex = assertThrows(IllegalArgumentException.class, () -> createSubject(WrongType.class, node("foo", new MyType())));
		assertThat(ex.getMessage(),
			equalTo("node 'foo' cannot be viewed as interface dev.nokee.model.internal.registry.AbstractDomainObjectFunctorTester$WrongType"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester()
			.setDefault(ModelType.class, of(MyType.class))
			.setDefault(ModelNode.class, node(new MyType()))
			.testAllPublicConstructors(getFunctorType());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val node = node("foo", new MyType(), new MyOtherType());
		new EqualsTester()
			.addEqualityGroup(createSubject(MyType.class, node), createSubject(MyType.class, node))
			.addEqualityGroup(createSubject(MyOtherType.class, node))
			.addEqualityGroup(createSubject(MyType.class, node("bar", MyType.class)))
			.testEquals();
	}
}
