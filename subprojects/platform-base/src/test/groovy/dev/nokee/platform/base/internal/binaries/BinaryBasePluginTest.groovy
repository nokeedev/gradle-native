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
package dev.nokee.platform.base.internal.binaries

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import dev.nokee.platform.base.internal.plugins.BinaryBasePlugin
import spock.lang.Specification

class BinaryBasePluginTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	def "registers binary configurer service"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(BinaryConfigurer) != null
	}

	def "registers binary repository service"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(BinaryRepository) != null
	}

	def "registers binary view factory"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(BinaryViewFactory) != null
	}

	def "registers known binary factory"() {
		when:
		project.apply plugin: BinaryBasePlugin

		then:
		project.extensions.findByType(KnownBinaryFactory) != null
	}
}
