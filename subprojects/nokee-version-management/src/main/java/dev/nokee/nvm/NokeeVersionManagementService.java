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

import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.services.BuildServiceRegistration;

import static dev.nokee.nvm.DefaultNokeeVersionManagementService.SERVICE_NAME;

public interface NokeeVersionManagementService {
	NokeeVersion getVersion();

	static Provider<NokeeVersionManagementService> fromBuild(Gradle gradle) {
		return ((GradleInternal) gradle).getServices().get(ProviderFactory.class).provider(() -> {
			return gradle.getSharedServices().getRegistrations().findByName(SERVICE_NAME);
		}).flatMap(BuildServiceRegistration::getService).map(it -> new NokeeVersionManagementService() {
			@Override
			public NokeeVersion getVersion() {
				return DefaultNokeeVersionManagementService.asNokeeVersion(it);
			}
		});
	}
}
