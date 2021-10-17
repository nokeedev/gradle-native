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
package dev.nokee.platform.ios;

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.HasPrivateHeadersTester;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.HasObjectiveCSourcesTester;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.HasBaseNameTester;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.platform.ios.internal.plugins.ObjectiveCIosApplicationPlugin.objectiveCIosApplication;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class ObjectiveCIosApplicationTest implements SourceAwareComponentTester<ObjectiveCIosApplication>, HasBaseNameTester, ComponentTester<ObjectiveCIosApplication>, DependencyAwareComponentTester<NativeComponentDependencies>, HasObjectiveCSourcesTester, HasPrivateHeadersTester {
	private final ObjectiveCIosApplication subject = createSubject("bovi");
	@Getter @TempDir File testDirectory;

	@Override
	public ObjectiveCIosApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create((DefaultModelRegistry) project.getExtensions().getByType(ModelRegistry.class), objectiveCIosApplication(componentName, project)).as(ObjectiveCIosApplication.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(new SourcesUnderTest("objectiveC", ObjectiveCSourceSet.class, "objectiveCSources"), new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"), new SourcesUnderTest("resources", IosResourceSet.class, "resources"));
	}

	@Test
	public void hasAdditionalConventionOnObjectiveCSourceSet() throws Throwable {
		val a = new FileSystemWorkspace(getTestDirectory());
		assertThat(createSubject("main").getObjectiveCSources().getSourceDirectories(),
			hasItem(a.file("src/main/objc")));
		assertThat(createSubject("test").getObjectiveCSources().getSourceDirectories(),
			hasItem(a.file("src/test/objc")));
	}

	@Override
	public ObjectiveCIosApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("bovi"));
	}
}
