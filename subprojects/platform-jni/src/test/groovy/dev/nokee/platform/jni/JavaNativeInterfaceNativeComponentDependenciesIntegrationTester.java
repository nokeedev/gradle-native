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

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.testers.DependencyBucketTester;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class JavaNativeInterfaceNativeComponentDependenciesIntegrationTester {
	public abstract JavaNativeInterfaceNativeComponentDependencies subject();

	public abstract String variantName();

	@Nested
	class NativeImplementationTest implements DependencyBucketTester<JavaNativeInterfaceNativeComponentDependencies> {
		@Override
		public JavaNativeInterfaceNativeComponentDependencies subject() {
			return JavaNativeInterfaceNativeComponentDependenciesIntegrationTester.this.subject();
		}

		@Override
		public DependencyBucket get(JavaNativeInterfaceNativeComponentDependencies self) {
			return subject().getNativeImplementation();
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation) {
			subject().nativeImplementation(notation);
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation, Action<? super ModuleDependency> action) {
			subject().nativeImplementation(notation, action);
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation, @SuppressWarnings("rawtypes") Closure closure) {
			subject().nativeImplementation(notation, closure);
		}

		@Test
		void hasConfigurationWithProperName() {
			assertThat(subject().getNativeImplementation().getAsConfiguration(), named(variantName() + "NativeImplementation"));
		}
	}

	@Nested
	class NativeLinkOnlyTest implements DependencyBucketTester<JavaNativeInterfaceNativeComponentDependencies> {
		@Override
		public JavaNativeInterfaceNativeComponentDependencies subject() {
			return JavaNativeInterfaceNativeComponentDependenciesIntegrationTester.this.subject();
		}

		@Override
		public DependencyBucket get(JavaNativeInterfaceNativeComponentDependencies self) {
			return subject().getNativeLinkOnly();
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation) {
			subject().nativeLinkOnly(notation);
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation, Action<? super ModuleDependency> action) {
			subject().nativeLinkOnly(notation, action);
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation, @SuppressWarnings("rawtypes") Closure closure) {
			subject().nativeLinkOnly(notation, closure);
		}

		@Test
		void hasConfigurationWithProperName() {
			assertThat(subject().getNativeLinkOnly().getAsConfiguration(), named(variantName() + "NativeLinkOnly"));
		}
	}

	@Nested
	class NativeRuntimeOnlyTest implements DependencyBucketTester<JavaNativeInterfaceNativeComponentDependencies> {
		@Override
		public JavaNativeInterfaceNativeComponentDependencies subject() {
			return JavaNativeInterfaceNativeComponentDependenciesIntegrationTester.this.subject();
		}

		@Override
		public DependencyBucket get(JavaNativeInterfaceNativeComponentDependencies self) {
			return subject().getNativeRuntimeOnly();
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation) {
			subject().nativeRuntimeOnly(notation);
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation, Action<? super ModuleDependency> action) {
			subject().nativeRuntimeOnly(notation, action);
		}

		@Override
		public void addDependency(JavaNativeInterfaceNativeComponentDependencies self, Object notation, @SuppressWarnings("rawtypes") Closure closure) {
			subject().nativeRuntimeOnly(notation, closure);
		}

		@Test
		void hasConfigurationWithProperName() {
			assertThat(subject().getNativeRuntimeOnly().getAsConfiguration(), named(variantName() + "NativeRuntimeOnly"));
		}
	}
}
