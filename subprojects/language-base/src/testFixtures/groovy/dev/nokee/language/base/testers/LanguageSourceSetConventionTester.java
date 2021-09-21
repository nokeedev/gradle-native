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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;

public interface LanguageSourceSetConventionTester<T extends LanguageSourceSet> {
	T createSubject();

	//region convention(file tree with non-existent base directory)
	@Test
	default void canSetConventionToNonExistentDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().convention(a.fileTree(a.file("src/main/headers")));
		assertThat(subject.getAsFileTree(), emptyIterable());
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/headers")));
	}

	@Test
	default void canSetConventionToFileCollectionOfNonExistentBaseDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().convention(a.fileCollection(a.file("src/main/headers"), a.file("src/main/public")));
		assertThat(subject.getAsFileTree(), emptyIterable());
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/headers"), a.file("src/main/public")));
	}
	//endregion

	//region convention(file tree)
	@Test
	default void canConfigureSourceSetConventionUsingFileTree(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().convention(a.fileTree(newFiles(a.newDirectory("b1"))));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b1/f1"), a.file("b1/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b1")));
	}

	@Test
	default void canConfigureSourceSetConventionWithIncludeFilter(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().convention(a.fileTree(newFiles(a.newDirectory("b2"))).matching(p -> p.include("f1")));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b2/f1")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b2")));
	}

	@Test
	default void canConfigureSourceSetConventionWithExcludeFilter(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().convention(a.fileTree(newFiles(a.newDirectory("b3"))).matching(p -> p.exclude("f1")));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b3/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b3")));
	}

	@Test
	default void canConfigureGlobalSourceSetFilterWithConvention(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().convention(a.fileTree(newFiles(a.newDirectory("b4")))).filter(p -> p.exclude("f2"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b4/f1")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b4")));
	}

	@Test
	default void overwriteConventionWhenFroming(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject()
			.convention(a.fileTree(newFiles(a.newDirectory("b5"))))
			.from(a.fileTree(newFiles(a.newDirectory("b6"))));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b6/f1"), a.file("b6/f2")));
		assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b6")));
	}
	//endregion
}
