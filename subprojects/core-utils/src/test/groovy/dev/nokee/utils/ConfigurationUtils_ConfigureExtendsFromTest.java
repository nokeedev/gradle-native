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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.providerFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.extendsFrom;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.configureExtendsFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ConfigurationUtils_ConfigureExtendsFromTest {
	@Test
	void canExtendConfigurationFromConfigurationInstances() {
		assertThat(testConfiguration(configureExtendsFrom(testConfiguration("a"), testConfiguration("b"))),
			extendsFrom(contains(named("a"), named("b"))));
	}

	@Test
	void canExtendConfigurationFromListOfConfiguration() {
		assertThat(testConfiguration(configureExtendsFrom(Arrays.asList(testConfiguration("c"), testConfiguration("d")))),
			extendsFrom(contains(named("c"), named("d"))));
	}

	@Test
	void canExtendConfigurationFromProviderOfConfigurationInstances() {
		assertThat(testConfiguration(configureExtendsFrom(rootProject().getConfigurations().register("e"))),
			extendsFrom(contains(named("e"))));
	}

	@Test
	void ignoresSelfWhenExtendingConfiguration() {
		val subject = testConfiguration();
		assertDoesNotThrow(() -> configureExtendsFrom(testConfiguration("f"), subject, testConfiguration("g")).execute(subject));
		assertThat(subject, extendsFrom(contains(named("f"), named("g"))));
	}

	@Test
	void canExtendConfigurationFromCallableObject() {
		assertThat(testConfiguration(configureExtendsFrom((Callable<Object>) () -> testConfiguration("h"))),
			extendsFrom(contains(named("h"))));
	}

	@Test
	void appendsNewConfigurationWhenExtendingConfiguration() {
		val subject = testConfiguration(it -> it.extendsFrom(testConfiguration("i")));
		configureExtendsFrom(testConfiguration("j")).execute(subject);
		assertThat(subject,	extendsFrom(contains(named("i"), named("j"))));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canCompareConfigurationAction() {
		val test = testConfiguration("test");
		val anotherTest = testConfiguration("anotherTest");
		new EqualsTester()
			.addEqualityGroup(configureExtendsFrom(test), configureExtendsFrom(test))
			.addEqualityGroup(configureExtendsFrom(anotherTest))
			.addEqualityGroup(configureExtendsFrom(test, anotherTest))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureExtendsFrom(testConfiguration("test")),
			hasToString("ConfigurationUtils.configureExtendsFrom(configuration ':test')"));
		assertThat(configureExtendsFrom(providerFactory().provider(() -> testConfiguration("test"))),
			hasToString("ConfigurationUtils.configureExtendsFrom(provider(?))"));
	}
}
