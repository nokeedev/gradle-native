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

import dev.nokee.model.internal.names.ElementName;
import dev.nokee.utils.TextCaseUtils;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.provider.Provider;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class DependencyBuckets {
	/**
	 * Returns the default display name for the specified dependency bucket name.
	 *
	 * Note: For declarable buckets, use {@link #defaultDisplayNameOfDeclarableBucket(ElementName)} instead.
	 *
	 * @param name  the dependency bucket name, must not be null
	 * @return the default display name, never null
	 */
	public static String defaultDisplayName(ElementName name) {
		assert name != null;
		return Arrays.stream(TextCaseUtils.toWords(name.toString()).split(" ")).map(it -> {
			if (it.equals("api")) {
				return "API";
			} else if (it.equals("jvm")) {
				return "JVM";
			} else {
				return it;
			}
		}).collect(Collectors.joining(" "));
	}

	/**
	 * Returns the default display name for the specified declarable dependency bucket name.
	 *
	 * Note: For consumable/resolvable buckets, use {@link #defaultDisplayName(ElementName)} instead.
	 *
	 * @param name  the dependency bucket name, must not be null
	 * @return the default display name, never null
	 */
	public static String defaultDisplayNameOfDeclarableBucket(ElementName name) {
		return defaultDisplayName(name) + " dependencies";
	}

	public static Configuration finalize(Configuration self) {
		((ConfigurationInternal) self).preventFromFurtherMutation();
		for (Configuration configuration : self.getExtendsFrom()) {
			finalize(configuration);
		}
		return self;
	}

	public static Object assertConfigurableNotation(Object notation) {
		if (notation instanceof FileCollection) {
			throw new IllegalArgumentException("file collection are not configurable dependency notation");
		} else if (notation instanceof Provider) {
			return ((Provider<?>) notation).map(DependencyBuckets::assertConfigurableNotation);
		}
		return notation;
	}
}
