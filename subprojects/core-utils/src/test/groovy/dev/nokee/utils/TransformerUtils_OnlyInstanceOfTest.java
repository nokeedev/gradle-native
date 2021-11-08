/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static dev.nokee.utils.TransformerUtils.onlyInstanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

final class TransformerUtils_OnlyInstanceOfTest {
	@Test
	void returnsEmptyIterableWhenElementIsNotAnInstanceOfSpecifiedType() {
		val obj = Mockito.mock(MyOtherType.class);
		assertThat(onlyInstanceOf(MyType.class).transform(obj), emptyIterable());
		assertThat(onlyInstanceOf(MyType.class, noOpTransformer()).transform(obj), emptyIterable());
	}

	@Test
	void returnsSingletonIterableWhenElementIsAnInstanceOfSpecifiedType() {
		val obj = Mockito.mock(MyType.class);
		assertThat(onlyInstanceOf(MyType.class).transform(obj), contains(obj));
		assertThat(onlyInstanceOf(MyType.class, noOpTransformer()).transform(obj), contains(obj));
	}

	@Test
	void canTransformMatchingElements() {
		val obj = Mockito.mock(MyType.class);
		val transformer = TransformerTestUtils.mockTransformer().whenCalled(noOpTransformer());
		onlyInstanceOf(MyType.class, transformer).transform(obj);
		assertThat(transformer, calledOnceWith(singleArgumentOf(obj)));
	}

	@Test
	void doesNotTransformNonMatchingElements() {
		val obj = Mockito.mock(MyOtherType.class);
		val transformer = TransformerTestUtils.mockTransformer().whenCalled(noOpTransformer());
		onlyInstanceOf(MyType.class, transformer).transform(obj);
		assertThat(transformer, neverCalled());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(onlyInstanceOf(MyType.class, aTransformer()), onlyInstanceOf(MyType.class, aTransformer()))
			.addEqualityGroup(onlyInstanceOf(MyType.class, anotherTransformer()))
			.addEqualityGroup(onlyInstanceOf(MyOtherType.class, aTransformer()))
			.testEquals();
	}

	private interface MyType {}
	private interface MyOtherType {}
}
