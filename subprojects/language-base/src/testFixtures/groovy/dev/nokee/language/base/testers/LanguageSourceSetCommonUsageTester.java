package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static dev.nokee.language.base.testers.FileSystemWorkspace.newFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public interface LanguageSourceSetCommonUsageTester<T extends LanguageSourceSet> {
	T createSubject();

	@Test
	default void canAccessRelativeToBaseDirectoryPath(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		newFiles(a.newDirectory("dir1"));
		newFiles(a.newDirectory("dir2"));

		assertThat(relativePaths(createSubject().from(a.rootDirectory())), containsInAnyOrder("dir1/f1", "dir1/f2", "dir2/f1", "dir2/f2"));
	}

	@Test
	default void canAccessRelativeToRespectiveBaseDirectoryPath(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val dir1 = a.newDirectory("dir1");
		a.newFile("dir1/file1-1");
		a.newFile("dir1/nestedDir/file1-2");
		val dir2 = a.newDirectory("dir2");
		a.newFile("dir2/file2-1");
		a.newFile("dir2/nestedDir/file2-2");

		assertThat(relativePaths(createSubject().from(dir1, dir2)), containsInAnyOrder("file1-1", "nestedDir/file1-2", "file2-1", "nestedDir/file2-2"));
	}

	static Set<String> relativePaths(LanguageSourceSet sourceSet) {
		val relativePaths = new HashSet<String>();
		sourceSet.getAsFileTree().visit(new FileVisitor() {
			@Override
			public void visitDir(FileVisitDetails details) { /* ignored */ }

			@Override
			public void visitFile(FileVisitDetails details) {
				relativePaths.add(details.getRelativePath().toString());
			}
		});
		return relativePaths;
	}
}
