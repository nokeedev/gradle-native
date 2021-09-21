/*
 * Copyright 2020 the original author or authors.
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
package dev.gradleplugins.exemplarkit.testers;

import dev.gradleplugins.exemplarkit.Step;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public abstract class StepTester {
	public abstract Step getStepUnderTest();
	public abstract String getExpectedExecutable();

	public List<String> getExpectedArguments() {
		return Collections.emptyList();
	}

	public Map<String, Object> getExpectedAttributes() {
		return Collections.emptyMap();
	}

	@Nullable
	public String getExpectedOutput() {
		return null;
	}

	@Test
	void hasExecutable() {
		assertThat(getStepUnderTest().getExecutable(), equalTo(getExpectedExecutable()));
	}

	@Test
	void hasArguments() {
		assertThat(getStepUnderTest().getArguments(), contains(getExpectedArguments().toArray(new String[0])));
	}

	@Test
	void hasAttributes() {
		assertThat(getStepUnderTest().getAttributes(), equalTo(getExpectedAttributes()));
	}

	@Test
	void hasOutput() {
		assertThat(getStepUnderTest().getOutput().orElse(null), equalTo(getExpectedOutput()));
	}
}
