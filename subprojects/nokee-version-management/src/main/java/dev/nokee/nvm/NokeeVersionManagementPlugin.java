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

import static dev.nokee.nvm.NokeeVersionManagementService.registerService;
import static dev.nokee.nvm.NokeeVersionManagementService.toNokeeVersion;
import static dev.nokee.nvm.NokeeVersionSource.versionFile;
import static dev.nokee.nvm.ProviderUtils.forUseAtConfigurationTime;

// FIXME: Javadoc fail if no public class
public class NokeeVersionManagementPlugin implements Plugin<Settings> {
	private final ProviderFactory providers;

	@Inject
	public NokeeVersionManagementPlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Settings settings) {
		final Provider<NokeeVersionManagementService> service = forUseAtConfigurationTime(registerService(settings.getGradle(), defaultParameters(settings)));
		final Provider<NokeeVersion> version = service.map(NokeeVersionManagementService::getVersion);

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
	private Action<NokeeVersionManagementService.Parameters> defaultParameters(Settings settings) {
		return new Action<NokeeVersionManagementService.Parameters>() {
			@Override
			public void execute(NokeeVersionManagementService.Parameters parameters) {
				parameters.getNokeeVersion().value(
					providers.environmentVariable("NOKEE_VERSION").map(NokeeVersion::version)
						.orElse(providers.provider(settings::getGradle).flatMap(forEachParent(NokeeVersionManagementService::findServiceRegistration)).map(toNokeeVersion()))
						.orElse(forUseAtConfigurationTime(providers.of(NokeeVersionSource.class, versionFile(settings.getSettingsDir())))));
			}

			private Transformer<Provider<NokeeVersionManagementService>, Gradle> forEachParent(Function<Gradle, BuildServiceRegistration<NokeeVersionManagementService, NokeeVersionManagementService.Parameters>> mapper) {
				return gradle -> {
					while (gradle.getParent() != null) {
						gradle = gradle.getParent();
						final BuildServiceRegistration<NokeeVersionManagementService, NokeeVersionManagementService.Parameters> serviceRegistration = mapper.apply(gradle);

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
