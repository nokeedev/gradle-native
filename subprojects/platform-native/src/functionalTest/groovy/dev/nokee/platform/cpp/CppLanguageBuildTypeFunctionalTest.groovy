package dev.nokee.platform.cpp

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeComponentBuildTypeFunctionalTest
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.elements.GreeterImplementationAwareSourceElement
import dev.nokee.platform.nativebase.fixtures.CCompileGreeter
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppCompileGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

class CppApplicationBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
			}
		'''
	}

	@Override
	protected void makeMultiProjectWithoutDependency() {
		settingsFile << '''
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CppGreeterApp().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CppGreeterApp().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CppCompileGreeter()
			}
		}
	}
}

class CppLibraryBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected void makeMultiProjectWithoutDependency() {
		settingsFile << '''
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CppGreeterLib().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CppGreeterLib().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CppCompileGreeter()
			}
		}
	}
}
