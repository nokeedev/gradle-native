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
package dev.nokee.language.cpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.cpp.internal.plugins.CppSourceSetSpec
import dev.nokee.language.nativebase.internal.NativePlatformFactory
import dev.nokee.model.internal.DomainObjectEntities
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.registry.ModelRegistry
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.runtime.nativebase.internal.TargetMachines

class CppSourceSetFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private final CppGreeterApp fixture = new CppGreeterApp()

	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-language-base'
				id 'dev.nokee.native-runtime-base'
			}

			import ${DomainObjectEntities.canonicalName}
			import ${ProjectIdentifier.canonicalName}
			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${CppSourceSet.canonicalName}
			import ${CppSourceSetSpec.canonicalName}
			import ${ModelRegistry.canonicalName}

			def registry = extensions.getByType(ModelRegistry)
			def sourceSet = registry.register(DomainObjectEntities.newEntity("qise", CppSourceSetSpec).build()).as(CppSourceSet).get()
			tasks.compileQise.targetPlatform.set(NativePlatformFactory.create(TargetMachines.host()))
		"""
	}

	def "can compile C++ sources into objects"() {
		fixture.writeToProject(testDirectory)
		buildFile << """
			sourceSet.source.from('src/main/cpp')
			sourceSet.headers.from('src/main/headers')
		"""

		expect:
		succeeds('compileQise')
		objectFiles(fixture.sources, "build/objs/qise")*.assertExists()
	}

	def "resolves compile dependencies from headerSearchPaths configuration"() {
		fixture.writeToProject(testDirectory)
		buildFile << '''
			sourceSet.source.from('src/main/cpp')
			dependencies {
				qiseHeaderSearchPaths files('src/main/headers')
			}
		'''

		expect:
		succeeds('compileQise')
		objectFiles(fixture.sources, "build/objs/qise")*.assertExists()
	}

	def "can compile generated C++ sources"() {
		fixture.sources.writeToSourceDir(file('srcs'))
		fixture.headers.writeToProject(testDirectory)
		buildFile << '''
			def generatorTask = tasks.register('generateSources', Sync) {
				from('srcs')
				destinationDir = file('src/main/cpp')
			}

			sourceSet.source.from(generatorTask.map { it.destinationDir })
			sourceSet.headers.from('src/main/headers')
		'''

		expect:
		def result = succeeds('compileQise')
		result.assertTaskNotSkipped(':generateSources')
		objectFiles(fixture.sources, "build/objs/qise")*.assertExists()
	}

	def "can compile with generated C++ headers"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/cpp')
			sourceSet.headers.from(generatorTask.map { it.destinationDir })
		'''

		expect:
		def result = succeeds('compileQise')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.sources, "build/objs/qise")*.assertExists()
	}

	def "can consume generated headers from headerSearchPaths configuration"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/cpp')
			dependencies {
				qiseHeaderSearchPaths files(generatorTask.map { it.destinationDir })
			}
		'''

		expect:
		def result = succeeds('compileQise')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.sources, "build/objs/qise")*.assertExists()
	}
}
