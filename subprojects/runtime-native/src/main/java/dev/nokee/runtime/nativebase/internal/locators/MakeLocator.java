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
package dev.nokee.runtime.nativebase.internal.locators;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.DefaultCommandLineToolDescriptor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeLocator implements CommandLineToolLocator {
	@Override
	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		File tool = OperatingSystem.current().findInPath("make");
		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asMakeVersion());
		return Collections.singleton(new DefaultCommandLineToolDescriptor(tool, version.toString()));
	}

	@Override
	public Set<String> getKnownTools() {
		return Collections.singleton("make");
	}

	private static final Pattern MAKE_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?)");
	static CommandLineToolOutputParser<VersionNumber> asMakeVersion() {
		return content -> {
			Matcher matcher = MAKE_VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return VersionNumber.parse(matcher.group(1));
			}
			// TODO: Print better error message
			throw new RuntimeException("Invalid version");
		};
	}
}
