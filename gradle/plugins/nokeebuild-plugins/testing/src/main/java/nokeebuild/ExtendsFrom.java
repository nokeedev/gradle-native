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

package nokeebuild;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

final class ExtendsFrom implements Action<GradlePluginDevelopmentTestSuite> {
	private final ConfigurationContainer configurations;
	private final SourceSet sourceSet;

	public ExtendsFrom(Project project, SourceSet sourceSet) {
		this.configurations = project.getConfigurations();
		this.sourceSet = sourceSet;
	}

	@Override
	public void execute(GradlePluginDevelopmentTestSuite testSuite) {
		final SourceSet testSourceSet = testSuite.getSourceSet().get();
		configurations.named(testSourceSet.getImplementationConfigurationName(),
			extendsFrom(configurations.named(sourceSet.getImplementationConfigurationName())));
		configurations.named(testSourceSet.getRuntimeOnlyConfigurationName(),
			extendsFrom(configurations.named(sourceSet.getRuntimeOnlyConfigurationName())));
	}

	private static Action<Configuration> extendsFrom(Provider<Configuration> configurationProvider) {
		return configuration -> {
			configuration.extendsFrom(configurationProvider.get());
		};
	}
}
