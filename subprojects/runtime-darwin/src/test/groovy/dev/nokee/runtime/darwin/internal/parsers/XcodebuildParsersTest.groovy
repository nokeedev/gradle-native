package dev.nokee.runtime.darwin.internal.parsers

import spock.lang.Specification
import spock.lang.Subject

@Subject(XcodebuildParsers)
class XcodebuildParsersTest extends Specification {

	def "can parse output from Xcode 11.3.1"() {
		given:
		String output = XcodebuildParsersTest.'showSdks11.3.1'

		when:
		def result = XcodebuildParsers.showSdkParser().parse(output)

		then:
		result*.identifier == ['iphoneos13.2', 'iphonesimulator13.2', 'driverkit.macosx19.0', 'macosx10.15', 'appletvos13.2', 'appletvsimulator13.2', 'watchos6.1', 'watchsimulator6.1']
	}


	private static String 'getShowSdks11.3.1'() {
		return '''iOS SDKs:
\tiOS 13.2                      \t-sdk iphoneos13.2

iOS Simulator SDKs:
\tSimulator - iOS 13.2          \t-sdk iphonesimulator13.2

macOS SDKs:
\tDriverKit 19.0                \t-sdk driverkit.macosx19.0
\tmacOS 10.15                   \t-sdk macosx10.15

tvOS SDKs:
\ttvOS 13.2                     \t-sdk appletvos13.2

tvOS Simulator SDKs:
\tSimulator - tvOS 13.2         \t-sdk appletvsimulator13.2

watchOS SDKs:
\twatchOS 6.1                   \t-sdk watchos6.1

watchOS Simulator SDKs:
\tSimulator - watchOS 6.1       \t-sdk watchsimulator6.1
'''
	}

	def "can parse output from Xcode 10.3"() {
		given:
		String output = XcodebuildParsersTest.'showSdks10.3'

		when:
		def result = XcodebuildParsers.showSdkParser().parse(output)

		then:
		result*.identifier == ['iphoneos12.4', 'iphonesimulator12.4', 'macosx10.14', 'appletvos12.4', 'appletvsimulator12.4', 'watchos5.3', 'watchsimulator5.3']
	}

	private static String 'getShowSdks10.3'() {
		return '''iOS SDKs:
\tiOS 12.4                      \t-sdk iphoneos12.4

iOS Simulator SDKs:
\tSimulator - iOS 12.4          \t-sdk iphonesimulator12.4

macOS SDKs:
\tmacOS 10.14                   \t-sdk macosx10.14

tvOS SDKs:
\ttvOS 12.4                     \t-sdk appletvos12.4

tvOS Simulator SDKs:
\tSimulator - tvOS 12.4         \t-sdk appletvsimulator12.4

watchOS SDKs:
\twatchOS 5.3                   \t-sdk watchos5.3

watchOS Simulator SDKs:
\tSimulator - watchOS 5.3       \t-sdk watchsimulator5.3
'''
	}
}
