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
package dev.nokee.platform.nativebase.internal.tasks

import dev.nokee.internal.testing.util.ProjectTestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(ExecutableLifecycleTask)
class ExecutableLifecycleTaskTest extends Specification {
	def tasks = ProjectTestUtils.rootProject().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', ExecutableLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', ExecutableLifecycleTask).description == 'Assembles a executable binary containing the main objects.'
	}
}
