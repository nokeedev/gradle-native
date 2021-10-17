/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.objectivecpp;

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.HasPrivateHeadersTester;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivecpp.HasObjectiveCppSourcesTester;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.objectivecpp.internal.plugins.ObjectiveCppApplicationPlugin.objectiveCppApplication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class ObjectiveCppApplicationTest implements SourceAwareComponentTester<ObjectiveCppApplication>, HasBaseNameTester, ComponentTester<ObjectiveCppApplication>, DependencyAwareComponentTester<NativeApplicationComponentDependencies>, HasObjectiveCppSourcesTester, HasPrivateHeadersTester {
	private final ObjectiveCppApplication subject = createSubject("bacu");
	@Getter @TempDir File testDirectory;

	@Override
	public ObjectiveCppApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create((DefaultModelRegistry) project.getExtensions().getByType(ModelRegistry.class), objectiveCppApplication(componentName, project)).as(ObjectiveCppApplication.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("objectiveCpp", ObjectiveCppSourceSet.class, "objectiveCppSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"));
	}


	@Test
	public void hasAdditionalConventionOnObjectiveCppSourceSet() throws Throwable {
		val a = new FileSystemWorkspace(getTestDirectory());
		assertThat(createSubject("main").getObjectiveCppSources().getSourceDirectories(),
			hasItem(a.file("src/main/objcpp")));
		assertThat(createSubject("test").getObjectiveCppSources().getSourceDirectories(),
			hasItem(a.file("src/test/objcpp")));
	}

	@Override
	public ObjectiveCppApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("bacu"));
	}
}
