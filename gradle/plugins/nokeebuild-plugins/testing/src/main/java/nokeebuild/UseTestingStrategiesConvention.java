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
import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginTestingStrategyFactory;
import nokeebuild.testing.strategies.DevelopmentTestingStrategy;
import nokeebuild.testing.strategies.OperatingSystemFamilyTestingStrategy;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.SetProperty;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class UseTestingStrategiesConvention implements Action<GradlePluginDevelopmentTestSuite> {
	private final ObjectFactory objects;
	private final ProviderFactory providers;
	private BiConsumer<? super SetProperty<String>, ? super SetProperty<OperatingSystemFamilyTestingStrategy>> action;

	public UseTestingStrategiesConvention(Project project, BiConsumer<? super SetProperty<String>, ? super SetProperty<OperatingSystemFamilyTestingStrategy>> action) {
		this.objects = project.getObjects();
		this.providers = project.getProviders();
		this.action = action;
	}

	@Override
	public void execute(GradlePluginDevelopmentTestSuite testSuite) {
		final SetProperty<String> testedGradleVersions = objects.setProperty(String.class);
		final SetProperty<OperatingSystemFamilyTestingStrategy> testedOsFamilies = objects.setProperty(OperatingSystemFamilyTestingStrategy.class);

		action.accept(testedGradleVersions, testedOsFamilies);

		testSuite.getTestingStrategies().value(providers.zip(testedGradleVersions.map(it -> {
			final GradlePluginTestingStrategyFactory strategyFactory = testSuite.getStrategies();
			return it.stream().map(v -> {
				switch (v) {
					case "minimum": return strategyFactory.getCoverageForMinimumVersion();
					case "latestGA": return strategyFactory.getCoverageForLatestGlobalAvailableVersion();
					case "latestNightly": return strategyFactory.getCoverageForLatestNightlyVersion();
					default: return strategyFactory.coverageForGradleVersion(v);
				}
			}).collect(Collectors.toList());
		}), testedOsFamilies, (versions, osFamilies) -> {
			final Set<GradlePluginTestingStrategy> strategies = new LinkedHashSet<>();
			final GradlePluginTestingStrategyFactory strategyFactory = testSuite.getStrategies();
			osFamilies.forEach(osFamily -> {
				versions.forEach(version -> {
					strategies.add(strategyFactory.composite(osFamily, version));
				});
			});

			final DevelopmentTestingStrategy developmentStrategy = new DevelopmentTestingStrategy();
			versions.forEach(version -> {
				strategies.add(strategyFactory.composite(developmentStrategy, version));
			});
			return strategies;
		})).disallowChanges();

		testSuite.getExtensions().add("testedGradleVersions", testedGradleVersions);
		testSuite.getExtensions().add("testedOsFamilies", testedOsFamilies);
	}
}
