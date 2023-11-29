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
import dev.nokee.language.jvm.GroovySourceSet;
import dev.nokee.language.jvm.HasGroovySourceSet;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
@PluginRequirement.Require(id = "groovy")
class JavaNativeInterfaceLibraryComponentGroovyPluginIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		this.subject = components(project).register("kefi", JniLibraryComponentInternal.class).get();
		subject.getTargetMachines().set(ImmutableSet.of(TargetMachines.host()));
	}

	@Test
	void hasGroovySourceSetWhenGroovyPluginApplied() {
		assertThat(subject.getSources().get(), hasItem(allOf(named("kefiGroovy"), isA(GroovySourceSet.class))));
	}

	@Test
	void hasJvmJarBinaryWhenGroovyPluginApplied() {
		assertThat(subject.getBinaries().get(), hasItem(allOf(named("kefiJvmJar"), isA(JvmJarBinary.class))));
	}

	// TODO: Test Jar task doesn't have JVM jar binary name

	@Nested
	class GroovyComponentSourcesTest implements SourceTester<HasGroovySourceSet, GroovySourceSet> {
		@Override
		public HasGroovySourceSet subject() {
			return subject.getSources();
		}

		@Override
		public NamedDomainObjectProvider<? extends LanguageSourceSet> get(HasGroovySourceSet self) {
			return self.getGroovy();
		}

		@Override
		public void configure(HasGroovySourceSet self, Action<? super GroovySourceSet> action) {
			self.groovy(action);
		}
	}
}
