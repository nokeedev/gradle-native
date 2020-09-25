package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterLib

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames {
	def componentUnderTest = new SwiftGreeterApp()

	@Override
	protected void makeSingleComponent() {
		settingsFile << "rootProject.name = 'app'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}
		'''
		componentUnderTest.writeToSourceDir(file('srcs'))
	}

	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << '''
			rootProject.name = 'application'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}

			application {
				swiftSources.from('srcs')
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.swift-library'
			}

			library {
				swiftSources.from('srcs')
			}
		'''
		def fixture = componentUnderTest.withImplementationAsSubproject('library')
		fixture.elementUsingGreeter.writeToSourceDir(file('srcs'))
		fixture.greeter.writeToSourceDir(file('library', 'srcs'))
		fixture.greeter.files.each {
			file('library', "src/main/${it.path}/${it.name}") << "broken!"
		}
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			application {
				swiftSources.from('srcs')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			application {
				${componentUnderTest.files.collect { "swiftSources.from('srcs/${it.name}')" }.join('\n')}
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames {
	def componentUnderTest = new SwiftGreeterLib()

	@Override
	protected void makeSingleComponent() {
		settingsFile << "rootProject.name = 'lib'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
		componentUnderTest.writeToSourceDir(file('srcs'))
	}

	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << '''
			rootProject.name = 'rootLibrary'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}

			library {
				swiftSources.from('srcs')
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.swift-library'
			}

			library {
				swiftSources.from('srcs')
			}
		'''
		def fixture = componentUnderTest.withImplementationAsSubproject('library')
		fixture.elementUsingGreeter.writeToSourceDir(file('srcs'))
		fixture.greeter.writeToSourceDir(file('library', 'srcs'))
		fixture.greeter.files.each {
			file('library', "src/main/${it.path}/${it.name}") << "broken!"
		}
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			library {
				swiftSources.from('srcs')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			library {
				${componentUnderTest.files.collect { "swiftSources.from('srcs/${it.name}')" }.join('\n')}
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithStaticLinkageNativeLanguageSourceLayoutFunctionalTest extends SwiftLibraryNativeLanguageSourceLayoutFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithSharedLinkageNativeLanguageSourceLayoutFunctionalTest extends SwiftLibraryNativeLanguageSourceLayoutFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithBothLinkageNativeLanguageSourceLayoutFunctionalTest extends SwiftLibraryNativeLanguageSourceLayoutFunctionalTest {
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
