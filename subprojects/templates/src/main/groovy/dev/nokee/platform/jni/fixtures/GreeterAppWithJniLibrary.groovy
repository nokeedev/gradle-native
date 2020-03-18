package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile
import dev.nokee.platform.jni.fixtures.elements.ApplicationWithLibraryElement
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter

class GreeterAppWithJniLibrary implements ApplicationWithLibraryElement {
	final JavaJniCppGreeterLib library
	final JavaMainUsesGreeter application = new JavaMainUsesGreeter()
	private final String resourcePath
	private final String projectName

	GreeterAppWithJniLibrary(String projectName, String resourcePath = '') {
		this.projectName = projectName
		this.resourcePath = resourcePath
		library = new JavaJniCppGreeterLib(projectName, resourcePath)
	}

	@Override
	String getExpectedOutput() {
		return application.expectedOutput
	}

	SourceElement withLibraryAsSubproject(String libraryProjectName) {
		return new SourceElement() {
			@Override
			List<SourceFile> getFiles() {
				throw new UnsupportedOperationException()
			}

			@Override
			void writeToProject(TestFile projectDir) {
				library.withResourcePath("${projectName}/").withProjectName(libraryProjectName).writeToProject(projectDir.file(libraryProjectName))
				application.writeToProject(projectDir)
			}
		}
	}

	GreeterAppWithJniLibrary withResourcePath(String resourcePath) {
		return new GreeterAppWithJniLibrary(projectName, resourcePath)
	}
}
