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
import org.gradle.api.Plugin;
import org.gradle.api.Transformer;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.services.BuildServiceRegistration;

import javax.inject.Inject;
import java.util.function.Function;

import static dev.nokee.init.NokeeManagementService.registerService;
import static dev.nokee.init.NokeeManagementService.toNokeeVersion;
import static dev.nokee.init.NokeeVersionSource.versionFile;
import static dev.nokee.init.ProviderUtils.forUseAtConfigurationTime;

// FIXME: Javadoc fail if no public class
public class DistributionsManagementPlugin implements Plugin<Settings> {
	private final ProviderFactory providers;

	@Inject
	public DistributionsManagementPlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Settings settings) {
		final Provider<NokeeManagementService> service = forUseAtConfigurationTime(registerService(settings.getGradle(), defaultParameters(settings)));
		final Provider<NokeeVersion> version = service.map(NokeeManagementService::getVersion);

		settings.pluginManagement(spec -> {
			// TODO: Allow disable of the default gradlePluginPortal()
			spec.repositories(repositories -> {
				if (spec.getRepositories().size() == 0) {
					spec.getRepositories().gradlePluginPortal();
				}
			});
			spec.repositories(new NokeeRepositoryAction(version.map(new InferNokeeRepositoryUrl())));
			spec.resolutionStrategy(strategy -> strategy.eachPlugin(new OnlyIfUnderNokeeNamespaceAction(details -> {
				details.useVersion(version.get().toString());
			})));
		});
	}

	@SuppressWarnings("UnstableApiUsage")
	private Action<NokeeManagementService.Parameters> defaultParameters(Settings settings) {
		return new Action<NokeeManagementService.Parameters>() {
			@Override
			public void execute(NokeeManagementService.Parameters parameters) {
				parameters.getNokeeVersion().value(
					providers.provider(settings::getGradle).flatMap(forEachParent(NokeeManagementService::findServiceRegistration)).map(toNokeeVersion())
						.orElse(forUseAtConfigurationTime(providers.of(NokeeVersionSource.class, versionFile(settings.getSettingsDir())))));
			}

			private Transformer<Provider<NokeeManagementService>, Gradle> forEachParent(Function<Gradle, BuildServiceRegistration<NokeeManagementService, NokeeManagementService.Parameters>> mapper) {
				return gradle -> {
					while (gradle.getParent() != null) {
						gradle = gradle.getParent();
						final BuildServiceRegistration<NokeeManagementService, NokeeManagementService.Parameters> serviceRegistration = mapper.apply(gradle);

						if (serviceRegistration != null) {
							return serviceRegistration.getService();
						}
					}
					return providers.provider(() -> null);
				};
			}
		};
	}
}
