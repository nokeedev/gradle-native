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

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.testers.SourceTester;
import dev.nokee.language.jvm.*;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class JavaNativeInterfaceLibrarySourcesIntegrationTester {
	public abstract JavaNativeInterfaceLibrarySources subject();

	@Test
	void noJavaSourceSetWhenJavaPluginNotApplied() {
		assertThat(subject().get(), not(hasItem(isA(JavaSourceSet.class))));
	}

	@Test
	void throwsSensibleErrorMessageWhenAccessingJavaSourceSetWithoutJavaPluginApplied() {
		val ex = assertThrows(RuntimeException.class, () -> subject().getJava());
		assertThat(ex.getMessage(), equalTo("Please apply 'java' plugin to access Java source set."));
	}

	@Test
	void noGroovySourceSetWhenGroovyPluginNotApplied() {
		assertThat(subject().get(), not(hasItem(isA(JavaSourceSet.class))));
	}

	@Test
	void throwsSensibleErrorMessageWhenAccessingGroovySourceSetWithoutGroovyPluginApplied() {
		val ex = assertThrows(RuntimeException.class, () -> subject().getGroovy());
		assertThat(ex.getMessage(), equalTo("Please apply 'groovy' plugin to access Groovy source set."));
	}

	@Test
	void noKotlinSourceSetWhenKotlinPluginNotApplied() {
		assertThat(subject().get(), not(hasItem(isA(KotlinSourceSet.class))));
	}

	@Test
	void throwsSensibleErrorMessageWhenAccessingKotlinSourceSetWithoutKotlinPluginApplied() {
		val ex = assertThrows(RuntimeException.class, () -> subject().getKotlin());
		assertThat(ex.getMessage(), equalTo("Please apply 'org.jetbrains.kotlin.jvm' plugin to access Kotlin source set."));
	}

	@Nested
	@PluginRequirement.Require(id = "java")
	class JavaTest implements SourceTester<HasJavaSourceSet, JavaSourceSet> {
		@Override
		public HasJavaSourceSet subject() {
			return JavaNativeInterfaceLibrarySourcesIntegrationTester.this.subject();
		}

		@Override
		public NamedDomainObjectProvider<? extends LanguageSourceSet> get(HasJavaSourceSet self) {
			return self.getJava();
		}

		@Override
		public void configure(HasJavaSourceSet self, Action<? super JavaSourceSet> action) {
			self.java(action);
		}

		@Override
		public void configure(HasJavaSourceSet self, @SuppressWarnings("rawtypes") Closure closure) {
			self.java(closure);
		}
	}

	@Nested
	@PluginRequirement.Require(id = "groovy")
	class GroovyTest implements SourceTester<HasGroovySourceSet, GroovySourceSet> {
		@Override
		public HasGroovySourceSet subject() {
			return JavaNativeInterfaceLibrarySourcesIntegrationTester.this.subject();
		}

		@Override
		public NamedDomainObjectProvider<? extends LanguageSourceSet> get(HasGroovySourceSet self) {
			return self.getGroovy();
		}

		@Override
		public void configure(HasGroovySourceSet self, Action<? super GroovySourceSet> action) {
			self.groovy(action);
		}

		@Override
		public void configure(HasGroovySourceSet self, @SuppressWarnings("rawtypes") Closure closure) {
			self.groovy(closure);
		}
	}

	@Nested
	@PluginRequirement.Require(id = "org.jetbrains.kotlin.jvm")
	class KotlinTest implements SourceTester<HasKotlinSourceSet, KotlinSourceSet> {
		@Override
		public HasKotlinSourceSet subject() {
			return JavaNativeInterfaceLibrarySourcesIntegrationTester.this.subject();
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
