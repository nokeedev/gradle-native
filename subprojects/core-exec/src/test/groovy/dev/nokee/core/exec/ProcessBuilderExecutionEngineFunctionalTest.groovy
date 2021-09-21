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

import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.util.environment.OperatingSystem

@IgnoreIf({OperatingSystem.current.windows}) // For now.
class ProcessBuilderExecutionEngineFunctionalTest extends Specification {
	def "capture standard output by default"() {
		expect:
		CommandLine.of("bash", "-c", "echo 'bob'").execute(new ProcessBuilderEngine()).waitFor().standardOutput.asString == 'bob\n'
	}

	def "capture the exit value in the result"() {
		expect:
		CommandLine.of("true").execute(new ProcessBuilderEngine()).waitFor().exitValue == 0
		CommandLine.of("false").execute(new ProcessBuilderEngine()).waitFor().exitValue == 1
	}

	// waitFor ensure the process is finished
	// Handle gives access to the process
}
