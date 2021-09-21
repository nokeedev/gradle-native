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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.internal.ComponentName;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import static java.util.Objects.requireNonNull;

public abstract class ConfigurationNamingScheme {
	public abstract String configurationName(String name);

	public static ConfigurationNamingScheme identity() {
		return new IdentityConfigurationNamingScheme();
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class IdentityConfigurationNamingScheme extends ConfigurationNamingScheme {
		@Override
		public String configurationName(String name) {
			return requireNonNull(name);
		}
	}

	public static ConfigurationNamingScheme prefixWith(String prefix) {
		requireNonNull(prefix);
		if (StringUtils.isEmpty(prefix)) {
			return new IdentityConfigurationNamingScheme();
		}
		return new PrefixWithConfigurationNamingScheme(prefix);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class PrefixWithConfigurationNamingScheme extends ConfigurationNamingScheme {
		private final String prefix;

		private PrefixWithConfigurationNamingScheme(String prefix) {
			this.prefix = requireNonNull(prefix);
		}

		@Override
		public String configurationName(String name) {
			return prefix + StringUtils.capitalize(requireNonNull(name));
		}
	}

	public static ConfigurationNamingScheme forComponent(ComponentName componentName) {
		if (componentName.isMain()) {
			return new IdentityConfigurationNamingScheme();
		}
		return new PrefixWithConfigurationNamingScheme(componentName.get());
	}

	public static ConfigurationNamingScheme forVariant(ComponentName componentName, String variantName) {
		requireNonNull(componentName);
		requireNonNull(variantName);
		if (StringUtils.isEmpty(variantName)) {
			return forComponent(componentName);
		} else if (componentName.isMain()) {
			return new PrefixWithConfigurationNamingScheme(variantName);
		}
		return new PrefixWithConfigurationNamingScheme(componentName.get() + StringUtils.capitalize(variantName));
	}
}
