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
package dev.nokee.platform.objectivec;

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.HasPrivateHeadersTester;
import dev.nokee.language.nativebase.HasPublicHeadersTester;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.HasObjectiveCSourcesTester;
import dev.nokee.language.objectivec.ObjectiveCSourceSet;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.testers.*;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.objectivec.internal.plugins.ObjectiveCLibraryPlugin.objectiveCLibrary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

class ObjectiveCLibraryTest implements SourceAwareComponentTester<ObjectiveCLibrary>, HasBaseNameTester, ComponentTester<ObjectiveCLibrary>, DependencyAwareComponentTester<NativeLibraryComponentDependencies>, VariantAwareComponentTester<VariantView<NativeLibrary>>, BinaryAwareComponentTester<BinaryView<Binary>>, HasObjectiveCSourcesTester, HasPrivateHeadersTester, HasPublicHeadersTester {
	private final ObjectiveCLibrary subject = createSubject("weqi");
	@Getter @TempDir File testDirectory;

	@Override
	public ObjectiveCLibrary createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = project.getExtensions().getByType(ModelRegistry.class).register(objectiveCLibrary(componentName, project)).as(ObjectiveCLibrary.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("objectiveC", ObjectiveCSourceSet.class, "objectiveCSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"),
			new SourcesUnderTest("public", NativeHeaderSet.class, "publicHeaders"));
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
	public ObjectiveCLibrary subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("weqi"));
	}
}
