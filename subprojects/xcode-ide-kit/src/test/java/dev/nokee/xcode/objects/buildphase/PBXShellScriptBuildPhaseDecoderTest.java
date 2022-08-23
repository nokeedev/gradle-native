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
import dev.nokee.xcode.project.PBXObjectFields;
import dev.nokee.xcode.project.PBXObjectReference;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProj;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class PBXShellScriptBuildPhaseDecoderTest {
	@Test
	void canDecodeShellScriptBuildPhase() {
		val project = new PBXObjectUnarchiver().decode(project(builder -> {
			builder.putField("name", "Run 'hello world'");
			builder.putField("shellScript", "echo \"Hello, world!\"\n");
			builder.putField("shellPath", "/bin/zsh");
			builder.putField("outputPaths", ImmutableList.of("/path/to/output.txt"));
			builder.putField("inputPaths", ImmutableList.of("/path/to/input.txt"));
			builder.putField("inputFileListPaths", ImmutableList.of("/path/to/inputs.xcfilelist"));
			builder.putField("outputFileListPaths", ImmutableList.of("/path/to/outputs.xcfilelist"));
		}));
		assertThat("expect one target", project.getTargets(), hasSize(1));
		assertThat("expect on build phase", project.getTargets().get(0).getBuildPhases(), hasSize(1));

		val subject = (PBXShellScriptBuildPhase) project.getTargets().get(0).getBuildPhases().get(0);
		assertThat(subject.getName(), optionalWithValue(equalTo("Run 'hello world'")));
		assertThat(subject.getShellScript(), equalTo("echo \"Hello, world!\"\n"));
		assertThat(subject.getShellPath(), equalTo("/bin/zsh"));
		assertThat(subject.getInputPaths(), contains("/path/to/input.txt"));
		assertThat(subject.getOutputPaths(), contains("/path/to/output.txt"));
		assertThat(subject.getInputFileListPaths(), contains("/path/to/inputs.xcfilelist"));
		assertThat(subject.getOutputFileListPaths(), contains("/path/to/outputs.xcfilelist"));
	}

	@Test
	void canDecodeShellScriptBuildPhaseWithMinimalFields() {
		val project = new PBXObjectUnarchiver().decode(project(builder -> {
			builder.putField("shellScript", "echo \"Hello, world!\"\n");
			builder.putField("shellPath", "/bin/zsh");
		}));
		assertThat("expect one target", project.getTargets(), hasSize(1));
		assertThat("expect on build phase", project.getTargets().get(0).getBuildPhases(), hasSize(1));

		val subject = (PBXShellScriptBuildPhase) project.getTargets().get(0).getBuildPhases().get(0);
		assertThat(subject.getName(), emptyOptional());
		assertThat(subject.getInputPaths(), emptyIterable());
		assertThat(subject.getOutputPaths(), emptyIterable());
		assertThat(subject.getInputFileListPaths(), emptyIterable());
		assertThat(subject.getOutputFileListPaths(), emptyIterable());
	}

	private static PBXProj project(Consumer<? super PBXObjectFields.Builder> shellScriptBuildPhaseAction) {
		return PBXProj.builder().objects(builder -> {
			builder.add(PBXObjectReference.of("1", it -> {
				shellScriptBuildPhaseAction.accept(it);
				it.putField("isa", "PBXShellScriptBuildPhase");
			}));
			builder.add(PBXObjectReference.of("2", it -> {
				it.putField("isa", "XCBuildConfiguration");
				it.putField("name", "Release");
			}));
			builder.add(PBXObjectReference.of("3", it -> {
				it.putField("isa", "XCConfigurationList");
				it.putField("buildConfigurations", ImmutableList.of("2"));
			}));
			builder.add(PBXObjectReference.of("4", it -> {
				it.putField("isa", "PBXAggregateTarget");
				it.putField("name", "a-target");
				it.putField("buildPhases", ImmutableList.of("1"));
				it.putField("buildConfigurationList", "3");
			}));
			builder.add(PBXObjectReference.of("5", it -> {
				it.putField("isa", "PBXProject");
				it.putField("targets", ImmutableList.of("4"));
			}));
		}).rootObject("5").build();
	}
}
