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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.fixtures.NativeComponentMatchers;
import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.testers.BaseNameAwareComponentTester;
import dev.nokee.platform.base.testers.SourceAwareComponentTester;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spock.lang.Subject;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.model.fixtures.ModelRegistryTestUtils.create;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.registry;
import static dev.nokee.platform.objectivecpp.internal.plugins.ObjectiveCppLibraryPlugin.objectiveCppLibrary;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

@Subject(ObjectiveCppLibrary.class)
class ObjectiveCppLibraryTest implements SourceAwareComponentTester<ObjectiveCppLibrary>, BaseNameAwareComponentTester {
	@Getter @TempDir File testDirectory;

	@Override
	public ObjectiveCppLibrary createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = create(registry(project.getObjects()), objectiveCppLibrary(componentName, project));
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Matcher<Component> hasArtifactBaseNameOf(String name) {
		return NativeComponentMatchers.hasArtifactBaseNameOf(name);
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("objectiveCpp", ObjectiveCppSourceSet.class, "objectiveCppSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"),
			new SourcesUnderTest("public", NativeHeaderSet.class, "publicHeaders"));
	}

	@Test
	public void hasAdditionalConventionOnObjectiveCppSourceSet() throws Throwable {
		val a = new FileSystemWorkspace(getTestDirectory());
		assertThat(createSubject("main").getObjectiveCppSources().getSourceDirectories(),
			hasItem(a.file("src/main/objcpp")));
		assertThat(createSubject("test").getObjectiveCppSources().getSourceDirectories(),
			hasItem(a.file("src/test/objcpp")));
	}
}
