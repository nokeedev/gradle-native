package dev.nokee.platform.c

import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.platform.nativebase.fixtures.CGreeterApp

class CApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CTaskNames {
	def componentUnderTest = new CGreeterApp()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
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

class CLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CTaskNames {
	def componentUnderTest = new CGreeter().asLib()

	@Override
	protected void makeSingleComponent() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
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

class CLibraryWithStaticLinkageNativeLanguageSourceLayoutFunctionalTest extends CLibraryNativeLanguageSourceLayoutFunctionalTest {
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

class CLibraryWithSharedLinkageNativeLanguageSourceLayoutFunctionalTest extends CLibraryNativeLanguageSourceLayoutFunctionalTest {
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

class CLibraryWithBothLinkageNativeLanguageSourceLayoutFunctionalTest extends CLibraryNativeLanguageSourceLayoutFunctionalTest {
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
