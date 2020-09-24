package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.artifacts.Configuration;

import java.util.Locale;

public enum ConfigurationBucketType {
	DECLARABLE(false, false), CONSUMABLE(true, false), RESOLVABLE(false, true);

	final boolean canBeConsumed;
	final boolean canBeResolved;

	ConfigurationBucketType(boolean canBeConsumed, boolean canBeResolved) {
		this.canBeConsumed = canBeConsumed;
		this.canBeResolved = canBeResolved;
	}

	String getBucketTypeName() {
		return toString().toLowerCase(Locale.CANADA);
	}

	void configure(Configuration configuration) {
		configuration.setCanBeConsumed(canBeConsumed);
		configuration.setCanBeResolved(canBeResolved);
	}

	void assertConfigured(Configuration configuration) {
		if (configuration.isCanBeConsumed() != canBeConsumed || configuration.isCanBeResolved() != canBeResolved) {
			throw new IllegalStateException(String.format("Cannot reuse existing configuration named '%s' as a %s bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: %s, canBeResolved: %s], actual: [canBeConsumed: %s, canBeResolved: %s]).", configuration.getName(), getBucketTypeName(), canBeConsumed, canBeResolved, configuration.isCanBeConsumed(), configuration.isCanBeResolved()));
		}
	}
}
