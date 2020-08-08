package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

public interface ConfigurationFactories {
	class Creating implements ConfigurationFactory {
		private final ConfigurationContainer configurations;

		public Creating(ConfigurationContainer configurations) {
			this.configurations = configurations;
		}

		@Override
		public Configuration create (String name){
			return configurations.create(name);
		}
	}

	class Prefixing implements ConfigurationFactory {
		private final ConfigurationFactory delegate;
		private final PrefixingNamingScheme namingScheme;

		public Prefixing(ConfigurationFactory delegate, PrefixingNamingScheme namingScheme) {
			this.delegate = delegate;
			this.namingScheme = namingScheme;
		}

		@Override
		public Configuration create(String name) {
			return delegate.create(namingScheme.prefix(name));
		}
	}

	class MaybeCreating implements ConfigurationFactory {
		private final ConfigurationContainer configurations;

		public MaybeCreating(ConfigurationContainer configurations) {
			this.configurations = configurations;
		}

		@Override
		public Configuration create(String name){
			return configurations.maybeCreate(name);
		}
	}
}
