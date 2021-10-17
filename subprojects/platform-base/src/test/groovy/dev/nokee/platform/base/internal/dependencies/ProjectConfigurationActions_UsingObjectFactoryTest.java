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
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.junit.jupiter.api.Test;

import java.util.function.BiConsumer;

import static dev.nokee.internal.testing.util.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.using;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.withObjectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class ProjectConfigurationActions_UsingObjectFactoryTest {
	@Test
	void canUseObjectFactoryInDelegateConfigurationAction() {
		val capturedObjectFactory = new MutableObject<ObjectFactory>();
		testConfiguration(using(objectFactory(), withObjectFactory((configuration, objectFactory) -> capturedObjectFactory.setValue(objectFactory))));
		assertThat(capturedObjectFactory.getValue(), equalTo(objectFactory()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualsOfWithObjectFactory() {
		BiConsumer<Configuration, ObjectFactory> doSomething = doSomething();
		BiConsumer<Configuration, ObjectFactory> doSomethingElse = (a, b) -> {};
		new EqualsTester()
			.addEqualityGroup(withObjectFactory(doSomething), withObjectFactory(doSomething))
			.addEqualityGroup(withObjectFactory(doSomethingElse))
			.testEquals();
	}

	@Test
	void checkToStringOfWithObjectFactory() {
		assertThat(withObjectFactory(doSomething()),
			hasToString("ProjectConfigurationUtils.withObjectFactory(doSomething())"));
	}

	private static BiConsumer<Configuration, ObjectFactory> doSomething() {
		return new BiConsumer<Configuration, ObjectFactory>() {
			@Override
			public void accept(Configuration configuration, ObjectFactory objectFactory) {}

			@Override
			public String toString() {
				return "doSomething()";
			}
		};
	}
}
