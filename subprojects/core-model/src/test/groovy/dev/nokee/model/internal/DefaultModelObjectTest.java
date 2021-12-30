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
package dev.nokee.model.internal;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelCastableStrategy;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPropertyLookupStrategy;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.ClosureTestUtils.doSomething;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultModelObjectTest {
	@SuppressWarnings("unchecked") private final Supplier<String> nameSupplier = mock(Supplier.class);
	@SuppressWarnings("unchecked") private final Supplier<DomainObjectIdentifier> identifierSupplier = mock(Supplier.class);
	private final ConfigurableProviderConvertibleStrategy providerConvertibleStrategy = mock(ConfigurableProviderConvertibleStrategy.class);
	private final ConfigurableStrategy configurableStrategy = mock(ConfigurableStrategy.class);
	private final ModelCastableStrategy castableStrategy = mock(ModelCastableStrategy.class);
	private final ModelPropertyLookupStrategy propertyLookupStrategy = mock(ModelPropertyLookupStrategy.class);
	@SuppressWarnings("unchecked") private final Supplier<MyType> valueSupplier = mock(Supplier.class);
	@SuppressWarnings("unchecked") private final Supplier<ModelNode> entitySupplier = mock(Supplier.class);
	private final DefaultModelObject<MyType> subject = new DefaultModelObject<>(nameSupplier, identifierSupplier, of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier);

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnConstructor() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.untyped()).testAllPublicConstructors(DefaultModelObject.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnPublicMethods() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.untyped()).testAllPublicInstanceMethods(subject);
	}

	//region KnownDomainObject
	@Test
	void forwardsIdentifierQueryToDelegate() {
		val expectedIdentifier = mock(DomainObjectIdentifier.class);
		when(identifierSupplier.get()).thenReturn(expectedIdentifier);

		assertSame(expectedIdentifier, subject.getIdentifier());
		verify(identifierSupplier).get();
	}

	@Test
	void returnsSpecifiedType() {
		assertSame(MyType.class, subject.getType());
	}

	@Test
	void forwardsConfigureUsingActionToStrategy() {
		subject.configure(doSomething());
		verify(configurableStrategy).configure(of(MyType.class), doSomething());
	}

	@Test
	void forwardsConfigureUsingClosureToStrategyAsAction() {
		subject.configure(doSomething(MyType.class));
		verify(configurableStrategy).configure(of(MyType.class), new ClosureWrappedConfigureAction<>(doSomething(MyType.class)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsMapToProviderConvertibleValue() {
		val result = mock(Provider.class);
		val provider = mock(NamedDomainObjectProvider.class);
		when(providerConvertibleStrategy.asProvider(any())).thenReturn(provider);
		when(provider.map(any())).thenReturn(result);

		assertEquals(result, subject.map(aTransformer()));
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
		verify(provider).map(aTransformer());
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsFlatMapToProviderConvertibleValue() {
		val result = mock(Provider.class);
		val provider = mock(NamedDomainObjectProvider.class);
		when(providerConvertibleStrategy.asProvider(any())).thenReturn(provider);
		when(provider.flatMap(any())).thenReturn(result);

		assertEquals(result, subject.flatMap(aTransformer()));
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
		verify(provider).flatMap(aTransformer());
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsAsProviderToStrategy() {
		val result = mock(NamedDomainObjectProvider.class);
		when(providerConvertibleStrategy.asProvider(any())).thenReturn(result);

		assertEquals(result, subject.asProvider());
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
	}
	//endregion

	//region ModelElement
	@Test
	void forwardsNameQueryToSupplier() {
		val expectedName = "taxe";
		when(nameSupplier.get()).thenReturn(expectedName);

		assertSame(expectedName, subject.getName());
		verify(nameSupplier).get();
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsTypeCastOperatorUsingModelTypeToDelegate() {
		val expectedObject = mock(DomainObjectProvider.class);
		when(castableStrategy.castTo(any())).thenReturn(expectedObject);

		assertSame(expectedObject, subject.as(of(MyType.class)));
		verify(castableStrategy).castTo(of(MyType.class));
	}

	@Test
	void forwardsInstanceOfOperatorUsingModelTypeToStrategy() {
		when(castableStrategy.instanceOf(of(MyType.class))).thenReturn(Boolean.TRUE);
		when(castableStrategy.instanceOf(of(WrongType.class))).thenReturn(Boolean.FALSE);
		assertAll(
			() -> {
				assertTrue(subject.instanceOf(of(MyType.class)));
				verify(castableStrategy).instanceOf(of(MyType.class));
			},
			() -> {
				assertFalse(subject.instanceOf(of(WrongType.class)));
				verify(castableStrategy).instanceOf(of(WrongType.class));
			}
		);
	}

	@Test
	void forwardsPropertyQueryToStrategy() {
		val result = mock(ModelElement.class);
		when(propertyLookupStrategy.get(any())).thenReturn(result);

		assertEquals(result, subject.property("dumo"));
		verify(propertyLookupStrategy).get("dumo");
	}

	@Test
	void forwardsConfigureUsingModelTypeAndActionToStrategy() {
		assertSame(subject, subject.configure(of(MyType.class), doSomething()));
		verify(configurableStrategy).configure(of(MyType.class), doSomething());
	}
	//endregion

	@Test
	void throwsExceptionWhenCastingUsingGroovyTypeCastOperator() {
		val ex = assertThrows(UnsupportedOperationException.class, () -> subject.asType(MyType.class));
		assertEquals("Use ModelObject#as(ModelType) instead.", ex.getMessage());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val identifier = mock(DomainObjectIdentifier.class);
		new EqualsTester()
			.addEqualityGroup(
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier),
				new DefaultModelObject<>(mock(Supplier.class), ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier),
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), mock(ConfigurableProviderConvertibleStrategy.class), configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier),
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, mock(ConfigurableStrategy.class), castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier),
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, mock(ModelCastableStrategy.class), propertyLookupStrategy, valueSupplier, entitySupplier),
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, mock(ModelPropertyLookupStrategy.class), valueSupplier, entitySupplier),
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, mock(Supplier.class), entitySupplier),
				new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, mock(Supplier.class))
			)
			.addEqualityGroup(new DefaultModelObject<>(nameSupplier, ofInstance(identifier), of(Object.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier))
			.addEqualityGroup(new DefaultModelObject<>(nameSupplier, ofInstance(mock(DomainObjectIdentifier.class)), of(MyType.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier))
			.addEqualityGroup(new DefaultModelObject<>(nameSupplier, ofInstance(mock(DomainObjectIdentifier.class)), of(Object.class), providerConvertibleStrategy, configurableStrategy, castableStrategy, propertyLookupStrategy, valueSupplier, entitySupplier))
			.testEquals();
	}

	interface MyType {}
	interface WrongType {}
}
