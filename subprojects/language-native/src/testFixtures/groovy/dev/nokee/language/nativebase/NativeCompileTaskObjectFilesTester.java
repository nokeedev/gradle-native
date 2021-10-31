/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.nativebase;

import dev.nokee.language.base.HasDestinationDirectory;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static dev.nokee.internal.testing.FileSystemMatchers.*;
import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface NativeCompileTaskObjectFilesTester<T extends Task & HasObjectFiles & HasDestinationDirectory> {
	T subject();

	@BeforeEach
	default void expectDestinationDirectory() {
		assertThat("expect a destination directory configured", subject().getDestinationDirectory(), presentProvider());
	}

	@Test
	default void includesDotOFilesFromDestinationDirectory() throws IOException {
		createFile(subject().getDestinationDirectory().file("foo.o").get());
		assertThat(subject().getObjectFiles(), contains(aFileNamed("foo.o")));
	}

	@Test
	default void includesDotObjFilesFromDestinationDirectory() throws IOException {
		createFile(subject().getDestinationDirectory().file("foo.obj").get());
		assertThat(subject().getObjectFiles(), contains(aFileNamed("foo.obj")));
	}

	@Test
	default void doesNotIncludeNonDotOAndDotObjFilesFromDestinationDirectory() throws IOException {
		createFile(subject().getDestinationDirectory().file("foo.txt").get());
		assertThat(subject().getObjectFiles(), emptyIterable());
	}

	@Test
	default void canChangeDestinationDirectoryWithoutChangingObjectFilesOutputs() throws IOException {
		val destinationDirectory = subject().getProject().getLayout().getBuildDirectory().dir("out").get();
		createFile(destinationDirectory.file("foo.o"));
		createFile(destinationDirectory.file("foo.obj"));
		createFile(destinationDirectory.file("foo.txt"));
		subject().getDestinationDirectory().set(destinationDirectory);
		assertThat(subject().getObjectFiles(), containsInAnyOrder(
			aFile(withAbsolutePath(endsWith("/build/out/foo.o"))),
			aFile(withAbsolutePath(endsWith("/build/out/foo.obj")))
		));
	}

	static File createFile(RegularFile provider) throws IOException {
		val path = provider.getAsFile();
		path.getParentFile().mkdirs();
		path.createNewFile();
		return path;
	}
}
