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
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryComponentNoJvmLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		val identifier = ComponentIdentifier.of("kuvu", ProjectIdentifier.ofRootProject());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		this.subject = registry.register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.host()));
	}

	@Test
	void noJavaSourceSetWhenJavaPluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(JavaSourceSet.class))));
	}

	@Test
	void noGroovySourceSetWhenGroovyPluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(JavaSourceSet.class))));
	}

	@Test
	void noKotlinSourceSetWhenKotlinPluginNotApplied() {
		assertThat(subject.getSources().get(), not(hasItem(isA(KotlinSourceSet.class))));
	}

	@Test
	void noJvmJarBinaryWhenJvmLanguagePluginNotApplied() {
		assertThat(subject.getBinaries().get(), not(hasItem(isA(JvmJarBinary.class))));
	}

	@Nested
	class ComponentSourcesTest {
		public JavaNativeInterfaceLibrarySources subject() {
			return subject.getSources();
		}

		@Test
		void throwsSensibleErrorMessageWhenAccessingJavaSourceSetWithoutJavaPluginApplied() {
			val ex = assertThrows(RuntimeException.class, () -> subject().getJava());
			assertThat(ex.getMessage(), equalTo("Please apply 'java' plugin to access Java source set."));
		}

		@Test
		void throwsSensibleErrorMessageWhenAccessingGroovySourceSetWithoutGroovyPluginApplied() {
			val ex = assertThrows(RuntimeException.class, () -> subject().getGroovy());
			assertThat(ex.getMessage(), equalTo("Please apply 'groovy' plugin to access Groovy source set."));
		}

		@Test
		void throwsSensibleErrorMessageWhenAccessingKotlinSourceSetWithoutKotlinPluginApplied() {
			val ex = assertThrows(RuntimeException.class, () -> subject().getKotlin());
			assertThat(ex.getMessage(), equalTo("Please apply 'org.jetbrains.kotlin.jvm' plugin to access Kotlin source set."));
		}
	}
}
