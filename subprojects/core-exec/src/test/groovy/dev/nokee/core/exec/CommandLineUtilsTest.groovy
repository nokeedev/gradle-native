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
