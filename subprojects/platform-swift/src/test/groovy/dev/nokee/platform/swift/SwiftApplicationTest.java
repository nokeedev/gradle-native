/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.swift;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.HasSwiftSourcesTester;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.swift.internal.plugins.SwiftApplicationPlugin.swiftApplication;
import static org.hamcrest.MatcherAssert.assertThat;

public class SwiftApplicationTest implements SourceAwareComponentTester<SwiftApplication>, HasBaseNameTester, ComponentTester<SwiftApplication>, DependencyAwareComponentTester<NativeApplicationComponentDependencies>, HasSwiftSourcesTester {
	private final SwiftApplication subject = createSubject("cefu");
	@Getter @TempDir File testDirectory;

	@Override
	public SwiftApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(testDirectory);
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create((DefaultModelRegistry) project.getExtensions().getByType(ModelRegistry.class), swiftApplication(componentName, project)).as(SwiftApplication.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourcesUnderTest("swift", SwiftSourceSet.class, "swiftSources"));
	}

	@Override
	public SwiftApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("cefu"));
	}
}
