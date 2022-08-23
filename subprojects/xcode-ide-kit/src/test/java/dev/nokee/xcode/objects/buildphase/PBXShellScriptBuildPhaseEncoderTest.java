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
import com.google.common.collect.MoreCollectors;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.project.PBXObjectArchiver;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class PBXShellScriptBuildPhaseEncoderTest {
	@Test
	void canEncodeShellScriptBuildPhase() {
		val proj = new PBXObjectArchiver().encode(project(builder -> {
			builder.name("Bar");
			builder.shellPath("/bin/bash");
			builder.shellScript("echo \"Good-bye!\"\n");
			builder.inputPaths(ImmutableList.of("$(SRCROOT)/input.txt"));
			builder.outputPaths(ImmutableList.of("$(DERIVED_DIR)/output.txt"));
			builder.inputFileListPaths(ImmutableList.of("$(SRCROOT)/inputs.xcfilelist"));
			builder.outputFileListPaths(ImmutableList.of("$(DERIVED_DIR)/outputs.xcfilelist"));
		}));

		val subject = proj.getObjects().get("PBXShellScriptBuildPhase").collect(MoreCollectors.onlyElement()).getFields();
		assertThat(subject.get("name"), equalTo("Bar"));
		assertThat(subject.get("shellPath"), equalTo("/bin/bash"));
		assertThat(subject.get("shellScript"), equalTo("echo \"Good-bye!\"\n"));
		assertThat(subject.get("inputPaths"), equalTo(ImmutableList.of("$(SRCROOT)/input.txt")));
		assertThat(subject.get("outputPaths"), equalTo(ImmutableList.of("$(DERIVED_DIR)/output.txt")));
		assertThat(subject.get("inputFileListPaths"), equalTo(ImmutableList.of("$(SRCROOT)/inputs.xcfilelist")));
		assertThat(subject.get("outputFileListPaths"), equalTo(ImmutableList.of("$(DERIVED_DIR)/outputs.xcfilelist")));
	}

	private static PBXProject project(Consumer<? super PBXShellScriptBuildPhase.Builder> shellScriptBuildPhaseAction) {
		val builder = PBXShellScriptBuildPhase.builder();
		shellScriptBuildPhaseAction.accept(builder);
		return PBXProject.builder()
			.target(PBXAggregateTarget.builder()
				.name("Foo")
				.buildPhases(ImmutableList.of(builder.build()))
				.buildConfigurations(XCConfigurationList.builder().buildConfiguration(it -> it.name("Release")).build())
				.build())
			.build();
	}
}
