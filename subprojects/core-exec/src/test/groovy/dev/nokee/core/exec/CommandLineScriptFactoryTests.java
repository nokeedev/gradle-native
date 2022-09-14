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
package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static dev.nokee.core.exec.CommandLine.script;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CommandLineScriptFactoryTests {
	@Test
	@EnabledOnOs(OS.WINDOWS)
	void canCreateCommandLineForCmdOnWindows() {
		val scriptCommand = script("dir", "C:\\some\\path");
		assertThat(scriptCommand.getTool(), equalTo(CommandLineTool.of("cmd")));
		assertThat(scriptCommand.getArguments(), equalTo(new CommandLineToolArguments(ImmutableList.of("/c", "dir C:\\some\\path"))));
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	void canCreateCommandLineForCmdOnNix() {
		val scriptCommand = script("ls", "-l", "/some/path");
		assertThat(scriptCommand.getTool(), equalTo(CommandLineTool.of("/bin/bash")));
		assertThat(scriptCommand.getArguments(), equalTo(new CommandLineToolArguments(ImmutableList.of("-c", "ls -l /some/path"))));
	}
}
