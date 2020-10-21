package dev.nokee.platform.c

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeComponentBuildTypeFunctionalTest
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.fixtures.CCompileGreeter
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib

class CApplicationBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
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
				id 'dev.nokee.c-application'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CGreeterApp().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CGreeterApp().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CCompileGreeter()
			}
		}
	}
}

class CLibraryBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
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
				id 'dev.nokee.c-library'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CGreeterLib().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CGreeterLib().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CCompileGreeter()
			}
		}
	}
}
