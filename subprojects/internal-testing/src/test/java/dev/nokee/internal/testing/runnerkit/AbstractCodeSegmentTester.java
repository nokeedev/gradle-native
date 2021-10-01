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

package dev.nokee.internal.testing.runnerkit;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class AbstractCodeSegmentTester<T extends CodeSegment> {
	public abstract T subject();

	@Test
	void canGenerateGroovyDslCode() {
		assertThat(subject().toString(GradleDsl.GROOVY), equalTo(groovyCode()));
	}

	@Test
	void canGenerateKotlinDslCode() {
		assertThat(subject().toString(GradleDsl.KOTLIN), equalTo(kotlinCode()));
	}

	@Test
	void toStringGenerateGroovyDslCode() {
		assertThat(subject().toString(), equalTo(groovyCode()));
	}

	public abstract String groovyCode();
	public abstract String kotlinCode();
}
