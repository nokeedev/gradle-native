/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode.objects.configuration;

import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.objects.configuration.BuildSettings.builder;
import static dev.nokee.xcode.objects.configuration.BuildSettings.empty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;

class BuildSettingsEmptyTest {
	@Test
	void canBuildUsingBuilder() {
		assertThat(builder().build(), equalTo(empty()));
	}

	@Test
	void hasZeroSize() {
		assertThat(empty().size(), equalTo(0));
	}

	@Test
	void returnsEmptyMap() {
		assertThat(empty().asMap(), anEmptyMap());
	}
}
