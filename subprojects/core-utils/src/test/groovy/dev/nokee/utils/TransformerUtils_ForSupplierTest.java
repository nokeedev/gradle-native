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

import com.google.common.base.Suppliers;
import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.utils.TransformerUtils.forSupplier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransformerUtils_ForSupplierTest {
	@Test
	void alwaysCallSupplier() {
		@SuppressWarnings("unchecked")
		val supplier = (Supplier<Object>) mock(Supplier.class);
		val subject = forSupplier(supplier);
		subject.transform(new Object());
		subject.transform(new Object());
		verify(supplier, times(2)).get();
	}

	@Test
	void ignoresInputValue() {
		assertThat(forSupplier(ofInstance(42)).transform(55), equalTo(42));
	}

	@Test
	void acceptNullInputValues() {
		val result = assertDoesNotThrow(() -> forSupplier(ofInstance(52)).transform(null));
		assertThat(result, equalTo(52));
	}

	@Test
	void canUseForSupplierWithLambda() {
		Transformer<String, Object> transformer1 = forSupplier(() -> "foo");
		Transformer<String, ?> transformer2 = forSupplier(() -> "foo");

		assertThat(transformer1.transform(null), equalTo("foo"));
		assertThat(transformer2.transform(null), equalTo("foo"));
	}

	@Test
	void canUseForSupplierWithSupplierInstance() {
		Supplier<? extends String> supplier3 = () -> "foo";
		Transformer<String, ?> transformer3 = forSupplier(supplier3);

		Supplier<String> supplier4 = () -> "foo";
		Transformer<String, ?> transformer4 = forSupplier(supplier4);

		assertThat(transformer3.transform(null), equalTo("foo"));
		assertThat(transformer4.transform(null), equalTo("foo"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(forSupplier(ofInstance(42)), forSupplier(ofInstance(42)))
			.addEqualityGroup(forSupplier(ofInstance(52)))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(forSupplier(ofInstance(42)),
			hasToString("TransformerUtils.forSupplier(Suppliers.ofInstance(42))"));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(forSupplier(ofInstance(42)), isA(TransformerUtils.Transformer.class));
	}

	@Test
	void canSerialize() {
		assertThat(forSupplier(ofInstance("some-value")), isSerializable());
	}
}
