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
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ActionTestUtils.doSomething;
import static dev.nokee.utils.ClosureTestUtils.doSomething;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultKnownDomainObjectTest {
	@SuppressWarnings("unchecked") private final Supplier<DomainObjectIdentifier> identifierSupplier = Mockito.mock(Supplier.class);
	private final ConfigurableStrategy configurableStrategy = Mockito.mock(ConfigurableStrategy.class);
	private final ConfigurableProviderConvertibleStrategy providerConvertibleStrategy = Mockito.mock(ConfigurableProviderConvertibleStrategy.class);
	@SuppressWarnings("unchecked") private final Supplier<ModelNode> entitySupplier = Mockito.mock(Supplier.class);
	private final DefaultKnownDomainObject<MyType> subject = new DefaultKnownDomainObject<>(identifierSupplier, of(MyType.class), providerConvertibleStrategy, configurableStrategy, entitySupplier);

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnPublicMethods() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.untyped()).testAllPublicInstanceMethods(subject);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnConstructor() {
		new NullPointerTester().setDefault(ModelType.class, ModelType.untyped()).testAllPublicConstructors(DefaultKnownDomainObject.class);
	}

	@Test
	void forwardsIdentifierQueryToSupplier() {
		val expectedIdentifier = Mockito.mock(DomainObjectIdentifier.class);
		when(identifierSupplier.get()).thenReturn(expectedIdentifier);

		assertSame(expectedIdentifier, subject.getIdentifier());
		verify(identifierSupplier).get();
	}

	@Test
	void forwardsConfigureUsingActionToStrategy() {
		assertSame(subject, subject.configure(doSomething()));
		verify(configurableStrategy).configure(of(MyType.class), doSomething());
	}

	@Test
	void forwardsConfigureUsingClosureToStrategyAsAction() {
		assertSame(subject, subject.configure(doSomething(MyType.class)));
		verify(configurableStrategy).configure(of(MyType.class), new ClosureWrappedConfigureAction<>(doSomething(MyType.class)));
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsMapToProviderConvertibleValue() {
		val result = Mockito.mock(Provider.class);
		val provider = Mockito.mock(NamedDomainObjectProvider.class);
		when(providerConvertibleStrategy.asProvider(any())).thenReturn(provider);
		when(provider.map(any())).thenReturn(result);

		assertEquals(result, subject.map(aTransformer()));
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
		verify(provider).map(aTransformer());
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsFlatMapToProviderConvertibleValue() {
		val result = Mockito.mock(Provider.class);
		val provider = Mockito.mock(NamedDomainObjectProvider.class);
		when(providerConvertibleStrategy.asProvider(any())).thenReturn(provider);
		when(provider.flatMap(any())).thenReturn(result);

		assertEquals(result, subject.flatMap(aTransformer()));
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
		verify(provider).flatMap(aTransformer());
	}

	@Test
	@SuppressWarnings("unchecked")
	void forwardsAsProviderToStrategy() {
		val result = Mockito.mock(NamedDomainObjectProvider.class);
		when(providerConvertibleStrategy.asProvider(any())).thenReturn(result);

		assertEquals(result, subject.asProvider());
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val identifier = Mockito.mock(DomainObjectIdentifier.class);
		new EqualsTester()
			.addEqualityGroup(
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, entitySupplier),
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy, entitySupplier),
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, Mockito.mock(ConfigurableStrategy.class), entitySupplier),
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), Mockito.mock(ConfigurableProviderConvertibleStrategy.class), configurableStrategy, entitySupplier)
			)
			.addEqualityGroup(new DefaultKnownDomainObject<>(ofInstance(identifier), of(Object.class), providerConvertibleStrategy, configurableStrategy, entitySupplier))
			.addEqualityGroup(new DefaultKnownDomainObject<>(ofInstance(Mockito.mock(DomainObjectIdentifier.class)), of(Object.class), providerConvertibleStrategy, configurableStrategy, entitySupplier))
			.testEquals();
	}

	public interface MyType {}
}
