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
