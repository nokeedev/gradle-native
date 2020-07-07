package dev.nokee.platform.cpp

import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp

class CppApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CppTaskNames {
	def componentUnderTest = new CppGreeterApp()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
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

class CppLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CppTaskNames {
	def componentUnderTest = new CppGreeter().asLib()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
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

class CppLibraryWithStaticLinkageNativeLanguageSourceLayoutFunctionalTest extends CppLibraryNativeLanguageSourceLayoutFunctionalTest {
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

class CppLibraryWithSharedLinkageNativeLanguageSourceLayoutFunctionalTest extends CppLibraryNativeLanguageSourceLayoutFunctionalTest {
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

class CppLibraryWithBothLinkageNativeLanguageSourceLayoutFunctionalTest extends CppLibraryNativeLanguageSourceLayoutFunctionalTest {
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
