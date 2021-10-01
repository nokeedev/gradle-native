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
package dev.nokee.language.objectivecpp;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.scripts.testing.DefaultImporterMatchers.hasDefaultImportFor;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(ObjectiveCppLanguageBasePlugin.class)
class ObjectiveCppLanguageBasePluginTest {
	@Test
	void appliesLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCppLanguageBasePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(LanguageBasePlugin.class), "should apply language base plugin");
	}

	@Test
	void defaultImportSourceSetTypes() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCppLanguageBasePlugin.class));
		assertThat(project, hasDefaultImportFor(ObjectiveCppSourceSet.class));
		assertThat(project, hasDefaultImportFor(CppHeaderSet.class));
		assertThat(project, hasDefaultImportFor(NativeHeaderSet.class));
	}
}
