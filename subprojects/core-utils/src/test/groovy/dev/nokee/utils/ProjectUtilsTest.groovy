/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.utils

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ProjectUtils)
class ProjectUtilsTest extends Specification {
	def "can detect root project"() {
		given:
		def root = ProjectBuilder.builder().withName('root').build()
		def child = ProjectBuilder.builder().withName('child').withParent(root).build()

		expect:
		ProjectUtils.isRootProject(root)
		!ProjectUtils.isRootProject(child)
	}

	def "returns a prefixable project path"() {
		given:
		def root = ProjectBuilder.builder().withName('root').build()
		def child = ProjectBuilder.builder().withName('child').withParent(root).build()

		expect:
		ProjectUtils.getPrefixableProjectPath(root) == ''
		ProjectUtils.getPrefixableProjectPath(child) == ':child'
	}
}
