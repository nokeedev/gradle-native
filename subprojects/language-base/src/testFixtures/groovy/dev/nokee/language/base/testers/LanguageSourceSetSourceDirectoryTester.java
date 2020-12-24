package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static dev.nokee.language.base.testers.FileSystemWorkspace.newFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public interface LanguageSourceSetSourceDirectoryTester<T extends LanguageSourceSet> {
	T createSubject();

	@Test
	default void canInferSourceDirectoryFromSingleFile(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.newFile("f1")).getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory()));
	}

	@Test
	default void canInferSourceDirectoryFromMultipleFilesInSameDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.newFile("f2"), a.newFile("f3")).getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory()));
	}

	@Test
	default void canInferSourceDirectoryFromFilesInNestedDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.newFile("f4"), a.newFile("nested/f5")).getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory(), a.file("nested")));
	}

	@Test
	default void canInferSourceDirectoryFromFilesInDistinctDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.newFile("d1/f6"), a.newFile("d2/f7")).getSourceDirectories(),
			containsInAnyOrder(a.file("d1"), a.file("d2")));
	}

	@Test
	default void canInferSourceDirectoryFromSingleSourceDirectory(@TempDir File temporaryDirectory) throws IOException {
		assertThat(createSubject().from(temporaryDirectory).getSourceDirectories(),
			containsInAnyOrder(temporaryDirectory));
	}

	@Test
	default void canInferSourceDirectoryFromNestedDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(newFiles(a.rootDirectory()), newFiles(a.newDirectory("nested"))).getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory(), a.file("nested")));
	}

	@Test
	default void canInferSourceDirectoryFromMultipleDistinctDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(
					newFiles(a.newDirectory("d3")),
					newFiles(a.newDirectory("d4"))
				).getSourceDirectories(),
			containsInAnyOrder(a.file("d3"), a.file("d4")));
	}

	@Test
	default void canInferSourceDirectoryFromNonExistingDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.file("src/main/headers")).getSourceDirectories(),
			containsInAnyOrder(a.file("src/main/headers")));
	}

	@Test
	default void canInferSourceDirectoryFromFileTreeWithNonExistingRootDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.fileTree(a.file("src/main/headers"))).getSourceDirectories(),
			containsInAnyOrder(a.file("src/main/headers")));
	}

	@Test
	default void canInferSourceDirectoryFromFileCollectionOfFiles(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(
				a.fileCollection(
					a.newFile("f8"),
					a.newFile("f9")
				)).getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory()));
	}

	@Test
	default void canInferSourceDirectoryFromFileTreeOfDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.fileTree(newFiles(a.newDirectory("d5")))).getSourceDirectories(),
			containsInAnyOrder(a.file("d5")));
	}

	@Test
	default void filtersDoesNotAffectPerFileSourceDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject()
				.from(a.newFile("f2"), a.newFile("nested/f3"))
				.filter(p -> p.exclude("f3"))
				.getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory(), a.file("nested")));
	}

	@Test
	default void filtersDoesNotAffectSourceDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject()
				.from(newFiles(a.rootDirectory()), newFiles(a.newDirectory("nested")))
				.filter(p -> p.exclude("f1"))
				.getSourceDirectories(),
			containsInAnyOrder(a.rootDirectory(), a.file("nested")));
	}
}
