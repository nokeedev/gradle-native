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
import dev.nokee.utils.ActionTestUtils;
import dev.nokee.utils.ClosureTestUtils;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.ProviderUtils.fixed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultKnownDomainObjectTest implements KnownDomainObjectTester<DefaultKnownDomainObjectTest.MyType> {
	private final MyType value = Mockito.mock(MyType.class);
	private final ConfigurableStrategy configurableStrategy = Mockito.mock(ConfigurableStrategy.class);
	private final ProviderConvertibleStrategy providerConvertibleStrategy = Mockito.mock(ProviderConvertibleStrategy.class);
	private final DomainObjectIdentifier identifier = Mockito.mock(DomainObjectIdentifier.class);
	private final DefaultKnownDomainObject<MyType> subject = new DefaultKnownDomainObject<>(identifier, MyType.class, providerConvertibleStrategy, configurableStrategy);

	@BeforeEach
	void canConvertToMyType() {
		when(providerConvertibleStrategy.asProvider(MyType.class)).thenReturn(fixed(value));
	}

	@Override
	public KnownDomainObject<MyType> subject() {
		return subject;
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnConstructor() {
		new NullPointerTester().testAllPublicConstructors(DefaultKnownDomainObject.class);
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
		val result = fixed(value);
		when(providerConvertibleStrategy.asProvider(MyType.class)).thenReturn(result);
		assertEquals(result, subject().asProvider());
		verify(providerConvertibleStrategy).asProvider(MyType.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(
				new DefaultKnownDomainObject<>(identifier, MyType.class, providerConvertibleStrategy, configurableStrategy),
				new DefaultKnownDomainObject<>(identifier, MyType.class, providerConvertibleStrategy, configurableStrategy),
				new DefaultKnownDomainObject<>(identifier, MyType.class, providerConvertibleStrategy, Mockito.mock(ConfigurableStrategy.class)),
				new DefaultKnownDomainObject<>(identifier, MyType.class, Mockito.mock(ProviderConvertibleStrategy.class), configurableStrategy)
			)
			.addEqualityGroup(new DefaultKnownDomainObject<>(identifier, Object.class, providerConvertibleStrategy, configurableStrategy))
			.addEqualityGroup(new DefaultKnownDomainObject<>(Mockito.mock(DomainObjectIdentifier.class), Object.class, providerConvertibleStrategy, configurableStrategy))
			.testEquals();
	}

	public interface MyType {}
}
