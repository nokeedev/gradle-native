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
package dev.nokee.language.objectivecpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.language.nativebase.internal.NativePlatformFactory
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppSourceSetSpec
import dev.nokee.platform.base.internal.DomainObjectEntities
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.registry.ModelRegistry
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp
import dev.nokee.runtime.nativebase.internal.TargetMachines
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppSourceSetFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private final ObjectiveCppGreeterApp fixture = new ObjectiveCppGreeterApp()

	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-language-base'
				id 'dev.nokee.native-runtime-base'
			}

			import ${DomainObjectEntities.canonicalName}
			import ${ProjectIdentifier.canonicalName}
			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${ObjectiveCppSourceSet.canonicalName}
			import ${ObjectiveCppSourceSetSpec.canonicalName}
			import ${ModelRegistry.canonicalName}

			def registry = extensions.getByType(ModelRegistry)
			def sourceSet = registry.register(DomainObjectEntities.newEntity("kupa", ObjectiveCppSourceSetSpec)).as(ObjectiveCppSourceSet).get()
			tasks.compileKupa.targetPlatform.set(NativePlatformFactory.create(TargetMachines.host()))
		"""
	}

	def "can compile Objective-C++ sources into objects"() {
		fixture.writeToProject(testDirectory)
		buildFile << """
			sourceSet.source.from('src/main/objcpp')
			sourceSet.headers.from('src/main/headers')
		"""

		expect:
		succeeds('compileKupa')
		objectFiles(fixture.sources, "build/objs/kupa")*.assertExists()
	}

	def "resolves compile dependencies from headerSearchPaths configuration"() {
		fixture.writeToProject(testDirectory)
		buildFile << '''
			sourceSet.source.from('src/main/objcpp')
			dependencies {
				kupaHeaderSearchPaths files('src/main/headers')
			}
		'''

		expect:
		succeeds('compileKupa')
		objectFiles(fixture.sources, "build/objs/kupa")*.assertExists()
	}

	def "can compile generated Objective-C++ sources"() {
		fixture.sources.writeToSourceDir(file('srcs'))
		fixture.headers.writeToProject(testDirectory)
		buildFile << '''
			def generatorTask = tasks.register('generateSources', Sync) {
				from('srcs')
				destinationDir = file('src/main/objcpp')
			}

			sourceSet.source.from(generatorTask.map { it.destinationDir })
			sourceSet.headers.from('src/main/headers')
		'''

		expect:
		def result = succeeds('compileKupa')
		result.assertTaskNotSkipped(':generateSources')
		objectFiles(fixture.sources, "build/objs/kupa")*.assertExists()
	}

	def "can compile with generated Objective-C++ headers"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/objcpp')
			sourceSet.headers.from(generatorTask.map { it.destinationDir })
		'''

		expect:
		def result = succeeds('compileKupa')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.sources, "build/objs/kupa")*.assertExists()
	}

	def "can consume generated headers from headerSearchPaths configuration"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/objcpp')
			dependencies {
				kupaHeaderSearchPaths files(generatorTask.map { it.destinationDir })
			}
		'''

		expect:
		def result = succeeds('compileKupa')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.sources, "build/objs/kupa")*.assertExists()
	}
}
