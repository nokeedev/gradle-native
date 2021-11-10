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
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.testers.SourceTester;
import dev.nokee.language.jvm.HasKotlinSourceSet;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.jni.internal.JavaNativeInterfaceLibraryComponentRegistrationFactory;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryComponentKotlinPluginIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply("org.jetbrains.kotlin.jvm");
		val identifier = ComponentIdentifier.of("bado", ProjectIdentifier.ofRootProject());
		val factory = project.getExtensions().getByType(JavaNativeInterfaceLibraryComponentRegistrationFactory.class);
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		this.subject = registry.register(factory.create(identifier)).as(JavaNativeInterfaceLibrary.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.host()));
	}

	@Test
	void hasKotlinSourceSetWhenKotlinPluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("badoKotlin"), isA(KotlinSourceSet.class))));
	}

	@Test
	void hasJvmJarBinaryWhenKotlinPluginApplied() {
		assertThat(subject.getBinaries().get(), hasItem(allOf(named("badoJvmJar"), isA(JvmJarBinary.class))));
	}

	// TODO: Test Jar task doesn't have JVM jar binary name

	@Nested
	class KotlinComponentSourcesTest implements SourceTester<HasKotlinSourceSet, KotlinSourceSet> {
		@Override
		public HasKotlinSourceSet subject() {
			return subject.getSources();
		}

		@Override
		public NamedDomainObjectProvider<? extends LanguageSourceSet> get(HasKotlinSourceSet self) {
			return self.getKotlin();
		}

		@Override
		public void configure(HasKotlinSourceSet self, Action<? super KotlinSourceSet> action) {
			self.kotlin(action);
		}

		@Override
		public void configure(HasKotlinSourceSet self, @SuppressWarnings("rawtypes") Closure closure) {
			self.kotlin(closure);
		}
	}
}
