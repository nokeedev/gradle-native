/*
 * Copyright 2022 the original author or authors.
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
package dev.gradleplugins.dockit.javadoc;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.javadoc.Javadoc;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static dev.gradleplugins.dockit.javadoc.JavadocExcludeOption.exclude;
import static dev.gradleplugins.dockit.javadoc.JavadocSourcesOption.sources;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.stream;

public class JavadocTaskUtils {
	public static Callable<Object> ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption(Javadoc task) {
		return new Callable<Object>() {
			private File dummyFile;

			@Override
			public Object call() throws Exception {
				if (dummyFile == null) {
					if (sources(task).isEmpty()) {
						return Collections.emptyList();
					} else {
						final HasSourcesVisitor visitor = new HasSourcesVisitor();
						sources(task).getAsFileTree().visit(visitor);
						if (visitor.hasSources()) {
							dummyFile = task.getTemporaryDir().toPath().resolve("Dummy.java").toFile();
						} else {
							return Collections.emptyList();
						}
					}
				}

				Files.createDirectories(dummyFile.getParentFile().toPath());
				return Files.write(dummyFile.toPath(), Arrays.asList("package internal;", "class Dummy {}"), UTF_8, CREATE, TRUNCATE_EXISTING);
			}
		};
	}

	private static final class HasSourcesVisitor implements FileVisitor {
		private boolean hasSources = false;

		public boolean hasSources() {
			return hasSources;
		}

		@Override
		public void visitDir(FileVisitDetails dirDetails) {
			// ignores
		}

		@Override
		public void visitFile(FileVisitDetails details) {
			// Antlr, for example, generates files in a root source directory despite not being in the default package.
			//   Ideally, we should peek into the source files to identify their package.
			if (details.getRelativePath().getSegments().length > 1 && stream(details.getRelativePath().getSegments()).noneMatch("internal"::equals)) {
				hasSources = true;
				details.stopVisiting();
			}
		}
	}
}
