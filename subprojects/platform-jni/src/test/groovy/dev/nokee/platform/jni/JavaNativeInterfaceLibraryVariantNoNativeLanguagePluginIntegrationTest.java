/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.DefaultBuildVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryVariantRegistrationFactory;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryVariantNoNativeLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JniLibrary subject;

	@BeforeEach
	void createSubject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		val componentIdentifier = ComponentIdentifier.of("zoko", ProjectIdentifier.of(project));
		registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(componentIdentifier)).build());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryVariantRegistrationFactory.class);
		val variantIdentifier = VariantIdentifier.of(DefaultBuildVariant.of(of("unix-arm")), componentIdentifier);
		subject = registry.register(factory.create(variantIdentifier)).as(JniLibrary.class).get();
	}

	@Test
	void noNativeCompileOnlyConfigurationWhenNoNativeLanguagePluginApplied() {
		assertThat(project().getConfigurations(), not(hasItem(named(endsWith("NativeCompileOnly")))));
	}

	@Test
	void noHeaderSearchPathsConfigurationWhenNoNativeLanguagePluginApplied() {
		assertThat(project().getConfigurations(), not(hasItem(named(endsWith("HeaderSearchPaths")))));
	}

	@Test
	void noCSourceSetWhenCLanguagePluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(CSourceSet.class))));
	}

	@Test
	void noCppSourceSetWhenCppLanguagePluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(CppSourceSet.class))));
	}

	@Test
	void noObjectiveCSourceSetWhenObjectiveCLanguagePluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(ObjectiveCSourceSet.class))));
	}

	@Test
	void noObjectiveCppSourceSetWhenObjectiveCppLanguagePluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(ObjectiveCppSourceSet.class))));
	}

	@Test
	void noSwiftSourceSetWhenSwiftLanguagePluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(SwiftSourceSet.class))));
	}
}
