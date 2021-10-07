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
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public abstract class DomainObjectProviderConfigureUsingClosureTester<T> extends AbstractDomainObjectProviderConfigureTester<T> {
	@Override
	protected void configure(DomainObjectProvider<T> provider, Action<T> configurationAction) {
		provider.configure(new ClosureActionAdapter<T>(this) {
			@Override
			public void doCall(T t) {
				configurationAction.execute(t);
			}
		});
	}

	private static abstract class ClosureActionAdapter<T> extends groovy.lang.Closure<Void> {

		public ClosureActionAdapter(Object owner, Object thisObject) {
			super(owner, thisObject);
		}

		public ClosureActionAdapter(Object owner) {
			super(owner);
		}

		public abstract void doCall(T t);
	}

	private interface CapturedValue {
		void capture(Object value);
	}

	@Test
	@SuppressWarnings("rawtypes")
	void throwsExceptionIfConfigurationClosureIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject().configure((Closure) null));
	}

	@Test
	void configuresDelegateFirstResolveStrategy() {
		val captor = mock(CapturedValue.class);
		val closure = new ClosureActionAdapter<T>(this) {
			@Override
			public void doCall(Object o) {
				captor.capture(getResolveStrategy());
			}
		};
		val provider = createSubject();

		when:
		provider.configure(closure);
		resolve(provider);

		then:
		verify(captor, times(1)).capture(Closure.DELEGATE_FIRST);
	}

	@Test
	void configuresDelegateToProviderValue() {
		val captor = mock(CapturedValue.class);
		val closure = new ClosureActionAdapter<T>(this) {
			@Override
			public void doCall(T t) {
				captor.capture(getDelegate());
			}
		};
		val provider = createSubject();

		when:
		provider.configure(closure);
		val value = resolve(provider);

		then:
		verify(captor, times(1)).capture(value);
	}
}
