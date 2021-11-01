package dev.nokee.language.c

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.c.internal.plugins.CSourceSetRegistrationFactory
import dev.nokee.language.nativebase.internal.NativePlatformFactory
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.registry.ModelRegistry
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.runtime.nativebase.internal.TargetMachines

class CSourceSetFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private final CGreeterApp fixture = new CGreeterApp()

	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-language-base'
				id 'dev.nokee.native-runtime-base'
			}

			import ${CSourceSetRegistrationFactory.canonicalName}
			import ${LanguageSourceSetIdentifier.canonicalName}
			import ${ProjectIdentifier.canonicalName}
			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${CSourceSet.canonicalName}
			import ${ModelRegistry.canonicalName}

			def registry = extensions.getByType(ModelRegistry)
			def identifier = LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "jixa")
			def sourceSet = registry.register(extensions.getByType(CSourceSetRegistrationFactory).create(identifier, false)).as(CSourceSet).get()
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
		objectFiles(fixture.sources, "build/objs/jixa")*.assertExists()
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
		objectFiles(fixture.sources, "build/objs/jixa")*.assertExists()
	}

	def "can compile generated C sources"() {
		fixture.sources.writeToSourceDir(file('srcs'))
		fixture.headers.writeToProject(testDirectory)
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
		objectFiles(fixture.sources, "build/objs/jixa")*.assertExists()
	}

	def "can compile with generated C headers"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
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
		objectFiles(fixture.sources, "build/objs/jixa")*.assertExists()
	}

	def "can consume generated headers from headerSearchPaths configuration"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
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
		objectFiles(fixture.sources, "build/objs/jixa")*.assertExists()
	}
}
