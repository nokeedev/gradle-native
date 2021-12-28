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
import dev.nokee.utils.ActionTestUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DefaultModelElementTest implements ModelElementTester {
	private static final ModelType<MyType> KNOWN_TYPE = of(MyType.class);
	@SuppressWarnings("unchecked") private final Supplier<String> nameSupplier = Mockito.mock(Supplier.class);
	@SuppressWarnings("unchecked") private final Supplier<String> displayNameSupplier = Mockito.mock(Supplier.class);
	private final ConfigurableStrategy configurableStrategy = Mockito.mock(ConfigurableStrategy.class);
	private final InstanceOfOperatorStrategy instanceOfStrategy = Mockito.mock(InstanceOfOperatorStrategy.class);
	private final TypeCastOperatorStrategy typeCastOperatorStrategy = Mockito.mock(TypeCastOperatorStrategy.class);
	private final ModelPropertyLookupStrategy propertyLookupStrategy = Mockito.mock(ModelPropertyLookupStrategy.class);
	private final DefaultModelElement subject = new DefaultModelElement(nameSupplier, displayNameSupplier, configurableStrategy, instanceOfStrategy, typeCastOperatorStrategy, propertyLookupStrategy);

	@BeforeEach
	void mockName() {
		when(nameSupplier.get()).thenReturn("test");
		when(displayNameSupplier.get()).thenReturn("element 'test'");
		when(instanceOfStrategy.instanceOf(argThat(notA(KNOWN_TYPE)))).thenReturn(false);
		when(instanceOfStrategy.instanceOf(KNOWN_TYPE)).thenReturn(true);
		when(instanceOfStrategy.getCastableTypes()).thenAnswer(it -> Stream.of(KNOWN_TYPE));
		when(typeCastOperatorStrategy.castTo(argThat(notA(KNOWN_TYPE)))).thenThrow(ClassCastException.class);
	}

	private static <T> ArgumentMatcher<T> notA(T type) {
		return argument -> !type.equals(argument);
	}

	public ModelElement subject() {
		return subject;
	}

	@Override
	public ModelType<?> aKnownType() {
		return of(MyType.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnConstructor() {
		new NullPointerTester().testAllPublicConstructors(DefaultModelElement.class);
	}

	@Test
	void forwardsConfigureUsingActionToStrategy() {
		val action = ActionTestUtils.mockAction(MyType.class);
		subject.configure(KNOWN_TYPE, action);
		verify(configurableStrategy).configure(KNOWN_TYPE, action);
	}

	@Test
	void forwardsNameQueryToSupplier() {
		assertEquals("test", subject.getName());
		verify(nameSupplier).get();
	}

	@Test
	void forwardsDisplayNameQueryToSupplier() {
		assertEquals("element 'test'", subject.getDisplayName());
		verify(displayNameSupplier).get();
	}

	@Test
	void forwardsInstanceOfToStrategy() {
		assertAll(
			() -> {
				assertTrue(subject.instanceOf(KNOWN_TYPE));
				verify(instanceOfStrategy).instanceOf(KNOWN_TYPE);
			},
			() -> {
				assertFalse(subject.instanceOf(of(WrongType.class)));
				verify(instanceOfStrategy).instanceOf(of(WrongType.class));
			}
		);
	}

	@Test
	void forwardsKnownTypeCastToStrategy() {
		@SuppressWarnings("unchecked")
		final DomainObjectProvider<MyType> result = Mockito.mock(DomainObjectProvider.class);
		when(typeCastOperatorStrategy.castTo(KNOWN_TYPE)).thenReturn(result);
		assertEquals(result, subject.as(KNOWN_TYPE));
		verify(typeCastOperatorStrategy).castTo(KNOWN_TYPE);
	}

	@Test
	void forwardsPropertyQueryToStrategy() {
		val result = Mockito.mock(ModelElement.class);
		when(propertyLookupStrategy.get(any())).thenReturn(result);
		assertEquals(result, subject.property("ripa"));
		verify(propertyLookupStrategy).get("ripa");
	}

	@Test
	void throwsExceptionWhenCastingUsingGroovyTypeCastOperator() {
		val ex = assertThrows(UnsupportedOperationException.class, () -> subject.asType(MyType.class));
		assertEquals("Use ModelElement#as(ModelType) instead.", ex.getMessage());
	}

	@Test
	void includesDisplayNameInTypeCastException() {
		when(displayNameSupplier.get()).thenReturn("some element named 'test'");
		val ex = assertThrows(ClassCastException.class, () -> subject.as(of(WrongType.class)));
		assertEquals("Could not cast some element named 'test' to WrongType. Available instances: MyType.", ex.getMessage());
	}

	public interface MyType {}
	public interface WrongType {}
}
