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
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationUtils_ConfigureAsDeclarableTest {
	@Test
	void canConfigureConfigurationAsDeclarableBucket()  {
		val configuration = testConfiguration(configureAsDeclarable());
		assertThat("should not be consumable", configuration.isCanBeConsumed(), equalTo(false));
		assertThat("should not be resolvable", configuration.isCanBeResolved(), equalTo(false));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureAsDeclarable(), configureAsDeclarable())
			.addEqualityGroup(configureAsResolvable())
			.addEqualityGroup(configureAsConsumable())
			.addEqualityGroup((Action<Configuration>) it -> {})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureAsDeclarable(), hasToString("ConfigurationUtils.configureAsDeclarable()"));
	}
}
