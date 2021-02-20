package dev.nokee.language.base.testers;

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;

public abstract class AbstractLanguageSourceSetContentTester {
	abstract LanguageSourceSet createSubject();

	@TempDir
	protected File temporaryDirectory;
	protected FileSystemWorkspace a;

	@BeforeEach
	void createFileSystemWorkspace() throws IOException {
		a = new FileSystemWorkspace(temporaryDirectory);
	}

	abstract Object[] source(String... paths);

	protected Object sourceDirectory(String path) {
		return source(path)[0];
	}

	protected Object sourceFile(String path) {
		return source(path)[0];
	}

	protected Object[] sourceFiles(String... paths) {
		return Arrays.stream(paths).map(this::sourceFile).toArray();
	}

//	abstract boolean supportMissingFiles();

	@Test
	void canAddDirectory() throws IOException {
		newFiles(a.newDirectory("srcs"));
		val subject = createSubject().from(sourceDirectory("srcs"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
	}

	@Test
	void canAddDirectoryThatGetsCreatedLater() throws IOException {
		val subject = createSubject().from(sourceDirectory("srcs"));
		newFiles(a.newDirectory("srcs")); // create after was added
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
	}

	@Test
	void canAddMissingDirectory() throws IOException {
		val subject = createSubject().from(sourceDirectory("a-missing-directory"));
		assertThat(subject.getAsFileTree(), emptyIterable());
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("a-missing-directory")));
	}

	@Test
	void canAddRegularFile() throws IOException {
		a.newFile("src/a-file");
		val subject = createSubject().from(sourceFile("src/a-file"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/a-file")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src")));
	}

	@Test
	void canAddRegularFileThatGetsCreatedLater() throws IOException {
		val subject = createSubject().from(sourceFile("src/a-file"));
		a.newFile("src/a-file"); // create after was added
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/a-file")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src")));
	}

	@Test
	void canAddMissingRegularFile() throws IOException {
		val subject = createSubject().from(sourceFile("src/a-missing-file"));
		assertThat(subject.getAsFileTree(), emptyIterable());
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/a-missing-file")));
	}

	@Test
	void canCherryPickRegularFilesOutOfSingleDirectory() throws IOException {
		newFiles(a.newDirectory("srcs"), 5);
		val subject = createSubject().from(sourceFiles("srcs/f1", "srcs/f4"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f4")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
	}

	@Test
	void canCherryPickRegularFilesOutOfMultipleDirectories() throws IOException {
		newFiles(a.newDirectory("src/main"), 5);
		newFiles(a.newDirectory("src/common"), 5);
		val subject = createSubject().from(sourceFiles("src/main/f2", "src/common/f1", "src/main/f4", "src/common/f3"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/f2"), a.file("src/main/f4"), a.file("src/common/f1"), a.file("src/common/f3")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main"), a.file("src/common")));
	}
}
