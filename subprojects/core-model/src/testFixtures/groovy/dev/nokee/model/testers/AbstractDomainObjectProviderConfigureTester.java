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
package dev.nokee.model.testers;

import dev.nokee.model.DomainObjectProvider;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public abstract class AbstractDomainObjectProviderConfigureTester<T> extends AbstractDomainObjectProviderTester<T> {
	protected abstract void configure(DomainObjectProvider<T> provider, Action<T> configurationAction);

	@Test
	void doesNotCallConfigurationActionImmediately() {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		configure(createSubject(), action);
		verify(action, Mockito.never()).execute(any());
	}

	@Test
	void callsConfigurationActionWhenProviderResolves() {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		val provider = createSubject();

		when:
		configure(provider, action);
		resolve(provider);

		then:
		verify(action, times(1)).execute(any());
	}

	@Test
	void callsConfigurationActionImmediatelyWhenProviderWasAlreadyResolved() {
		@SuppressWarnings("unchecked")
		val action = (Action<T>) mock(Action.class);
		val provider = createSubject();

		when:
		resolve(provider);
		configure(provider, action);

		then:
		verify(action, times(1)).execute(any());
	}
}
