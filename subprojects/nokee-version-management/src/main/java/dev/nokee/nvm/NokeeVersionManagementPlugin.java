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

import lombok.val;
import org.gradle.StartParameter;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Transformer;
import org.gradle.api.initialization.Settings;
import org.gradle.api.internal.SettingsInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceSpec;
import org.gradle.api.services.BuildServiceRegistration;

import javax.inject.Inject;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Function;

import static dev.nokee.nvm.NokeeVersionManagementService.registerService;
import static dev.nokee.nvm.NokeeVersionManagementService.toNokeeVersion;
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
			spec.repositories(repositories -> repositories.maven(new NokeeRepositoryAction(version.map(new InferNokeeRepositoryUrl(providers)))));
			spec.resolutionStrategy(strategy -> strategy.eachPlugin(new OnlyIfUnderNokeeNamespaceAction(details -> {
				details.useVersion(version.get().toString());
			})));
		});

		settings.getGradle().allprojects(new InjectNokeeVersionManagementExtensions(version));
	}

	@SuppressWarnings("UnstableApiUsage")
	private Action<NokeeVersionManagementService.Parameters> defaultParameters(Settings settings) {
		return new Action<NokeeVersionManagementService.Parameters>() {
			@Override
			public void execute(NokeeVersionManagementService.Parameters parameters) {
				parameters.getNokeeVersion().value(fromEnvironmentVariable().orElse(fromParentNokeeBuilds()).orElse(fromNokeeVersionFile()));
			}

			private Provider<NokeeVersion> fromEnvironmentVariable() {
				return forUseAtConfigurationTime(providers.environmentVariable("NOKEE_VERSION")).map(NokeeVersion::version);
			}

			//region gradle.parent services
			private Provider<NokeeVersion> fromParentNokeeBuilds() {
				return providers.provider(() -> providers.provider(((SettingsInternal) settings)::getGradle).map(forEachParent(NokeeVersionManagementService::findServiceRegistration)).flatMap(BuildServiceRegistration::getService).map(toNokeeVersion()).getOrNull());
			}

			private <T> Transformer<T, Gradle> forEachParent(Function<Gradle, T> mapper) {
				return new ForEachParentGradleTransformer<>(mapper);
			}
			//endregion

			//region .nokee-version file
			private Provider<NokeeVersion> fromNokeeVersionFile() {
				return forUseAtConfigurationTime(providers.of(nokeeVersionProvider(), this::configure));
			}

			@SuppressWarnings("unchecked")
			private <T extends ValueSource<NokeeVersion, ? extends NokeeVersionParameters>> Class<T> nokeeVersionProvider() {
				if (settings.getGradle().getParent() == null) {
					return (Class<T>) CurrentNokeeVersionSource.class;
				} else {
					return (Class<T>) NokeeVersionSource.class;
				}
			}

			private void configure(ValueSourceSpec<? extends NokeeVersionParameters> spec) {
				spec.parameters(parameters -> {
					parameters.getNokeeVersionFile().fileValue(new File(settings.getSettingsDir(), ".nokee-version"));

					if (parameters instanceof CurrentNokeeVersionSource.Parameters) {
						val extendedParameters = (CurrentNokeeVersionSource.Parameters) parameters;
						extendedParameters.getNetworkStatus().set(networkStatus(settings.getGradle().getStartParameter()));
						extendedParameters.getCurrentReleaseUrl().set(forUseAtConfigurationTime(currentReleaseUrl()).map(this::uri));
					}
				});
			}

			private Provider<String> currentReleaseUrl() {
				return providers.systemProperty("dev.nokee.internal.currentRelease.url");
			}

			private CurrentNokeeVersionSource.Parameters.NetworkStatus networkStatus(StartParameter startParameter) {
				return startParameter.isOffline() ? CurrentNokeeVersionSource.Parameters.NetworkStatus.DISALLOWED
					: CurrentNokeeVersionSource.Parameters.NetworkStatus.ALLOWED;
			}

			private URI uri(String s) {
				try {
					return new URI(s);
				} catch (URISyntaxException e) {
					throw new RuntimeException(e);
				}
			}
			//endregion
		};
	}
}
