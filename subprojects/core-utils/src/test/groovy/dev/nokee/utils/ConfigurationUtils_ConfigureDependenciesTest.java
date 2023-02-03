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
import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.invocations.InvocationMatchers.calledOnceWith;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDoubleTypes.ofBiConsumer;
import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.add;
import static dev.nokee.utils.ConfigurationUtils.configureDependencies;
import static dev.nokee.utils.ConsumerTestUtils.aBiConsumer;
import static dev.nokee.utils.ConsumerTestUtils.anotherBiConsumer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;

class ConfigurationUtils_ConfigureDependenciesTest {
	@Test
	void callsBackWithConfigurationAndDependencySet() {
		val subject = testConfiguration();
		val action = newMock(ofBiConsumer(Object.class, Object.class));
		configureDependencies(action.instance()).execute(subject);
		assertThat(action.to(method(BiConsumer<Object, Object>::accept)), calledOnceWith(subject, subject.getDependencies()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureDependencies(aBiConsumer()), configureDependencies(aBiConsumer()))
			.addEqualityGroup(configureDependencies(anotherBiConsumer()))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDependencies(aBiConsumer()),
			hasToString("ConfigurationUtils.configureDependencies(aBiConsumer())"));
		assertThat(configureDependencies(anotherBiConsumer()),
			hasToString("ConfigurationUtils.configureDependencies(anotherBiConsumer())"));
	}

	@Nested
	class AddTest {
		@Test
		void canAddMappedDependency() {
			val subject = testConfiguration(
				configureDependencies(add(ignored -> ProjectTestUtils.createDependency("com.example:foo:4.2"))));
			assertThat(subject, ConfigurationMatchers.dependencies(contains(forCoordinate("com.example", "foo", "4.2"))));
		}
	}
}
