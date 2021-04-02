package dev.nokee.internal.testing;

import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.ConfigurableFileTree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class FileSystemWorkspace {
	private Project project = null;
	private final File workspaceDirectory;

	// TODO: Not sure if this should really be public, maybe move bridge test in the fixtures...
	public FileSystemWorkspace(File workspaceDirectory) throws IOException {
		this.workspaceDirectory = workspaceDirectory.getCanonicalFile();
	}

	public FileSystemWorkspace(Path workspaceDirectory) throws IOException {
		this(workspaceDirectory.toFile());
	}

	public File file(String path) {
		return new File(workspaceDirectory, path);
	}

	public File rootDirectory() {
		return workspaceDirectory;
	}

	public File newFile(String path) throws IOException {
		val file = file(path);
		assert !file.exists();
		file.getParentFile().mkdirs();
		assert file.createNewFile();
		return file;
	}

	public ConfigurableFileCollection fileCollection(Object... path) {
		if (project == null) {
			this.project = ProjectTestUtils.createRootProject(workspaceDirectory);
		}
		return project.files(path);
	}

	public ConfigurableFileTree fileTree(File directory) {
		if (project == null) {
			this.project = ProjectTestUtils.createRootProject(workspaceDirectory);
		}
		return project.fileTree(directory);
	}

	public File newDirectory(String path) {
		val directory = file(path);
		assert !directory.exists();
		assert directory.mkdirs();
		return directory;
	}

	public static File newFiles(File directory) throws IOException {
		return newFiles(directory, 2);
	}

	public static File newFiles(File directory, int count) throws IOException {
		val a = new FileSystemWorkspace(directory);
		for (int i = 1; i <= count; i++) {
			a.newFile("f" + i);
		}
		return directory;
	}
}
