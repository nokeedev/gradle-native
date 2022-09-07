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
package dev.nokee.model.internal.plugins;

import com.google.common.base.Preconditions;
import lombok.val;
import org.gradle.api.plugins.PluginAware;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.MoreCollectors.toOptional;

public final class PluginTargetSupport {
	private final String pluginId;
	private final Map<Class<? extends PluginAware>, Consumer<PluginAware>> invokers;

	private PluginTargetSupport(String pluginId, Map<Class<? extends PluginAware>, Consumer<PluginAware>> invokers) {
		this.pluginId = pluginId;
		this.invokers = invokers;
	}

	public void apply(PluginAware target) {
		invokers.entrySet().stream().filter(it -> it.getKey().isInstance(target)).collect(toOptional())
			.orElseThrow(() -> createInvalidPluginTargetException(target, pluginId)).getValue().accept(target);
	}

	private static RuntimeException createInvalidPluginTargetException(Object target, String pluginId) {
		return new RuntimeException(String.format("Could not apply plugin '%s' to %s. Please refer to the plugin reference documentation.", pluginId, target));
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String pluginId;
		private final Map<Class<? extends PluginAware>, Consumer<PluginAware>> invokers = new LinkedHashMap<>();

		public <T extends PluginAware> Builder forTarget(Class<T> targetType, Consumer<? super T> action) {
			invokers.put(targetType, it -> {
				assert targetType.isInstance(it);

				@SuppressWarnings("unchecked")
				val argument = (T) it;
				action.accept(argument);
			});
			return this;
		}

		public PluginTargetSupport build() {
			Preconditions.checkArgument(!invokers.isEmpty(), "at least one target must be specified");
			return new PluginTargetSupport(Objects.requireNonNull(pluginId, "'pluginId' must not be null"), invokers);
		}

		public Builder withPluginId(String pluginId) {
			this.pluginId = pluginId;
			return this;
		}
	}
}
