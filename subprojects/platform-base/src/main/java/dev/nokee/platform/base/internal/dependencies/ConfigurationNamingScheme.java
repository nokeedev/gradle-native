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
