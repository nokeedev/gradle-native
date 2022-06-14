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
package dev.nokee.nvm;

import groovy.lang.Closure;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class DefaultNokeeVersionManagementDependencyExtension implements NokeeVersionManagementDependencyExtension {
	private static final Logger LOGGER = Logging.getLogger(DefaultNokeeVersionManagementDependencyExtension.class);
	private final DependencyHandler dependencies;
	private final Provider<NokeeVersion> versionProvider;

	public DefaultNokeeVersionManagementDependencyExtension(DependencyHandler dependencies, Provider<NokeeVersion> versionProvider) {
		this.dependencies = dependencies;
		this.versionProvider = versionProvider;

		try {
			Method target = Class.forName("dev.nokee.nvm.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
			target.invoke(null, dependencies, "nokeeApi", new NokeeApiClosure(dependencies));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			LOGGER.info("Unable to extend DependencyHandler with nokeeApi().");
		}
	}

	@Override
	public Dependency nokeeApi() {
		return dependencies.platform(dependencies.create("dev.nokee:nokee-gradle-plugins:" + versionProvider.get()));
	}

	private final class NokeeApiClosure extends Closure<Dependency> {
		public NokeeApiClosure(DependencyHandler handler) {
			super(handler);
		}

		public Dependency doCall() {
			return nokeeApi();
		}
	}
}
