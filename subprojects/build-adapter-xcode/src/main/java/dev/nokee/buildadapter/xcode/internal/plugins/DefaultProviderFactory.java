/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.utils.internal.ValueSourceCallableAdapter;
import org.gradle.api.provider.Provider;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static dev.nokee.utils.ProviderUtils.forParameters;

public final class DefaultProviderFactory implements ProviderFactory {
	private final org.gradle.api.provider.ProviderFactory delegate;

	public DefaultProviderFactory(org.gradle.api.provider.ProviderFactory delegate) {
		this.delegate = delegate;
	}

	@Override
	public <T> Provider<T> provider(Callable<T> callable) {
		if (callable instanceof Serializable) {
			@SuppressWarnings("unchecked")
			final Provider<T> result = (Provider<T>) delegate.of(ValueSourceCallableAdapter.class, forParameters(it -> it.getCallable().set(callable)));
			return result;
		} else {
			return delegate.provider(callable);
		}
	}
}
