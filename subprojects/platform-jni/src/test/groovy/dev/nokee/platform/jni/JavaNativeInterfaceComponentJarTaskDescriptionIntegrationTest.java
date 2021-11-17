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

import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.TaskMatchers.description;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceComponentJarTaskDescriptionIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		val identifier = ComponentIdentifier.of("qoba", ProjectIdentifier.ofRootProject());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		this.subject = registry.register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.host()));
	}

	@Test
	void containsOnlySharedLibraryForJniJarBinaryOnSingleVariantWhenJavaPluginNotApplied() {
		assertThat(subject.getTasks().get(), hasItem(allOf(
			named("jarQoba"),
			description("Assembles a JAR archive containing the shared library for JNI JAR binary ':qoba:jniJar'.")
		)));
	}

	@Test
	@PluginRequirement.Require(id = "java")
	void containsClassesAndSharedLibraryForJvmJarBinaryOnSingleVariantWhenJavaPluginApplied() {
		assertThat(subject.getTasks().get(), hasItem(allOf(
			named("jarQoba"),
			description("Assembles a JAR archive containing the classes and shared library for JVM JAR binary ':qoba:jvmJar'.")
		)));
	}

	@Test
	@PluginRequirement.Require(id = "java")
	void containsOnlyClassesForJvmJarBinaryOnMultipleVariantWhenJavaPluginApplied() {
		subject.getTargetMachines().set(ImmutableSet.of(of("macos-x64"), of("freebsd-x64")));
		assertThat(subject.getTasks().get(), hasItem(allOf(
			named("jarQoba"),
			description("Assembles a JAR archive containing the classes for JVM JAR binary ':qoba:jvmJar'.")
		)));
	}
}
