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
package dev.nokee.utils.internal;

import org.gradle.api.Transformer;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static org.hamcrest.Matchers.equalTo;

class ConfigurationTimeTransformerAdapterIntegrationTests {
	Transformer<String, String> transformer = (Transformer<String, String> & Serializable) it -> "transformed-" + it;
	ConfigurationTimeTransformerAdapter<String, String> subject;
	static final String INPUT = "input-value";

	@BeforeEach
	void createSubject() {
		subject = new ConfigurationTimeTransformerAdapter<>(providerFactory(), "display-name", transformer);
	}

	@Test
	void returnsTransformedInput() {
		MatcherAssert.assertThat(subject.transform(INPUT), equalTo("transformed-input-value"));
	}
}
