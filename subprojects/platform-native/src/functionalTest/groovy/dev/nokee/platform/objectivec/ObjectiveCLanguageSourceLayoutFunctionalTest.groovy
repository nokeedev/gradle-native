package dev.nokee.platform.objectivec

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterLib
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
	protected void makeComponentWithLibrary() {
		settingsFile << '''
			rootProject.name = 'application'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-application'
			}

			application {
				objectiveCSources.from('srcs')
				dependencies {
					implementation project(':library')
				}
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			library {
				objectiveCSources.from('srcs')
				publicHeaders.from('includes')
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		def fixture = componentUnderTest.withImplementationAsSubproject('library')
		fixture.elementUsingGreeter.sources.writeToSourceDir(file('srcs'))
		fixture.greeter.sources.writeToSourceDir(file('library', 'srcs'))
		fixture.greeter.publicHeaders.writeToSourceDir(file('library', 'includes'))
		fixture.greeter.files.each {
			file('library', "src/main/${it.path}/${it.name}") << "broken!"
		}
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			application {
				objectiveCSources.from('srcs')
				privateHeaders.from('includes')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			application {
				${componentUnderTest.sources.files.collect { "objectiveCSources.from('srcs/${it.name}')" }.join('\n')}
				privateHeaders.from('includes')
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements ObjectiveCTaskNames {
	def componentUnderTest = new ObjectiveCGreeterLib()

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
	protected void makeComponentWithLibrary() {
		settingsFile << '''
			rootProject.name = 'rootLibrary'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			library {
				objectiveCSources.from('srcs')
				privateHeaders.from('includes')
				dependencies {
					implementation project(':library')
				}
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			library {
				objectiveCSources.from('srcs')
				publicHeaders.from('includes')
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		def fixture = componentUnderTest.withImplementationAsSubproject()
		fixture.elementUsingGreeter.sources.writeToSourceDir(file('srcs'))
		fixture.elementUsingGreeter.headers.writeToSourceDir(file('includes'))
		fixture.greeter.sources.writeToSourceDir(file('library', 'srcs'))
		fixture.greeter.publicHeaders.writeToSourceDir(file('library', 'includes'))
		fixture.greeter.files.each {
			file('library', "src/main/${it.path}/${it.name}") << "broken!"
		}
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			library {
				objectiveCSources.from('srcs')
				privateHeaders.from('headers')
				publicHeaders.from('includes')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			library {
				${componentUnderTest.sources.files.collect { "objectiveCSources.from('srcs/${it.name}')" }.join('\n')}
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
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
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

	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
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
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
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
