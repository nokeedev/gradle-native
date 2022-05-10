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

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package nokeebuild.buildscan;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.process.ExecOperations;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static nokeebuild.buildscan.IdeaIdeCustomValueProvider.IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES;
import static nokeebuild.buildscan.IdeaIdeCustomValueProvider.IDEA_VERSION_SYSTEM_PROPERTY_NAME;
import static nokeebuild.buildscan.UseGradleEnterpriseBuildScanServerIfConfigured.GRADLE_ENTERPRISE_URL_PROPERTY_NAME;

@SuppressWarnings("UnstableApiUsage")
class BuildScanPlugin implements Plugin<Settings> {
	private final ProviderFactory providers;
	private final ExecOperations execOperations;

	@Inject
	public BuildScanPlugin(ProviderFactory providers, ExecOperations execOperations) {
		this.providers = providers;
		this.execOperations = execOperations;
	}

	public void apply(Settings settings) {
		// We prefer using `--scan` flag because...
		//   We would be left playing a cat & mouse game with the enterprise plugin
		//   to figure out which version should match our build.
		settings.getPlugins().withId("com.gradle.enterprise", new ConfigureGradleEnterprisePlugin(settings, new SkipIfBuildScanExplicitlyDisabledViaStartParameters(settings.getStartParameter(), enterprise -> {
			final BuildScanParameters buildScanParameters = new BuildScanParameters(settings);
			if (buildScanParameters.serverUrl() != null) {
				buildScanParameters.accessKeys().stream()
					.filter(accessKey -> accessKey.startsWith(requireNonNull(buildScanParameters.serverUrl())))
					.findFirst()
					.map(it -> it.split("="))
					.ifPresent(gradleEnterpriseCredentials -> {
						enterprise.setServer(gradleEnterpriseCredentials[0]);
						enterprise.setAccessKey(gradleEnterpriseCredentials[1]);
					});
			}
			new ConfigureBuildScanExtension(buildScanParameters).execute(enterprise);
		})));
	}

	private class BuildScanParameters implements ConfigureBuildScanExtension.Parameters {
		private final Settings settings;

		private BuildScanParameters(Settings settings) {
			this.settings = settings;
		}

		@Override
		public boolean buildCacheEnabled() {
			return settings.getGradle().getStartParameter().isBuildCacheEnabled();
		}

		@Override
		public BuildEnvironmentCustomValueProvider.BuildEnvironment buildEnvironment() {
			if (providers.environmentVariable("CI").forUseAtConfigurationTime().isPresent()) {
				return BuildEnvironmentCustomValueProvider.BuildEnvironment.CI;
			} else {
				return BuildEnvironmentCustomValueProvider.BuildEnvironment.LOCAL;
			}
		}

		@Override
		public boolean wasLaunchedFromIdea() {
			return Stream.of(IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES)
				.anyMatch(it -> providers.systemProperty(it).forUseAtConfigurationTime().isPresent());
		}

		@Override
		public Optional<String> ideaVersion() {
			return Optional.ofNullable(providers.systemProperty(IDEA_VERSION_SYSTEM_PROPERTY_NAME)
				.forUseAtConfigurationTime().getOrNull());
		}

		@Nullable
		@Override
		public String serverUrl() {
			return providers.systemProperty(GRADLE_ENTERPRISE_URL_PROPERTY_NAME)
				.forUseAtConfigurationTime().getOrNull();
		}

		public List<String> accessKeys() {
			return Optional.ofNullable(providers.systemProperty("gradle.enterprise.accessKey").forUseAtConfigurationTime().orElse(providers.environmentVariable("GRADLE_ENTERPRISE_ACCESS_KEY").forUseAtConfigurationTime()).getOrNull()).map(it -> Arrays.asList(it.split(";"))).orElse(Collections.emptyList());
		}

		@Override
		public boolean isGitHubActionsEnvironment() {
			return providers.environmentVariable("GITHUB_ACTIONS")
				.forUseAtConfigurationTime().isPresent();
		}

		@Override
		public String githubRepository() {
			return envVar("GITHUB_REPOSITORY");
		}

		@Override
		public String githubRunId() {
			return envVar("GITHUB_RUN_ID");
		}

		@Override
		public String githubRunNumber() {
			return envVar("GITHUB_RUN_NUMBER");
		}

		@Nullable
		private String envVar(String variableName) {
			return providers.environmentVariable(variableName).forUseAtConfigurationTime().getOrNull();
		}

		@Override
		public Optional<String> gitRef() {
			// https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables
			String githubRef = envVar("GITHUB_REF");
			String githubHeadRef = envVar("GITHUB_HEAD_REF");
			if (githubRef != null) {
				return Optional.of(githubRef);
			} else if (githubHeadRef != null) {
				return Optional.of(githubHeadRef);
			} else {
				return capture(outStream -> {
					execOperations.exec(spec -> {
						spec.commandLine("git", "rev-parse", "--abbrev-ref", "HEAD");
						spec.setStandardOutput(outStream);
						spec.setErrorOutput(outStream);
						spec.workingDir(settings.getRootDir());
					});
				});
			}
		}

		@Override
		public Optional<String> gitStatus() {
			return capture(outStream -> {
				execOperations.exec(spec -> {
					spec.commandLine("git", "status", "--porcelain");
					spec.setStandardOutput(outStream);
					spec.setErrorOutput(outStream);
					spec.workingDir(settings.getRootDir());
				});
			});
		}

		@Override
		public Optional<String> gitCommitSha() {
			return capture(outStream -> {
				execOperations.exec(spec -> {
					spec.commandLine("git", "rev-parse", "--verify", "HEAD");
					spec.setStandardOutput(outStream);
					spec.setErrorOutput(outStream);
					spec.workingDir(settings.getRootDir());
				});
			});
		}

		@Override
		public Optional<String> gitCommitId() {
			return capture(outStream -> {
				execOperations.exec(spec -> {
					spec.commandLine("git", "rev-parse", "--short=8", "--verify", "HEAD");
					spec.setStandardOutput(outStream);
					spec.setErrorOutput(outStream);
					spec.workingDir(settings.getRootDir());
				});
			});
		}

		@Override
		public Optional<String> gitRepository() {
			return capture(outStream -> {
				execOperations.exec(spec -> {
					spec.commandLine("git", "config", "--get", "remote.origin.url");
					spec.setStandardOutput(outStream);
					spec.setErrorOutput(outStream);
					spec.workingDir(settings.getRootDir());
				});
			});
		}

		@Override
		public Optional<Boolean> publicBuildScanTermsOfServiceAgreed() {
			return Optional.ofNullable(providers.systemProperty("gradle.enterprise.agreePublicBuildScanTermOfService").forUseAtConfigurationTime().map(it -> "yes".equals(it)).getOrNull());
		}
	}

	private static Optional<String> capture(Consumer<? super OutputStream> action) {
		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		action.accept(outStream);
		return Optional.of(outStream.toString().trim()).filter(it -> !it.isEmpty());
	}
}
