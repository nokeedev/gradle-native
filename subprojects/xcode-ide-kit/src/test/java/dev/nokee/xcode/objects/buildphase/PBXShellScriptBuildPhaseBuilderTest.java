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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;

class PBXShellScriptBuildPhaseBuilderTest {
	@Test
	void canBuildEmptyBuildPhase() {
		val subject = builder().build();
		assertThat(subject.getName(), emptyOptional());
		assertThat(subject.getShellPath(), equalTo("/bin/sh"));
		assertThat(subject.getShellScript(), emptyString());
		assertThat(subject.getInputPaths(), emptyIterable());
		assertThat(subject.getOutputPaths(), emptyIterable());
		assertThat(subject.getInputFileListPaths(), emptyIterable());
		assertThat(subject.getOutputFileListPaths(), emptyIterable());
	}

	@Test
	void canConfigureShellPath() {
		val subject = builder().shellPath("/bin/bash").build();
		assertThat(subject.getShellPath(), equalTo("/bin/bash"));
	}

	@Test
	void canConfigureShellScript() {
		val subject = builder().shellScript("echo \"Hello, world!\"\n").build();
		assertThat(subject.getShellScript(), equalTo("echo \"Hello, world!\"\n"));
	}

	@Test
	void canConfigureInputPaths() {
		val subject = builder().inputPaths(ImmutableList.of("$(SRCROOT)/input.txt")).build();
		assertThat(subject.getInputPaths(), contains("$(SRCROOT)/input.txt"));
	}

	@Test
	void canConfigureOutputPaths() {
		val subject = builder().outputPaths(ImmutableList.of("$(DERIVED_DIR)/output.txt")).build();
		assertThat(subject.getOutputPaths(), contains("$(DERIVED_DIR)/output.txt"));
	}

	@Test
	void canConfigureInputFileListPaths() {
		val subject = builder().inputFileListPaths(ImmutableList.of("$(SRCROOT)/inputs.xcfilelist")).build();
		assertThat(subject.getInputFileListPaths(), contains("$(SRCROOT)/inputs.xcfilelist"));
	}

	@Test
	void canConfigureOutputFileListPaths() {
		val subject = builder().outputFileListPaths(ImmutableList.of("$(DERIVED_DIR)/outputs.xcfilelist")).build();
		assertThat(subject.getOutputFileListPaths(), contains("$(DERIVED_DIR)/outputs.xcfilelist"));
	}
}
