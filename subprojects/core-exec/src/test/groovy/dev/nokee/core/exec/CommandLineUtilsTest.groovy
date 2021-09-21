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

import static dev.nokee.core.exec.CommandLineUtils.scriptCommandLine

@Subject(CommandLineUtils)
class CommandLineUtilsTest extends Specification {
	@Requires({ SystemUtils.IS_OS_WINDOWS })
	def "use cmd terminal on Windows"() {
		expect:
		scriptCommandLine == ['cmd', '/c']
	}

	@Requires({ !SystemUtils.IS_OS_WINDOWS })
	def "use cmd terminal on *nix"() {
		expect:
		scriptCommandLine == ['/bin/bash', '-c']
	}
}
