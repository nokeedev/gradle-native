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
package dev.nokee.platform.base.internal.dependencies;

import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.platform.base.internal.ComponentName.ofMain;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.*;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Owner.*;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescriptionScheme.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(ConfigurationDescriptionScheme.class)
class ConfigurationDescriptionSchemeTest {
	@Test
	void canCreateDescriptionSchemeOwnedByProject() {
		assertThat(forProject(rootProject()).description(ofName("implementation"), ofDeclarable()),
			equalTo(new ConfigurationDescription(ofName("implementation"), ofDeclarable(), ofProject(rootProject()))));
		assertThat(forProject(rootProject()).description(ofName("linkLibraries"), ofConsumable()),
			equalTo(new ConfigurationDescription(ofName("linkLibraries"), ofConsumable(), ofProject(rootProject()))));
		assertThat(forProject(rootProject()).description(ofName("compileElements"), ofResolvable()),
			equalTo(new ConfigurationDescription(ofName("compileElements"), ofResolvable(), ofProject(rootProject()))));
	}

	@Test
	void canCreateDescriptionSchemeOwnedByComponent() {
		assertThat(forComponent(ofMain()).description(ofName("api"), ofDeclarable()),
			equalTo(new ConfigurationDescription(ofName("api"), ofDeclarable(), ofComponent(ofMain()))));
		assertThat(forComponent(ofMain()).description(ofName("runtimeLibraries"), ofConsumable()),
			equalTo(new ConfigurationDescription(ofName("runtimeLibraries"), ofConsumable(), ofComponent(ofMain()))));
		assertThat(forComponent(ofMain()).description(ofName("runtimeElements"), ofResolvable()),
			equalTo(new ConfigurationDescription(ofName("runtimeElements"), ofResolvable(), ofComponent(ofMain()))));
	}

	@Test
	void canCreateDescriptionSchemeOwnedByVariant() {
		assertThat(forVariant(ofMain(), "debug").description(ofName("compileOnly"), ofDeclarable()),
			equalTo(new ConfigurationDescription(ofName("compileOnly"), ofDeclarable(), ofVariant(ofMain(), "debug"))));
		assertThat(forVariant(ofMain(), "debug").description(ofName("headerSearchPaths"), ofConsumable()),
			equalTo(new ConfigurationDescription(ofName("headerSearchPaths"), ofConsumable(),  ofVariant(ofMain(), "debug"))));
		assertThat(forVariant(ofMain(), "debug").description(ofName("linkElements"), ofResolvable()),
			equalTo(new ConfigurationDescription(ofName("linkElements"), ofResolvable(),  ofVariant(ofMain(), "debug"))));
	}
}
