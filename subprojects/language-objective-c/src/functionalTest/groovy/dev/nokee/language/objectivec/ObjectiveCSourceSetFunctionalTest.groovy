package dev.nokee.language.objectivec

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.nativebase.internal.NativePlatformFactory
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCSourceSetRegistrationFactory
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.registry.ModelRegistry
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp
import dev.nokee.runtime.nativebase.internal.TargetMachines
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCSourceSetFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private final ObjectiveCGreeterApp fixture = new ObjectiveCGreeterApp()

	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.objective-c-language-base'
				id 'dev.nokee.native-runtime-base'
			}

			import ${ObjectiveCSourceSetRegistrationFactory.canonicalName}
			import ${LanguageSourceSetIdentifier.canonicalName}
			import ${ProjectIdentifier.canonicalName}
			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${ObjectiveCSourceSet.canonicalName}
			import ${ModelRegistry.canonicalName}

			def registry = extensions.getByType(ModelRegistry)
			def identifier = LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "jure")
			def sourceSet = registry.register(extensions.getByType(ObjectiveCSourceSetRegistrationFactory).create(identifier, false)).as(ObjectiveCSourceSet).get()
			tasks.compileJure.targetPlatform.set(NativePlatformFactory.create(TargetMachines.host()))
		"""
	}

	def "can compile Objective-C sources into objects"() {
		fixture.writeToProject(testDirectory)
		buildFile << """
			sourceSet.source.from('src/main/objc')
			sourceSet.headers.from('src/main/headers')
		"""

		expect:
		succeeds('compileJure')
		objectFiles(fixture.sources, "build/objs/jure")*.assertExists()
	}

	def "resolves compile dependencies from headerSearchPaths configuration"() {
		fixture.writeToProject(testDirectory)
		buildFile << '''
			sourceSet.source.from('src/main/objc')
			dependencies {
				jureHeaderSearchPaths files('src/main/headers')
			}
		'''

		expect:
		succeeds('compileJure')
		objectFiles(fixture.sources, "build/objs/jure")*.assertExists()
	}

	def "can compile generated Objective-C sources"() {
		fixture.sources.writeToSourceDir(file('srcs'))
		fixture.headers.writeToProject(testDirectory)
		buildFile << '''
			def generatorTask = tasks.register('generateSources', Sync) {
				from('srcs')
				destinationDir = file('src/main/objc')
			}

			sourceSet.source.from(generatorTask.map { it.destinationDir })
			sourceSet.headers.from('src/main/headers')
		'''

		expect:
		def result = succeeds('compileJure')
		result.assertTaskNotSkipped(':generateSources')
		objectFiles(fixture.sources, "build/objs/jure")*.assertExists()
	}

	def "can compile with generated Objective-C headers"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/objc')
			sourceSet.headers.from(generatorTask.map { it.destinationDir })
		'''

		expect:
		def result = succeeds('compileJure')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.sources, "build/objs/jure")*.assertExists()
	}

	def "can consume generated headers from headerSearchPaths configuration"() {
		fixture.sources.writeToProject(testDirectory)
		fixture.headers.writeToSourceDir(file('hdrs'))
		buildFile << '''
			def generatorTask = tasks.register('generateHeaders', Sync) {
				from('hdrs')
				destinationDir = file('build/generated-src')
			}

			sourceSet.source.from('src/main/objc')
			dependencies {
				jureHeaderSearchPaths files(generatorTask.map { it.destinationDir })
			}
		'''

		expect:
		def result = succeeds('compileJure')
		result.assertTaskNotSkipped(':generateHeaders')
		objectFiles(fixture.sources, "build/objs/jure")*.assertExists()
	}
}
