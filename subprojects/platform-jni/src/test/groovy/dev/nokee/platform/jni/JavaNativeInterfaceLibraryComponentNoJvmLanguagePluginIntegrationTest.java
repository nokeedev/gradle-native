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
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.runtime.nativebase.internal.TargetMachines;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.components;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;

@PluginRequirement.Require(id = "dev.nokee.jni-library-base")
class JavaNativeInterfaceLibraryComponentNoJvmLanguagePluginIntegrationTest extends AbstractPluginTest {
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		this.subject = components(project).register("kuvu", JniLibraryComponentInternal.class).get();
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
}
