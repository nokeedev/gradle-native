package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.nokee.platform.jni.fixtures.elements.ApplicationWithLibraryElement
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file

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
		if (resourcePath.isEmpty()) {
			return newLibraryAsSubproject(libraryProjectName, "${projectName}/")
		}
		return newLibraryAsSubproject(libraryProjectName, resourcePath);
	}

	private SourceElement newLibraryAsSubproject(String libraryProjectName, String resourcePath) {
		return new SourceElement() {
			@Override
			List<SourceFile> getFiles() {
				throw new UnsupportedOperationException()
			}

			@Override
			void writeToProject(File projectDir) {
				library.withResourcePath(resourcePath).withProjectName(libraryProjectName).writeToProject(file(projectDir, libraryProjectName))
				application.writeToProject(projectDir)
			}

			SourceElement withResourcePath(String newResourcePath) {
				return newLibraryAsSubproject(libraryProjectName, newResourcePath);
			}
		}
	}

	GreeterAppWithJniLibrary withResourcePath(String resourcePath) {
		return new GreeterAppWithJniLibrary(projectName, resourcePath)
	}
}
