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

import dev.nokee.runtime.nativebase.MachineArchitecture
import spock.lang.Specification
import spock.lang.Subject

@Subject(PreferHostMachineArchitectureComparator)
class PreferHostMachineArchitectureComparatorTest extends Specification {
	def subject = new PreferHostMachineArchitectureComparator()

	def "always prefer host machine architecture"() {
		expect:
		subject.compare(host, notHost) == -1
		subject.compare(notHost, host) == 1
	}

	def "no opinion on different machine architecture that is not host"() {
		expect:
		subject.compare(notHost, someOtherArchitecture) == 0
	}

	def "no opinion on same machine architecture"() {
		expect:
		subject.compare(host, host) == 0
		subject.compare(someOtherArchitecture, someOtherArchitecture) == 0
	}

	private MachineArchitecture getHost() {
		return MachineArchitecture.forName(System.getProperty('os.arch'))
	}

	private MachineArchitecture getNotHost() {
		assert MachineArchitecture.X86 != MachineArchitecture.forName(System.getProperty('os.arch')).canonicalName
		return MachineArchitecture.forName(MachineArchitecture.X86);
	}

	private MachineArchitecture getSomeOtherArchitecture() {
		return MachineArchitecture.forName('some-other-architecture')
	}
}
