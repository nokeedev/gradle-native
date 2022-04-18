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
package dev.nokee.platform.base;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelProjections;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ModelBackedVariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.Value;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.ComponentIdentifier.ofMain;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;

// See https://github.com/nokeedev/gradle-native/issues/555
class VariantAwareComponentDimensionsValueOrderingIntegrationTest {
	private final Project project = ProjectTestUtils.rootProject();
	private VariantAwareComponent<Variant> subject;

	@BeforeEach
	void applyPlugins() {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);
		subject = project.getExtensions().getByType(ModelRegistry.class).register(ModelRegistration.builder()
			.withComponent(new IdentifierComponent(ofMain(ProjectIdentifier.of(project))))
			.withComponent(ModelProjections.managed(of(MyComponent.class))).build()).as(MyComponent.class).get();
		subject.getDimensions().newAxis(OSFamily.class).value(asList(new OSFamily("windows"), new OSFamily("macOS"), new OSFamily("linux")));
		subject.getDimensions().newAxis(MyAxis.class, it -> it.onlyIf(OSFamily.class, (a, b) -> !a.isPresent() || b.isWindows())).value(asList(new MyAxis("first"), new MyAxis("second")));
	}

	@Test
	void ordersBuildVariantWithoutDimensionsBeforeTheBuildVariantWithDimensionValues() {
		assertThat(subject.getBuildVariants(), providerOf(contains(
			hasToString("windows"),
			hasToString("windowsFirst"), hasToString("windowsSecond"),
			hasToString("macOS"), hasToString("linux"))));
	}

	public interface MyComponent extends ModelBackedVariantAwareComponentMixIn<Variant> {}
	@Value
	private static class OSFamily implements Named {
		String name;

		boolean isWindows() {
			return "windows".equals(name);
		}
	}

	@Value
	private static class MyAxis implements Named {
		String name;
	}
}
