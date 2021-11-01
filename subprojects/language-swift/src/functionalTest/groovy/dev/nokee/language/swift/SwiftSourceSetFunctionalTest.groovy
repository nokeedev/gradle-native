package dev.nokee.language.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.nativebase.internal.NativePlatformFactory
import dev.nokee.language.swift.internal.plugins.SwiftSourceSetRegistrationFactory
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.registry.ModelRegistry
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.runtime.nativebase.internal.TargetMachines

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftSourceSetFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	private final SwiftGreeterApp fixture = new SwiftGreeterApp()

	def setup() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-language-base'
				id 'dev.nokee.native-runtime-base'
			}

			import ${SwiftSourceSetRegistrationFactory.canonicalName}
			import ${LanguageSourceSetIdentifier.canonicalName}
			import ${ProjectIdentifier.canonicalName}
			import ${NativePlatformFactory.canonicalName}
			import ${TargetMachines.canonicalName}
			import ${SwiftSourceSet.canonicalName}
			import ${ModelRegistry.canonicalName}

			def registry = extensions.getByType(ModelRegistry)
			def identifier = LanguageSourceSetIdentifier.of(ProjectIdentifier.of(project), "pihe")
			def sourceSet = registry.register(extensions.getByType(SwiftSourceSetRegistrationFactory).create(identifier, false)).as(SwiftSourceSet).get()
			tasks.compilePihe.targetPlatform.set(NativePlatformFactory.create(TargetMachines.host()))
		"""
	}

	def "can compile Swift sources into objects"() {
		fixture.writeToProject(testDirectory)
		buildFile << """
			sourceSet.source.from('src/main/swift')
		"""

		expect:
		succeeds('compilePihe')
		objectFiles(fixture, "build/objs/pihe")*.assertExists()
	}

	def "can compile generated Swift sources"() {
		fixture.writeToSourceDir(file('srcs'))
		buildFile << '''
			def generatorTask = tasks.register('generateSources', Sync) {
				from('srcs')
				destinationDir = file('src/main/swift')
			}

			sourceSet.source.from(generatorTask.map { it.destinationDir })
		'''

		expect:
		def result = succeeds('compilePihe')
		result.assertTaskNotSkipped(':generateSources')
		objectFiles(fixture, "build/objs/pihe")*.assertExists()
	}

	// TODO: Write tests for importModules resolution (these tests are somewhat cover in platform-swift)
	//   We should still have some functional tests here as well.
}
