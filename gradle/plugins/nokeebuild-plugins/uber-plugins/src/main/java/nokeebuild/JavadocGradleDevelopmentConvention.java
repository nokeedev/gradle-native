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
package nokeebuild;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.*;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GUtil;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static nokeebuild.javadoc.JavadocExcludeOption.exclude;
import static nokeebuild.javadoc.JavadocLinksOption.links;
import static nokeebuild.javadoc.JavadocSourcePathsOption.sourcePaths;
import static nokeebuild.javadoc.JavadocSourcesOption.sources;
import static nokeebuild.javadoc.JavadocSubpackagesOption.subpackages;
import static nokeebuild.javadoc.JavadocTitleOption.title;

final class JavadocGradleDevelopmentConvention implements Action<Javadoc> {
	private final Project project;

	public JavadocGradleDevelopmentConvention(Project project) {
		this.project = project;
	}

	@Override
	public void execute(Javadoc task) {
		task.setSource(callableOf(ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption(task)));

		title(task).set(toWords(project.getName()).map(StringUtils::capitalize).collect(joining(" ")) + " " + project.getVersion());
		subpackages(task).set(project.provider(() -> {
			final List<String> result = new ArrayList<>();
			sources(task).getAsFileTree().visit(new GuessSubPackageVisitor(result::add));
			return result;
		}));
		exclude(task).set(project.provider(() -> {
			final List<String> result = new ArrayList<>();
			sources(task).getAsFileTree().visit(new ExcludesInternalPackages(result::add));
			return result;
		}));
		sources(task).from(callableOf(this::pluginSourceFiles));
		links(task).addAll(compatibility(gradlePlugin(project)).getMinimumGradleVersion().map(version -> {
			return Arrays.asList(
				project.uri("https://docs.oracle.com/javase/" + minimumJavaVersionFor(version).getMajorVersion() + "/docs/api"),
				project.uri("https://docs.gradle.org/" + version + "/javadoc/")
			);
		}));
		sourcePaths(task).from(callableOf(this::pluginSourceDirectories));
	}

	private static Callable<Object> ofDummyFileToAvoidNoSourceTaskOutcomeBecauseUsingSourcePathJavadocOption(Javadoc task) {
		return () -> {
			if (sources(task).isEmpty()) {
				return Collections.emptyList();
			} else {
				final MutableBoolean hasSources = new MutableBoolean(false);
				sources(task).getAsFileTree().visit(new FileVisitor() {
					@Override
					public void visitDir(FileVisitDetails dirDetails) {
						// ignores
					}

					@Override
					public void visitFile(FileVisitDetails details) {
						// Antlr, for example, generates files in a root source directory despite not being in the default package.
						//   Ideally, we should peek into the source files to identify their package.
						if (details.getRelativePath().getSegments().length > 1 && stream(details.getRelativePath().getSegments()).noneMatch("internal"::equals)) {
							hasSources.setTrue();
							details.stopVisiting();
						}
					}
				});
				if (hasSources.booleanValue()) {
					try {
						return Files.write(task.getTemporaryDir().toPath().resolve("Dummy.java"), Arrays.asList("package internal;", "class Dummy {}"), UTF_8, CREATE, TRUNCATE_EXISTING);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				} else {
					return Collections.emptyList();
				}
			}
		};
	}

	private static Stream<String> toWords(String s) {
		return stream(GUtil.toWords(s, '+').split("\\+"));
	}

	private FileTree pluginSourceFiles() {
		return gradlePlugin(project).getPluginSourceSet().getAllJava();
	}

	private FileCollection pluginSourceDirectories() {
		return gradlePlugin(project).getPluginSourceSet().getAllJava().getSourceDirectories();
	}

	private static final class ExcludesInternalPackages implements FileVisitor {
		private final Consumer<? super String> packageToExcludeListener;

		private ExcludesInternalPackages(Consumer<? super String> packageToExcludeListener) {
			this.packageToExcludeListener = packageToExcludeListener;
		}

		@Override
		public void visitDir(FileVisitDetails details) {
			if (details.getName().equals("internal")) {
				packageToExcludeListener.accept(toPackage(details.getRelativePath()));
			}
		}

		private String toPackage(RelativePath path) {
			return join(".", path.getSegments());
		}

		@Override
		public void visitFile(FileVisitDetails details) {
			// ignore
		}
	}

	private static final class GuessSubPackageVisitor implements FileVisitor {
		private final Consumer<? super String> subpackageListener;

		public GuessSubPackageVisitor(Consumer<? super String> subpackageListener) {
			this.subpackageListener = subpackageListener;
		}

		@Override
		public void visitDir(FileVisitDetails details) {
			subpackageListener.accept(details.getRelativePath().getSegments()[0]);
			details.stopVisiting();
		}

		@Override
		public void visitFile(FileVisitDetails details) {
			// ignore
		}
	}

	public static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
		return (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");
	}

	private static <T> Callable<T> callableOf(Callable<T> delegate) {
		return new Callable<T>() {
			private transient volatile boolean initialized;
			private transient T value;

			@Override
			public T call() throws Exception {
				// A 2-field variant of Double Checked Locking.
				if (!initialized) {
					synchronized (this) {
						if (!initialized) {
							T t = delegate.call();
							value = t;
							initialized = true;
							return t;
						}
					}
				}
				// This is safe because we checked `initialized.`
				return value;
			}
		};
	}
}
