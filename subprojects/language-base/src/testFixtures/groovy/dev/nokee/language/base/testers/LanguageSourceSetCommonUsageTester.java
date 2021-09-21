/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.language.base.testers;

import dev.nokee.internal.testing.FileSystemWorkspace;
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

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
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
