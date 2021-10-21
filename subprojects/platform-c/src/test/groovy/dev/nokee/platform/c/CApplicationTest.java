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
package dev.nokee.platform.c;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.HasCSourcesTester;
import dev.nokee.language.nativebase.HasPrivateHeadersTester;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.testers.*;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.stream.Stream;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.platform.c.internal.plugins.CApplicationPlugin.cApplication;
import static org.hamcrest.MatcherAssert.assertThat;

class CApplicationTest implements SourceAwareComponentTester<CApplication>, HasBaseNameTester, ComponentTester<CApplication>, DependencyAwareComponentTester<NativeApplicationComponentDependencies>, VariantAwareComponentTester<VariantView<NativeApplication>>, BinaryAwareComponentTester<BinaryView<Binary>>, HasCSourcesTester, HasPrivateHeadersTester {
	private final CApplication subject = createSubject("kdrj");
	@Getter @TempDir File testDirectory;

	@Override
	public CApplication createSubject(String componentName) {
		val project = ProjectTestUtils.createRootProject(getTestDirectory());
		project.getPluginManager().apply(NativeComponentBasePlugin.class);
		val component = project.getExtensions().getByType(ModelRegistry.class).register(cApplication(componentName, project)).as(CApplication.class).get();
		((FunctionalSourceSet) component.getSources()).get(); // force realize all source set
		return component;
	}

	@Override
	public Stream<SourcesUnderTest> provideSourceSetUnderTest() {
		return Stream.of(
			new SourcesUnderTest("c", CSourceSet.class, "cSources"),
			new SourcesUnderTest("headers", NativeHeaderSet.class, "privateHeaders"));
	}

	@Override
	public CApplication subject() {
		return subject;
	}

	@Test
	void hasBaseNameConventionAsComponentName() {
		subject().getBaseName().set((String) null);
		assertThat(subject().getBaseName(), providerOf("kdrj"));
	}
}
