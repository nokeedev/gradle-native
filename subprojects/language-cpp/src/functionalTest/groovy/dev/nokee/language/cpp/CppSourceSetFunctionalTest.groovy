package dev.nokee.language.cpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.cpp.internal.plugins.CppSourceSetRegistrationFactory
import dev.nokee.language.nativebase.internal.NativePlatformFactory
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

			import ${CppSourceSetRegistrationFactory.canonicalName}
			import ${LanguageSourceSetIdentifier.canonicalName}
			import ${ProjectIdentifier.canonicalName}
			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${CppSourceSet.canonicalName}
			import ${ModelRegistry.canonicalName}

			def registry = extensions.getByType(ModelRegistry)
			def identifier = LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "qise")
			def sourceSet = registry.register(extensions.getByType(CppSourceSetRegistrationFactory).create(identifier, false)).as(CppSourceSet).get()
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
