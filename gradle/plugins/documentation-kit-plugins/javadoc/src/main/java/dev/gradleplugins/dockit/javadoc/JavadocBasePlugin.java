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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RelativePath;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.gradleplugins.dockit.javadoc.JavadocExcludeOption.exclude;
import static dev.gradleplugins.dockit.javadoc.JavadocSourcesOption.sources;
import static dev.gradleplugins.dockit.javadoc.JavadocSubpackagesOption.subpackages;
import static dev.gradleplugins.dockit.javadoc.JavadocTitleOption.title;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

abstract class JavadocBasePlugin implements Plugin<Project> {
	@Inject
	public JavadocBasePlugin() {}

	@Override
	public void apply(Project project) {
		final TaskCollection<Javadoc> javadocTasks = project.getTasks().withType(Javadoc.class);
		javadocTasks.configureEach(new JavadocLinksOption(project));
		javadocTasks.configureEach(new JavadocSourcePathsOption(project));
		javadocTasks.configureEach(new JavadocTitleOption(project));
		javadocTasks.configureEach(new JavadocExcludeOption(project));
		javadocTasks.configureEach(new JavadocSubpackagesOption(project));
		javadocTasks.configureEach(new JavadocAdditionalArgsOption(project));
		javadocTasks.configureEach(new JavadocSourcesOption(project));
		javadocTasks.configureEach(task -> {
			title(task).convention(toWords(project.getName()).map(JavadocBasePlugin::capitalize).collect(joining(" ")) + versionIfDefined(project).map(it -> " v" + it).orElse(""));
			subpackages(task).convention(project.provider(() -> {
				final List<String> result = new ArrayList<>();
				sources(task).getAsFileTree().visit(new GuessSubPackageVisitor(result::add));
				return result;
			}));
			exclude(task).convention(project.provider(() -> {
				final List<String> result = new ArrayList<>();
				sources(task).getAsFileTree().visit(new ExcludesInternalPackages(result::add));
				return result;
			}));
		});
	}

	private static String capitalize(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	private static Optional<String> versionIfDefined(Project project) {
		if (Objects.equals(project.getVersion(), Project.DEFAULT_VERSION)) {
			return Optional.empty();
		} else {
			return Optional.of(project.getVersion().toString());
		}
	}

	private static Stream<String> toWords(String s) {
		return stream(GUtil.toWords(s, '+').split("\\+"));
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
}
