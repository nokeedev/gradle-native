package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.nokee.platform.jni.fixtures.elements.ApplicationWithLibraryElement;
import dev.nokee.platform.jni.fixtures.elements.JavaMainUsesGreeter;

import java.io.File;
import java.util.List;

public final class GreeterAppWithJniLibrary implements ApplicationWithLibraryElement {
	private final JavaJniCppGreeterLib library;
	private final JavaMainUsesGreeter application = new JavaMainUsesGreeter();
	private final String resourcePath;
	private final String projectName;

    public GreeterAppWithJniLibrary(String projectName) {
		this(projectName, "");
	}
    public GreeterAppWithJniLibrary(String projectName, String resourcePath) {
		this.projectName = projectName;
		this.resourcePath = resourcePath;
		library = new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	@Override
	public String getExpectedOutput() {
		return application.getExpectedOutput();
	}

	public SourceElement withLibraryAsSubproject(String libraryProjectName) {
		if (resourcePath.isEmpty()) {
			return newLibraryAsSubproject(libraryProjectName, projectName + "/");
		}
		return newLibraryAsSubproject(libraryProjectName, resourcePath);
	}

	private SourceElement newLibraryAsSubproject(String libraryProjectName, String resourcePath) {
		return new SourceElement() {
			@Override
			public List<SourceFile> getFiles() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void writeToProject(File projectDir) {
				library.withResourcePath(resourcePath).withProjectName(libraryProjectName).writeToProject(new File(projectDir, libraryProjectName));
				application.writeToProject(projectDir);
			}

			SourceElement withResourcePath(String newResourcePath) {
				return newLibraryAsSubproject(libraryProjectName, newResourcePath);
			}
		};
	}

	public GreeterAppWithJniLibrary withResourcePath(String resourcePath) {
		return new GreeterAppWithJniLibrary(projectName, resourcePath);
	}

	@Override
	public SourceElement getLibrary() {
		return library;
	}

	@Override
	public SourceElement getApplication() {
		return application;
	}
}
