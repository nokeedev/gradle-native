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
package dev.nokee.language.c

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.c.internal.CSourceSetSpec
import dev.nokee.language.nativebase.internal.NativePlatformFactory
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.runtime.nativebase.internal.TargetMachines

import static dev.gradleplugins.fixtures.sources.NativeElements.headers
import static dev.gradleplugins.fixtures.sources.NativeElements.sources

class CSourceSetFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private final CGreeterApp fixture = new CGreeterApp()

	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-language-base'
				id 'dev.nokee.native-runtime-base'
			}

			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${CSourceSetSpec.canonicalName}

			def sourceSet = sources.register('jixa', CSourceSetSpec).get()
			tasks.compileJixa.targetPlatform.set(NativePlatformFactory.create(TargetMachines.host()))
		"""
	}

	def "can compile C sources into objects"() {
		fixture.writeToProject(testDirectory)
		buildFile << """
			sourceSet.source.from('src/main/c')
			sourceSet.headers.from('src/main/headers')
		"""

		expect:
		succeeds('compileJixa')
		objectFiles(fixture.get(sources()), "build/objs/jixa")*.assertExists()
	}

	def "resolves compile dependencies from headerSearchPaths configuration"() {
		fixture.writeToProject(testDirectory)
		buildFile << '''
			sourceSet.source.from('src/main/c')
			dependencies {
				jixaHeaderSearchPaths files('src/main/headers')
			}
		'''

		expect:
		succeeds('compileJixa')
		objectFiles(fixture.get(sources()), "build/objs/jixa")*.assertExists()
	}

	def "can compile generated C sources"() {
		fixture.get(sources()).writeToSourceDir(file('srcs'))
		fixture.get(headers()).writeToProject(testDirectory)
		buildFile << '''
			def generatorTask = tasks.register('generateSources', Sync) {
				from('srcs')
				destinationDir = file('src/main/c')
			}

			sourceSet.source.from(generatorTask.map { it.destinationDir })
			sourceSet.headers.from('src/main/headers')
		'''

		expect:
		def result = succeeds('compileJixa')
		result.assertTaskNotSkipped(':generateSources')
		objectFiles(fixture.get(sources()), "build/objs/jixa")*.assertExists()
	}

	def "can compile with generated C headers"() {
		fixture.get(sources()).writeToProject(testDirectory)
		fixture.get(headers()).writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/c')
			sourceSet.headers.from(generatorTask.map { it.destinationDir })
		'''

		expect:
		def result = succeeds('compileJixa')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.get(sources()), "build/objs/jixa")*.assertExists()
	}

	def "can consume generated headers from headerSearchPaths configuration"() {
		fixture.get(sources()).writeToProject(testDirectory)
		fixture.get(headers()).writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/c')
			dependencies {
				jixaHeaderSearchPaths files(generatorTask.map { it.destinationDir })
			}
		'''

		expect:
		def result = succeeds('compileJixa')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.get(sources()), "build/objs/jixa")*.assertExists()
	}
}
