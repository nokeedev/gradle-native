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
import dev.nokee.internal.testing.junit.jupiter.Subject;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.testers.HasCompileTaskTester;
import dev.nokee.language.base.testers.HasConfigurableSourceTester;
import dev.nokee.language.base.testers.LanguageSourceSetHasBuildableCompileTaskIntegrationTester;
import dev.nokee.language.base.testers.LanguageSourceSetHasBuildableSourceIntegrationTester;
import dev.nokee.language.base.testers.LanguageSourceSetTester;
import dev.nokee.language.nativebase.LanguageSourceSetHasCompiledSourceIntegrationTester;
import dev.nokee.language.nativebase.LanguageSourceSetNativeCompileTaskIntegrationTester;
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetSpec;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.testers.HasPublicTypeTester;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createDependency;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;
import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.ConfigurationUtils.forUsage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;

@PluginRequirement.Require(id = "dev.nokee.swift-language-base")
class SwiftSourceSetIntegrationTest extends AbstractPluginTest implements LanguageSourceSetTester
	, HasPublicTypeTester<SwiftSourceSet>
	, HasConfigurableSourceTester
	, HasCompileTaskTester
	, LanguageSourceSetHasBuildableSourceIntegrationTester<SwiftSourceSetSpec>
	, LanguageSourceSetHasBuildableCompileTaskIntegrationTester<SwiftSourceSetSpec>
	, LanguageSourceSetHasCompiledSourceIntegrationTester<SwiftSourceSetSpec>
	, LanguageSourceSetNativeCompileTaskIntegrationTester<SwiftSourceSetSpec>
{
	@Subject DomainObjectProvider<SwiftSourceSetSpec> subject;

	DomainObjectProvider<SwiftSourceSetSpec> createSubject() {
		return project.getExtensions().getByType(ModelRegistry.class).register(newEntity("riku", SwiftSourceSetSpec.class)).as(SwiftSourceSetSpec.class);
	}

	@BeforeEach
	public void configureTargetPlatform() {
		subject().getCompileTask().get().getTargetPlatform().set(create(of("macos-x64")));
	}

	@Override
	public SwiftSourceSetSpec subject() {
		return subject.get();
	}

	private Configuration importModules() {
		return subject.element("importModules", Configuration.class).get();
	}

	@Test
	public void hasName() {
		assertThat(subject(), named("riku"));
	}

	@Test
	void hasToString() {
		assertThat(subject(), Matchers.hasToString("Swift sources 'riku'"));
	}

	@Test
	public void hasCompileTask() {
		assertThat(subject().getCompileTask(), providerOf(isA(SwiftCompile.class)));
	}

	@Test
	void linksImportModulesConfigurationToCompileTaskModules() throws IOException {
		val module = Files.createTempDirectory("Foo.swiftmodule").toFile();
		importModules().getDependencies().add(createDependency(objectFactory().fileCollection().from(module)));
		assertThat(subject().getCompileTask().get().getModules(), hasItem(aFile(module)));
	}

	@Test
	void linksImportModulesConfigurationToCompileTaskAsFrameworkCompileArguments() throws IOException {
		val artifact = Files.createTempDirectory("Sifo.framework").toFile();
		val frameworkProducer = ProjectTestUtils.createChildProject(project());
		frameworkProducer.getConfigurations().create("apiElements",
			configureAsConsumable()
				.andThen(configureAttributes(forUsage(project().getObjects().named(Usage.class, Usage.SWIFT_API))))
				.andThen(configureAttributes(it -> it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
					project().getObjects().named(LibraryElements.class, "framework-bundle"))))
				.andThen(it -> it.getOutgoing().artifact(artifact, t -> t.setType("framework")))
		);

		importModules().getDependencies().add(createDependency(frameworkProducer));
		assertThat(subject().getCompileTask().get().getModules(),
			not(hasItem(aFile(artifact))));
		assertThat(subject().getCompileTask().get().getCompilerArgs(),
			providerOf(containsInRelativeOrder("-F", artifact.getParentFile().getAbsolutePath())));
	}
}
