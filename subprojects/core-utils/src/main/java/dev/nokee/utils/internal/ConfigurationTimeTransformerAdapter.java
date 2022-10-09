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
package dev.nokee.utils.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.Transformer;
import org.gradle.api.provider.ProviderFactory;

import static dev.nokee.utils.ProviderUtils.forParameters;
import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;

/**
 * Captures transformer input and output via value source.
 *
 * @param <OUT>  output type, must be serializable
 * @param <IN>  input type, must be serializable
 */
@EqualsAndHashCode
public final class ConfigurationTimeTransformerAdapter<OUT, IN> implements Transformer<OUT, IN> {
	private final ProviderFactory providers;
	private final Transformer<? extends OUT, ? super IN> delegate;

	public ConfigurationTimeTransformerAdapter(ProviderFactory providers, Transformer<? extends OUT, ? super IN> delegate) {
		this.providers = providers;
		this.delegate = delegate;
	}

	@Override
	public OUT transform(IN in) {
		@SuppressWarnings("unchecked") final OUT result = (OUT) forUseAtConfigurationTime(providers.of(ValueSourceTransformerAdapter.class, forParameters(it -> {
			it.getInput().set(in);
			it.getTransformer().set(delegate);
		}))).get();
		return result;
	}
}
