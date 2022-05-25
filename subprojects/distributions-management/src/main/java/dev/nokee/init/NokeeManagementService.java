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
package dev.nokee.init;

import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.services.BuildServiceRegistration;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("UnstableApiUsage")
abstract class NokeeManagementService implements BuildService<NokeeManagementService.Parameters> {
	private static final String SERVICE_NAME = "nokeeManagement";

	interface Parameters extends BuildServiceParameters {
		Property<NokeeVersion> getNokeeVersion();
	}

	@Inject
	public NokeeManagementService() {}

	public NokeeVersion getVersion() {
		final NokeeVersion result = getParameters().getNokeeVersion().getOrNull();
		if (result == null) {
			throw new RuntimeException("Please add the Nokee version to use in a .nokee-version file.");
		}
		return result;
	}

	public static Provider<NokeeManagementService> registerService(Gradle gradle, Action<? super Parameters> action) {
		return gradle.getSharedServices().registerIfAbsent(SERVICE_NAME, NokeeManagementService.class, it -> {
			it.parameters(action);
		});
	}

	@SuppressWarnings("unchecked")
	public static Provider<NokeeManagementService> fromService(Gradle gradle) {
		return (Provider<NokeeManagementService>) gradle.getSharedServices().getRegistrations().getByName(SERVICE_NAME).getService();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static BuildServiceRegistration<NokeeManagementService, NokeeManagementService.Parameters> findServiceRegistration(Gradle gradle) {
		return (BuildServiceRegistration<NokeeManagementService, NokeeManagementService.Parameters>) gradle.getSharedServices().getRegistrations().findByName(SERVICE_NAME);
	}

	public static Transformer<NokeeVersion, BuildService<?>> toNokeeVersion() {
		return service -> {
			// Not the same class loader so classes don't align, reflection required
			try {
				return NokeeVersion.version(service.getClass().getMethod("getVersion").invoke(service).toString());
			} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
