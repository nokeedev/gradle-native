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
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.KnownDomainObjectTester;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ProviderUtils.fixed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultKnownDomainObjectTest implements KnownDomainObjectTester<DefaultKnownDomainObjectTest.MyType> {
	private final MyType value = Mockito.mock(MyType.class);
	private final NamedDomainObjectProvider<MyType> provider = mockConfigurableProvider(fixed(value));
	@SuppressWarnings("unchecked") private final Supplier<DomainObjectIdentifier> identifierSupplier = Mockito.mock(Supplier.class);
	private final ConfigurableStrategy configurableStrategy = Mockito.mock(ConfigurableStrategy.class);
	private final ConfigurableProviderConvertibleStrategy providerConvertibleStrategy = Mockito.mock(ConfigurableProviderConvertibleStrategy.class);
	private final DomainObjectIdentifier identifier = Mockito.mock(DomainObjectIdentifier.class);
	private final DefaultKnownDomainObject<MyType> subject = new DefaultKnownDomainObject<>(identifierSupplier, of(MyType.class), providerConvertibleStrategy, configurableStrategy);

	@BeforeEach
	void canConvertToMyType() {
		when(identifierSupplier.get()).thenReturn(identifier);
		when(providerConvertibleStrategy.asProvider(of(MyType.class))).thenReturn(provider);
	}

	@Override
	public KnownDomainObject<MyType> subject() {
		return subject;
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
		val action = ActionTestUtils.mockAction(MyType.class);
		subject().configure(action);
		verify(configurableStrategy).configure(of(MyType.class), action);
	}

	@Test
	void forwardsConfigureUsingClosureToStrategyAsAction() {
		val closure = ClosureTestUtils.mockClosure(MyType.class);
		subject().configure(closure);
		verify(configurableStrategy).configure(of(MyType.class), new ClosureWrappedConfigureAction<>(closure));
	}

	@Test
	void forwardsAsProviderToStrategy() {
		val result = mockConfigurableProvider(fixed(value));
		when(providerConvertibleStrategy.asProvider(of(MyType.class))).thenReturn(result);
		assertEquals(result, subject().asProvider());
		verify(providerConvertibleStrategy).asProvider(of(MyType.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy),
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, configurableStrategy),
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), providerConvertibleStrategy, Mockito.mock(ConfigurableStrategy.class)),
				new DefaultKnownDomainObject<>(ofInstance(identifier), of(MyType.class), Mockito.mock(ConfigurableProviderConvertibleStrategy.class), configurableStrategy)
			)
			.addEqualityGroup(new DefaultKnownDomainObject<>(ofInstance(identifier), of(Object.class), providerConvertibleStrategy, configurableStrategy))
			.addEqualityGroup(new DefaultKnownDomainObject<>(ofInstance(Mockito.mock(DomainObjectIdentifier.class)), of(Object.class), providerConvertibleStrategy, configurableStrategy))
			.testEquals();
	}

	public interface MyType {}

	private static <T> NamedDomainObjectProvider<T> mockConfigurableProvider(Provider<T> provider) {
		@SuppressWarnings("unchecked") val result = (NamedDomainObjectProvider<T>) Mockito.mock(NamedDomainObjectProvider.class);
		Mockito.when(result.isPresent()).thenAnswer(it -> provider.isPresent());
		Mockito.when(result.get()).thenAnswer(it -> provider.get());
		Mockito.when(result.getOrNull()).thenAnswer(it -> provider.getOrNull());
		Mockito.when(result.getOrElse(any())).thenAnswer(it -> provider.getOrElse(it.getArgument(0)));
		Mockito.when(result.map(any())).thenAnswer(it -> provider.map(it.getArgument(0)));
		Mockito.when(result.flatMap(any())).thenAnswer(it -> provider.flatMap(it.getArgument(0)));
		return result;
	}
}
