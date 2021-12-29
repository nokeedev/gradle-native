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
package dev.nokee.model.internal.core;

import com.google.common.testing.NullPointerTester;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ConfigurableStrategy;
import dev.nokee.model.internal.InstanceOfOperatorStrategy;
import dev.nokee.model.internal.TypeCastOperatorStrategy;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Supplier;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultModelElementTest {
	@SuppressWarnings("unchecked") private final Supplier<String> nameSupplier = Mockito.mock(Supplier.class);
	private final ConfigurableStrategy configurableStrategy = Mockito.mock(ConfigurableStrategy.class);
	private final InstanceOfOperatorStrategy instanceOfStrategy = Mockito.mock(InstanceOfOperatorStrategy.class);
	private final TypeCastOperatorStrategy typeCastOperatorStrategy = Mockito.mock(TypeCastOperatorStrategy.class);
	private final ModelPropertyLookupStrategy propertyLookupStrategy = Mockito.mock(ModelPropertyLookupStrategy.class);
	private final DefaultModelElement subject = new DefaultModelElement(nameSupplier, configurableStrategy, instanceOfStrategy, typeCastOperatorStrategy, propertyLookupStrategy);

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnPublicMethods() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.untyped()).testAllPublicInstanceMethods(subject);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnConstructor() {
		new NullPointerTester().testAllPublicConstructors(DefaultModelElement.class);
	}

	@Test
	void forwardsConfigureUsingModelTypeAndActionToStrategy() {
		subject.configure(of(MyType.class), doSomething());
		verify(configurableStrategy).configure(of(MyType.class), doSomething());
	}

	@Test
	void forwardsNameQueryToSupplier() {
		val expectedName = "zalu";
		when(nameSupplier.get()).thenReturn(expectedName);

		assertEquals(expectedName, subject.getName());
		verify(nameSupplier).get();
	}

	@Test
	void forwardsInstanceOfToStrategy() {
		when(instanceOfStrategy.instanceOf(of(MyType.class))).thenReturn(Boolean.TRUE);
		when(instanceOfStrategy.instanceOf(of(WrongType.class))).thenReturn(Boolean.FALSE);
		assertAll(
			() -> {
				assertTrue(subject.instanceOf(of(MyType.class)));
				verify(instanceOfStrategy).instanceOf(of(MyType.class));
			},
			() -> {
				assertFalse(subject.instanceOf(of(WrongType.class)));
				verify(instanceOfStrategy).instanceOf(of(WrongType.class));
			}
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsTypeCastOperatorUsingModelTypeToStrategy() {
		val result = Mockito.mock(DomainObjectProvider.class);
		when(typeCastOperatorStrategy.castTo(any())).thenReturn(result);

		assertSame(result, subject.as(of(MyType.class)));
		verify(typeCastOperatorStrategy).castTo(of(MyType.class));
	}

	@Test
	void forwardsPropertyQueryToStrategy() {
		val result = Mockito.mock(ModelElement.class);
		when(propertyLookupStrategy.get(any())).thenReturn(result);

		assertSame(result, subject.property("ripa"));
		verify(propertyLookupStrategy).get("ripa");
	}

	@Test
	void throwsExceptionWhenCastingUsingGroovyTypeCastOperator() {
		val ex = assertThrows(UnsupportedOperationException.class, () -> subject.asType(MyType.class));
		assertEquals("Use ModelElement#as(ModelType) instead.", ex.getMessage());
	}

	public interface MyType {}
	public interface WrongType {}
}
