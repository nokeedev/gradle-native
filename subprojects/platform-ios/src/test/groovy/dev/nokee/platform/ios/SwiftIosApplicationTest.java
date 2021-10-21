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
package dev.nokee.platform.ios;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.swift.HasSwiftSourcesTester;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.testers.*;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.ios.internal.plugins.SwiftIosApplicationPlugin.swiftIosApplication;
import static org.hamcrest.MatcherAssert.assertThat;

class SwiftIosApplicationTest implements SourceAwareComponentTester<SwiftIosApplication>, HasBaseNameTester, ComponentTester<SwiftIosApplication>, DependencyAwareComponentTester<NativeComponentDependencies>, VariantAwareComponentTester<VariantView<NativeApplication>>, BinaryAwareComponentTester<BinaryView<Binary>>, HasSwiftSourcesTester {
	private final SwiftIosApplication subject = createSubject("foma");
	@Getter @TempDir File testDirectory;

	@Override
	public SwiftIosApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = project.getExtensions().getByType(ModelRegistry.class).register(swiftIosApplication(componentName, project)).as(SwiftIosApplication.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourcesUnderTest("swift", SwiftSourceSet.class, "swiftSources"), new SourcesUnderTest("resources", IosResourceSet.class, "resources"));
	}

	@Override
	public SwiftIosApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("foma"));
	}
}
