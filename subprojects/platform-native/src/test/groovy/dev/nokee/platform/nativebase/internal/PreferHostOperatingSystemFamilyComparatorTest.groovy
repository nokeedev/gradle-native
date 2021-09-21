/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal

import dev.nokee.runtime.nativebase.OperatingSystemFamily
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferHostOperatingSystemFamilyComparator)
class PreferHostOperatingSystemFamilyComparatorTest extends Specification {
	def subject = new PreferHostOperatingSystemFamilyComparator()

	def "always prefer host operating system family"() {
		expect:
		subject.compare(host, notHost) == -1
		subject.compare(notHost, host) == 1
	}

	def "no opinion on different operating system that is not host"() {
		expect:
		subject.compare(notHost, someOtherFamily) == 0
	}

	def "no opinion on same operating system family"() {
		expect:
		subject.compare(host, host) == 0
		subject.compare(someOtherFamily, someOtherFamily) == 0
	}

	private OperatingSystemFamily getHost() {
		return OperatingSystemFamily.forName(System.getProperty("os.name"))
	}

	private OperatingSystemFamily getNotHost() {
		return OperatingSystemFamily.forName('not-host')
	}

	private OperatingSystemFamily getSomeOtherFamily() {
		return OperatingSystemFamily.forName('some-other-family')
	}
}
