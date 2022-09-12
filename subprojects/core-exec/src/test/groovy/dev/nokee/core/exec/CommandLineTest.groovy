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
package dev.nokee.core.exec

import org.apache.commons.lang3.SystemUtils
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.core.exec.CommandLine.script

@Subject(CommandLine)
class CommandLineTest extends Specification {
	@Requires({ SystemUtils.IS_OS_WINDOWS })
	def "can create command line for cmd on Windows"() {
		expect:
		def scriptCommand = script('dir', 'C:\\some\\path')
		scriptCommand.tool.executable == 'cmd'
		scriptCommand.arguments.get() == ['/c', 'dir C:\\some\\path']
	}

	@Requires({ !SystemUtils.IS_OS_WINDOWS })
	def "can create command line for cmd on *nix"() {
		expect:
		def scriptCommand = script('ls', '-l', '/some/path')
		scriptCommand.tool.executable == '/bin/bash'
		scriptCommand.arguments.get() == ['-c', 'ls -l /some/path']
	}
}
