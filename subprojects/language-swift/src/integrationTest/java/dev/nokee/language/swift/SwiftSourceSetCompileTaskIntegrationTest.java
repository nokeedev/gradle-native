/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.language.swift;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.TaskMatchers;
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.language.nativebase.NativeCompileTaskObjectFilesTester;
import dev.nokee.language.nativebase.NativeCompileTaskTester;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.registry.ModelRegistry;
import org.gradle.language.swift.SwiftVersion;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.parentFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftSourceSetCompileTaskIntegrationTest extends AbstractPluginTest implements SwiftCompileTester
	, NativeCompileTaskTester
	, NativeCompileTaskObjectFilesTester<SwiftCompileTask>
{
	@Subject SwiftCompileTask subject;

	@Override
	public SwiftCompileTask subject() {
		return subject;
	}

	SwiftCompileTask createSubject() {
		project.getPluginManager().apply(SwiftCompilerPlugin.class);
		return project.getExtensions().getByType(ModelRegistry.class).register(newEntity("rubi", SwiftSourceSetSpec.class).build()).element("compile", SwiftCompileTask.class).get();
	}

	@BeforeEach
	void configureTargetPlatform() {
		subject.getTargetPlatform().set(create(of("macos-x64")));
	}

	@Test
	void hasName() {
		assertThat(subject, named("compileRubi"));
	}

	@Test
	void hasDescription() {
		assertThat(subject, TaskMatchers.description("Compiles the sources ':rubi'."));
	}

	@Test
	void disablesDebuggableByDefault() {
		assertThat(subject().getDebuggable().value((Boolean) null), providerOf(false));
	}

	@Test
	void disablesOptimizationByDefault() {
		assertThat(subject().getOptimized().value((Boolean) null), providerOf(false));
	}

	@Test
	void defaultsModuleNameToSourceSetName() {
		assertThat(subject().getModuleName(), providerOf("Rubi"));
	}

	@Test
	void defaultsSourceCompatibilityToSwift5() {
		assertThat(subject().getSourceCompatibility(), providerOf(SwiftVersion.SWIFT5));
	}

	@Test
	void hasModuleFileUnderModulesInsideBuildDirectory() {
		assertThat(subject().getModuleFile(),
			providerOf(aFile(withAbsolutePath(containsString("/build/modules/")))));
	}

	@Test
	void includesTargetNameInModuleFile() {
		assertThat(subject().getModuleFile(), providerOf(aFile(parentFile(withAbsolutePath(endsWith("/rubi"))))));
	}

	@Test
	@EnabledOnOs(OS.MAC)
	void addsMacOsSdkPathToCompilerArguments() {
		subject().getTargetPlatform().set(create(of("macos-x64")));
		assertThat(subject().getCompilerArgs(), providerOf(hasItem("-sdk")));
	}
}
