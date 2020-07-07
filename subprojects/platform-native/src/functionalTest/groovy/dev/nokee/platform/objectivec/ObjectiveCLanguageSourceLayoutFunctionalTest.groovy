package dev.nokee.platform.objectivec

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.jni.fixtures.ObjectiveCGreeter
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements ObjectiveCTaskNames {
	def componentUnderTest = new ObjectiveCGreeterApp()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		componentUnderTest.sources.writeToSourceDir(file('srcs'))
		componentUnderTest.headers.writeToSourceDir(file('includes'))
		componentUnderTest.headers.files.each {
			file("src/main/${it.path}/${it.name}") << "broken!"
		}
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			application {
				sources.from('srcs')
				privateHeaders.from('includes')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			application {
				${componentUnderTest.sources.files.collect { "sources.from('srcs/${it.name}')" }.join('\n')}
				privateHeaders.from('includes')
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements ObjectiveCTaskNames {
	def componentUnderTest = new ObjectiveCGreeter().asLib()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		componentUnderTest.sources.writeToSourceDir(file('srcs'))
		componentUnderTest.privateHeaders.writeToSourceDir(file('includes'))
		componentUnderTest.privateHeaders.files.each {
			file("src/main/headers/${it.name}") << "broken!"
		}
		componentUnderTest.publicHeaders.writeToSourceDir(file('includes'))
		componentUnderTest.publicHeaders.files.each {
			file("src/main/public/${it.name}") << "broken!"
		}
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			library {
				sources.from('srcs')
				privateHeaders.from('headers')
				publicHeaders.from('includes')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			library {
				${componentUnderTest.sources.files.collect { "sources.from('srcs/${it.name}')" }.join('\n')}
				privateHeaders.from('headers')
				publicHeaders.from('includes')
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryWithStaticLinkageNativeLanguageSourceLayoutFunctionalTest extends ObjectiveCLibraryNativeLanguageSourceLayoutFunctionalTest {
	@Override
	protected void makeSingleComponent() {
		super.makeSingleComponent()
		buildFile << '''
			library {
				targetLinkages = [linkages.static]
			}
		'''
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryWithSharedLinkageNativeLanguageSourceLayoutFunctionalTest extends ObjectiveCLibraryNativeLanguageSourceLayoutFunctionalTest {
	@Override
	protected void makeSingleComponent() {
		super.makeSingleComponent()
		buildFile << '''
			library {
				targetLinkages = [linkages.shared]
			}
		'''
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryWithBothLinkageNativeLanguageSourceLayoutFunctionalTest extends ObjectiveCLibraryNativeLanguageSourceLayoutFunctionalTest {
	@Override
	protected void makeSingleComponent() {
		super.makeSingleComponent()
		buildFile << '''
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		'''
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
	}
}
