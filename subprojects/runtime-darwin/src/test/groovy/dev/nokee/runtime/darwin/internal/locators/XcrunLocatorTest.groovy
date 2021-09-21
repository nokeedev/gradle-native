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
package dev.nokee.runtime.darwin.internal.locators

import spock.lang.Specification
import spock.lang.Subject

@Subject(XcrunLocator)
class XcrunLocatorTest extends Specification {
	def "can parse output from Xcode 11.3.1"() {
		when:
		def version = XcrunLocator.asXcodeRunVersion().parse('xcrun version 48.\n')

		then:
		version.major == 48
		version.minor == 0
		version.micro == 0
		version.patch == 0
		version.qualifier == null
	}

	def "can parse output from Xcode 10.3"() {
		when:
		def version = XcrunLocator.asXcodeRunVersion().parse('xcrun version 43.1.\n')

		then:
		version.major == 43
		version.minor == 1
		version.micro == 0
		version.patch == 0
		version.qualifier == null
	}
}
