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

import com.google.common.base.Preconditions;
import dev.nokee.utils.internal.ConfigurationTimeTransformerAdapter;
import org.gradle.api.Transformer;
import org.gradle.api.provider.ProviderFactory;

import java.io.Serializable;

public final class BuildInputService {
	private final ProviderFactory providers;

	public BuildInputService(ProviderFactory providers) {
		this.providers = providers;
	}

	public <OUT, IN> Transformer<OUT, IN> capture(String displayName, Transformer<? extends OUT, ? super IN> transformer) {
		Preconditions.checkArgument(transformer instanceof Serializable);
		return new ConfigurationTimeTransformerAdapter<>(providers, displayName, transformer);
	}
}
