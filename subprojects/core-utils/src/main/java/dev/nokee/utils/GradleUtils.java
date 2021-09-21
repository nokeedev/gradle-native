/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.utils;

import org.gradle.api.invocation.Gradle;

public final class GradleUtils {
	private GradleUtils() {}

	public static boolean isHostBuild(Gradle gradle) {
		return gradle.getParent() == null;
	}

	public static boolean hasIncludedBuilds(Gradle gradle) {
		return !gradle.getIncludedBuilds().isEmpty();
	}

	public static boolean isIncludedBuild(Gradle gradle) {
		return gradle.getParent() != null;
	}

	public static boolean isCompositeBuild(Gradle gradle) {
		return isIncludedBuild(gradle) || hasIncludedBuilds(gradle);
	}
}
