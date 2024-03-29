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
package dev.nokee.ide.visualstudio.internal.plugins

import dev.nokee.internal.testing.util.ProjectTestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(VisualStudioIdePlugin)
class VisualStudioIdePluginTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	def "applies Visual Studio IDE base plugin"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide'

		then:
		project.pluginManager.hasPlugin('dev.nokee.visual-studio-ide-base')
	}
}
