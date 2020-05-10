package dev.nokee.ide.xcode.internal.plugins

import spock.lang.Ignore
import spock.lang.Specification

class XcodeIdeRequestTest extends Specification {
	def "can parse task name with explicit action"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective-c-ios-application_ObjectiveCIosApplication_Default")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'ObjectiveCIosApplication'
		request.configuration == 'Default'
	}

	def "can parse task name when project name contains underscores"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective_c_ios_application_ObjectiveCIosApplication_Default")

		then:
		request.action == 'build'
		request.projectName == 'objective_c_ios_application'
		request.targetName == 'ObjectiveCIosApplication'
		request.configuration == 'Default'
	}

	@Ignore
	def "can parse task name when target name contains underscores"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective-c-ios-application_Objective_C_Ios_Application_Default")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'Objective_C_Ios_Application'
		request.configuration == 'Default'
	}

	def "can parse task name when target name contains dashes"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective-c-ios-application_Objective-C-Ios-Application_Default")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'Objective-C-Ios-Application'
		request.configuration == 'Default'
	}

	def "can parse task name when configuration name contains dashes"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective-c-ios-application_ObjectiveCIosApplication_Debug-Optimized")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'ObjectiveCIosApplication'
		request.configuration == 'Debug-Optimized'
	}

	@Ignore
	def "can parse task name when configuration name contains underscores"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective-c-ios-application_ObjectiveCIosApplication_Debug_Optimized")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'ObjectiveCIosApplication'
		request.configuration == 'Debug_Optimized'
	}

	@Ignore
	def "can parse task name when configuration name starts with underscores"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode__build_objective-c-ios-application_ObjectiveCIosApplication___NokeeTestRunner_Default")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'ObjectiveCIosApplication'
		request.configuration == '__NokeeTestRunner_Default'
	}

	def "can parse task name when action is implicit"() {
		when:
		def request = XcodeIdePlugin.XcodeIdeRequest.of("_xcode___objective-c-ios-application_ObjectiveCIosApplication_Default")

		then:
		request.action == 'build'
		request.projectName == 'objective-c-ios-application'
		request.targetName == 'ObjectiveCIosApplication'
		request.configuration == 'Default'
	}
}
