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

import com.google.common.annotations.VisibleForTesting;
import dev.nokee.platform.base.internal.ComponentName;
import org.gradle.api.Project;

import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Owner.*;

public final class ConfigurationDescriptionScheme {
	private final ConfigurationDescription.Owner owner;

	private ConfigurationDescriptionScheme(ConfigurationDescription.Owner owner) {
		this.owner = owner;
	}

	public ConfigurationDescription description(ConfigurationDescription.Subject subject, ConfigurationDescription.Bucket bucket) {
		return new ConfigurationDescription(subject, bucket, owner);
	}

	@VisibleForTesting
	static ConfigurationDescriptionScheme forThisProject() {
		return new ConfigurationDescriptionScheme(ofThisProject());
	}

	public static ConfigurationDescriptionScheme forProject(Project project) {
		return new ConfigurationDescriptionScheme(ofProject(project));
	}

	public static ConfigurationDescriptionScheme forComponent(ComponentName componentName) {
		return new ConfigurationDescriptionScheme(ofComponent(componentName));
	}

	public static ConfigurationDescriptionScheme forVariant(ComponentName componentName, String variantName) {
		return new ConfigurationDescriptionScheme(ofVariant(componentName, variantName));
	}
}
