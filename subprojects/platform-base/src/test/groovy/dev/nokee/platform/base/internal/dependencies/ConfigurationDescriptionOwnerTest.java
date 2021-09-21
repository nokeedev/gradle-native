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

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.platform.base.internal.ComponentName;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.createChildProject;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.platform.base.internal.ComponentName.of;
import static dev.nokee.platform.base.internal.ComponentName.ofMain;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Owner.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

@Subject(ConfigurationDescription.Owner.class)
class ConfigurationDescriptionOwnerTest {
	@Test
	void thisProjectOwner() {
		assertThat(ofThisProject(), hasToString("this project"));
	}

	@Test
	void rootProjectOwner() {
		assertThat(ofProject(rootProject()), hasToString("project ':'"));
	}

	@Test
	void projectOwner() {
		assertThat(ofProject(createChildProject(rootProject(), "foo")), hasToString("project ':foo'"));
	}

	@Test
	void mainComponentOwner() {
		assertThat(ofComponent(ofMain()), hasToString("main component"));
	}

	@Test
	void nonMainComponentOwner() {
		assertThat(ofComponent(of("test")), hasToString("component 'test'"));
		assertThat(ofComponent(of("integTest")), hasToString("component 'integTest'"));
	}

	@Test
	void variantOwnerOfMainComponent() {
		assertThat(ofVariant(ofMain(), "macos"), hasToString("variant 'macos'"));
	}

	@Test
	void variantOwnerOfNonMainComponent() {
		assertThat(ofVariant(of("test"), "windows"), hasToString("variant 'windows' of component 'test'"));
		assertThat(ofVariant(of("integTest"), "x86Debug"), hasToString("variant 'x86Debug' of component 'integTest'"));
	}

	@Test
	void defaultVariantOwnerOfMainComponent() {
		assertThat(ofVariant(ofMain(), ""), hasToString("main component"));
	}

	@Test
	void defaultVariantOwnerOfNonMainComponent() {
		assertThat(ofVariant(of("uiTest"), ""), hasToString("component 'uiTest'"));
		assertThat(ofVariant(of("unitTest"), ""), hasToString("component 'unitTest'"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ComponentName.class, ofMain()).testAllPublicStaticMethods(ConfigurationDescription.Owner.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofProject(rootProject()), ofProject(rootProject()))
			.addEqualityGroup(ofProject(createChildProject(rootProject())))
			.addEqualityGroup(ofComponent(ofMain()), ofComponent(ofMain()), ofVariant(ofMain(), ""))
			.addEqualityGroup(ofVariant(ofMain(), "debug"), ofVariant(ofMain(), "debug"))
			.addEqualityGroup(ofVariant(of("test"), "debug"))
			.addEqualityGroup(ofVariant(of("test"), "macos"))
			.addEqualityGroup(ofVariant(ofMain(), "macos"))
			.addEqualityGroup(ofThisProject(), ofThisProject())
			.testEquals();
	}
}
