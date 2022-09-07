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
package dev.nokee.utils;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.services.BuildServiceSpec;

/**
 * Conveniences when registering build service.
 */
@SuppressWarnings("UnstableApiUsage")
public class BuildServiceUtils {
	private BuildServiceUtils() {}

	/**
	 * Registers a build service if absent or return current build service.
	 *
	 * <p>This method has some added conveniences not present in Gradle API.
	 * It assumes there will ever be at most one build service of the given type.
	 * For this reason, it will infer the service name from the service type.
	 * This method doesn't require a configuration action when the parameters are obviously not present.
	 *
	 * @param self  the Gradle build to register the service, must not be null
	 * @param type  the build service type, must not be null
	 * @return a provider of the registered build service, never null and always present
	 * @param <T>  the build service type
	 * @see #registerBuildServiceIfAbsent(Project, Class) convenience for {@code Project} instances
	 * @see #registerBuildServiceIfAbsent(Settings, Class) convenience for {@code Settings} instances
	 */
	public static <T extends BuildService<BuildServiceParameters.None>> Provider<T> registerBuildServiceIfAbsent(Gradle self, Class<T> type) {
		return self.getSharedServices().registerIfAbsent(type.getSimpleName(), type, ActionUtils.doNothing());
	}

	/**
	 * Registers a build service if absent or return current build service.
	 *
	 * <p>This method has some added conveniences not present in Gradle API.
	 * It assumes there will ever be at most one build service of the given type.
	 * For this reason, it will infer the service name from the service type.
	 * This method will configure the build parameters directly, requiring less indirection.
	 *
	 * @param self  the Gradle build to register the service, must not be null
	 * @param type  the build service type, must not be null
	 * @param action  the configuration action for the parameters, must not be null
	 * @return a provider of the registered build service, never null and always present
	 * @param <T>  the build service type
	 * @see #registerBuildServiceIfAbsent(Project, Class, Action) convenience for {@code Project} instances
	 * @see #registerBuildServiceIfAbsent(Settings, Class, Action) convenience for {@code Settings} instances
	 */
	public static <T extends BuildService<P>, P extends BuildServiceParameters> Provider<T> registerBuildServiceIfAbsent(Gradle self, Class<T> type, Action<? super P> action) {
		return self.getSharedServices().registerIfAbsent(type.getSimpleName(), type, forParameters(action));
	}

	public static <T extends BuildService<BuildServiceParameters.None>> Provider<T> registerBuildServiceIfAbsent(Project self, Class<T> type) {
		return registerBuildServiceIfAbsent(self.getGradle(), type);
	}

	public static <T extends BuildService<P>, P extends BuildServiceParameters> Provider<T> registerBuildServiceIfAbsent(Project self, Class<T> type, Action<? super P> action) {
		return registerBuildServiceIfAbsent(self.getGradle(), type, action);
	}

	public static <T extends BuildService<BuildServiceParameters.None>> Provider<T> registerBuildServiceIfAbsent(Settings self, Class<T> type) {
		return registerBuildServiceIfAbsent(self.getGradle(), type);
	}

	public static <T extends BuildService<P>, P extends BuildServiceParameters> Provider<T> registerBuildServiceIfAbsent(Settings self, Class<T> type, Action<? super P> action) {
		return registerBuildServiceIfAbsent(self.getGradle(), type, action);
	}

	public static <P extends BuildServiceParameters> Action<BuildServiceSpec<P>> forParameters(Action<? super P> action) {
		return it -> it.parameters(action);
	}
}
