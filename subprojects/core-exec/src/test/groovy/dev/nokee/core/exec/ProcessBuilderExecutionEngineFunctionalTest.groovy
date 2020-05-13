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
