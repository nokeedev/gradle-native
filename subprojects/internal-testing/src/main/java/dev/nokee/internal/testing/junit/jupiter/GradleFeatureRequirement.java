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
package dev.nokee.internal.testing.junit.jupiter;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

public enum GradleFeatureRequirement {
	CONFIGURATION_CACHE {
		@Override
		public ConditionEvaluationResult isSupported(VersionNumber gradleVersion) {
			return gradleVersion.compareTo(VersionNumber.parse("6.9.2")) > 0 ? enabled("--configuration-cache supported") : disabled("--configuration-cache not supported");
		}
	};

	public abstract ConditionEvaluationResult isSupported(VersionNumber gradleVersion);
}
