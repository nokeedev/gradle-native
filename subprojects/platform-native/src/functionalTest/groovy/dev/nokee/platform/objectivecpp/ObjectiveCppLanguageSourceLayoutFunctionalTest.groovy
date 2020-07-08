package dev.nokee.platform.objectivecpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterLib
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements ObjectiveCppTaskNames {
	def componentUnderTest = new ObjectiveCppGreeterApp()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-cpp-application'
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
				id 'dev.nokee.objective-cpp-application'
			}

			application {
				sources.from('srcs')
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
				id 'dev.nokee.objective-cpp-library'
			}

			library {
				sources.from('srcs')
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
class ObjectiveCppLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements ObjectiveCppTaskNames {
	def componentUnderTest = new ObjectiveCppGreeterLib()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-cpp-library'
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
			rootProject.name = 'application'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-cpp-library'
			}

			library {
				sources.from('srcs')
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
				id 'dev.nokee.objective-cpp-library'
			}

			library {
				sources.from('srcs')
				publicHeaders.from('includes')
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		def fixture = componentUnderTest.withImplementationAsSubproject('library')
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
class ObjectiveCppLibraryWithStaticLinkageNativeLanguageSourceLayoutFunctionalTest extends ObjectiveCppLibraryNativeLanguageSourceLayoutFunctionalTest {
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
class ObjectiveCppLibraryWithSharedLinkageNativeLanguageSourceLayoutFunctionalTest extends ObjectiveCppLibraryNativeLanguageSourceLayoutFunctionalTest {
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
class ObjectiveCppLibraryWithBothLinkageNativeLanguageSourceLayoutFunctionalTest extends ObjectiveCppLibraryNativeLanguageSourceLayoutFunctionalTest {
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
