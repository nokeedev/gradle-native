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
package dev.nokee.language.base.testers;

import com.google.common.util.concurrent.Callables;
import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.SourceSet;
import dev.nokee.utils.ClosureTestUtils;
import lombok.val;
import org.gradle.api.Buildable;
import org.gradle.api.Task;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.util.PatternFilterable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public abstract class ConfigurableSourceSetIntegrationTester implements ConfigurableSourceSetTester {
	protected FileSystemWorkspace a;

	@BeforeEach
	void createFileSystemWorkspace() throws IOException {
		a = new FileSystemWorkspace(getTemporaryDirectory());
	}

	public abstract ConfigurableSourceSet subject();

	public abstract File getTemporaryDirectory() throws IOException;

	// TODO: most likely will have to remove
	@Nested
	class EmptyTest {
		@Test
		void hasEmptyFileTreeWhenNoSource() {
			assertThat("file tree should be empty", subject().getAsFileTree(), emptyIterable());
		}

		@Test
		void hasEmptySourceDirectoriesWhenNoSource() {
			assertThat("source directories should be empty", subject().getSourceDirectories(), emptyIterable());
		}

		@Test
		void hasNoFilterPatternByDefault() {
			assertThat(subject().getFilter().getExcludes(), empty());
			assertThat(subject().getFilter().getIncludes(), empty());
		}
	}

	@Nested
	class FilterTest {
		@Test
		void honorsIncludeFilterChangesExistingFileTree() throws IOException {
			val subject = subject().from(a.newFile("f1"), a.newFile("f2"));
			val fileTree = subject.getAsFileTree();
			assertThat(fileTree, containsInAnyOrder(a.file("f1"), a.file("f2")));
			subject.getFilter().include("f1");
			assertThat(fileTree, containsInAnyOrder(a.file("f1")));
		}

		@Test
		void newFileTreeHonorsCurrentFilter() throws IOException {
			val subject = subject().from(a.newFile("f1"), a.newFile("f2")).filter(p -> p.exclude("f1"));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("f2")));
		}

		@Test
		void honorsFiltersFromFileTree() throws IOException {
			assertThat(subject().from(a.fileTree(newFiles(a.rootDirectory())).matching(p -> p.include("f1"))).getAsFileTree(),
				containsInAnyOrder(a.file("f1")));
		}

		@Test
		void canConfigureFiltersFluently() {
			assertThat(subject()
					.filter(p -> p.include("*.c"))
					.filter(ClosureTestUtils.<PatternFilterable>adapt(p -> p.include("*.conly")))
					.getFilter()
					.getIncludes(),
				containsInAnyOrder("*.c", "*.conly"));
		}
	}

	@Nested
	class SourceDirectoryTest {
		@Test
		void canInferSourceDirectoryFromSingleFile() throws IOException {
			assertThat(subject().from(a.newFile("f1")).getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory()));
		}

		@Test
		void canInferSourceDirectoryFromMultipleFilesInSameDirectory() throws IOException {
			assertThat(subject().from(a.newFile("f2"), a.newFile("f3")).getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory()));
		}

		@Test
		void canInferSourceDirectoryFromFilesInNestedDirectory() throws IOException {
			assertThat(subject().from(a.newFile("f4"), a.newFile("nested/f5")).getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory(), a.file("nested")));
		}

		@Test
		void canInferSourceDirectoryFromFilesInDistinctDirectories() throws IOException {
			assertThat(subject().from(a.newFile("d1/f6"), a.newFile("d2/f7")).getSourceDirectories(),
				containsInAnyOrder(a.file("d1"), a.file("d2")));
		}

		@Test
		void canInferSourceDirectoryFromSingleSourceDirectory() throws IOException {
			assertThat(subject().from(getTemporaryDirectory()).getSourceDirectories(),
				containsInAnyOrder(getTemporaryDirectory()));
		}

		@Test
		void canInferSourceDirectoryFromNestedDirectories() throws IOException {
			assertThat(subject().from(newFiles(a.rootDirectory()), newFiles(a.newDirectory("nested"))).getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory(), a.file("nested")));
		}

		@Test
		void canInferSourceDirectoryFromMultipleDistinctDirectories() throws IOException {
			assertThat(subject().from(
				newFiles(a.newDirectory("d3")),
				newFiles(a.newDirectory("d4"))
				).getSourceDirectories(),
				containsInAnyOrder(a.file("d3"), a.file("d4")));
		}

		@Test
		void canInferSourceDirectoryFromNonExistingDirectory() throws IOException {
			assertThat(subject().from(a.file("src/main/headers")).getSourceDirectories(),
				containsInAnyOrder(a.file("src/main/headers")));
		}

		@Test
		void canInferSourceDirectoryFromFileTreeWithNonExistingRootDirectory() throws IOException {
			assertThat(subject().from(a.fileTree(a.file("src/main/headers"))).getSourceDirectories(),
				containsInAnyOrder(a.file("src/main/headers")));
		}

		@Test
		void canInferSourceDirectoryFromFileCollectionOfFiles() throws IOException {
			assertThat(subject().from(
				a.fileCollection(
					a.newFile("f8"),
					a.newFile("f9")
				)).getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory()));
		}

		@Test
		void canInferSourceDirectoryFromFileTreeOfDirectory() throws IOException {
			assertThat(subject().from(a.fileTree(newFiles(a.newDirectory("d5")))).getSourceDirectories(),
				containsInAnyOrder(a.file("d5")));
		}

		@Test
		void filtersDoesNotAffectPerFileSourceDirectories() throws IOException {
			assertThat(subject()
					.from(a.newFile("f2"), a.newFile("nested/f3"))
					.filter(p -> p.exclude("f3"))
					.getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory(), a.file("nested")));
		}

		@Test
		void filtersDoesNotAffectSourceDirectories() throws IOException {
			assertThat(subject()
					.from(newFiles(a.rootDirectory()), newFiles(a.newDirectory("nested")))
					.filter(p -> p.exclude("f1"))
					.getSourceDirectories(),
				containsInAnyOrder(a.rootDirectory(), a.file("nested")));
		}
	}

	@Nested
	class ConventionTest {
		//region convention(file tree with non-existent base directory)
		@Test
		void canSetConventionToNonExistentDirectory() throws IOException {
			val subject = subject().convention(a.fileTree(a.file("src/main/headers")));
			assertThat(subject.getAsFileTree(), emptyIterable());
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/headers")));
		}

		@Test
		void canSetConventionToFileCollectionOfNonExistentBaseDirectories() throws IOException {
			val subject = subject().convention(a.fileCollection(a.file("src/main/headers"), a.file("src/main/public")));
			assertThat(subject.getAsFileTree(), emptyIterable());
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main/headers"), a.file("src/main/public")));
		}
		//endregion

		//region convention(file tree)
		@Test
		void canConfigureSourceSetConventionUsingFileTree() throws IOException {
			val subject = subject().convention(a.fileTree(newFiles(a.newDirectory("b1"))));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b1/f1"), a.file("b1/f2")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b1")));
		}

		@Test
		void canConfigureSourceSetConventionWithIncludeFilter() throws IOException {
			val subject = subject().convention(a.fileTree(newFiles(a.newDirectory("b2"))).matching(p -> p.include("f1")));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b2/f1")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b2")));
		}

		@Test
		void canConfigureSourceSetConventionWithExcludeFilter() throws IOException {
			val subject = subject().convention(a.fileTree(newFiles(a.newDirectory("b3"))).matching(p -> p.exclude("f1")));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b3/f2")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b3")));
		}

		@Test
		void canConfigureGlobalSourceSetFilterWithConvention() throws IOException {
			val subject = subject().convention(a.fileTree(newFiles(a.newDirectory("b4")))).filter(p -> p.exclude("f2"));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b4/f1")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b4")));
		}

		@Test
		void overwriteConventionWhenFroming() throws IOException {
			val subject = subject()
				.convention(a.fileTree(newFiles(a.newDirectory("b5"))))
				.from(a.fileTree(newFiles(a.newDirectory("b6"))));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("b6/f1"), a.file("b6/f2")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("b6")));
		}
		//endregion
	}

	@Nested
	class CommonUsageTest {
		@Test
		void canAccessRelativeToBaseDirectoryPath() throws IOException {
			newFiles(a.newDirectory("root/dir1"));
			newFiles(a.newDirectory("root/dir2"));

			assertThat(relativePaths(subject().from(a.file("root"))), containsInAnyOrder("dir1/f1", "dir1/f2", "dir2/f1", "dir2/f2"));
		}

		@Test
		void canAccessRelativeToRespectiveBaseDirectoryPath() throws IOException {
			val dir1 = a.newDirectory("dir1");
			a.newFile("dir1/file1-1");
			a.newFile("dir1/nestedDir/file1-2");
			val dir2 = a.newDirectory("dir2");
			a.newFile("dir2/file2-1");
			a.newFile("dir2/nestedDir/file2-2");

			assertThat(relativePaths(subject().from(dir1, dir2)), containsInAnyOrder("file1-1", "nestedDir/file1-2", "file2-1", "nestedDir/file2-2"));
		}

		private Set<String> relativePaths(SourceSet sourceSet) {
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

	@Nested
	class BuildDependenciesTest {
		@Test
		void conservesBuildDependenciesOnSourceDirectories() {
			val project = rootProject();
			val buildTask = project.getTasks().create("buildTask");
			val files = project.files(project.file("foo")).builtBy(buildTask);
			val sourceDirectories = subject().from(files).getSourceDirectories();
			assertThat(buildDependencies(sourceDirectories), containsInAnyOrder(buildTask));
		}

		@Test
		void conservesBuildDependenciesOnFileTree() {
			val project = rootProject();
			val buildTask = project.getTasks().create("buildTask");
			val files = project.files(project.file("foo")).builtBy(buildTask);
			val fileTree = subject().from(files).getAsFileTree();
			assertThat(buildDependencies(fileTree), containsInAnyOrder(buildTask));
		}

		@Test
		void hasBuildDependencies() {
			val project = rootProject();
			val buildTask = project.getTasks().create("buildTask");
			val files = project.files(project.file("foo")).builtBy(buildTask);
			assertThat(buildDependencies(subject().from(files)), containsInAnyOrder(buildTask));
		}

		@Test
		void hasNoBuildDependenciesForEmptySourceSet() {
			assertThat(buildDependencies(subject()), empty());
		}

		@SuppressWarnings("unchecked")
		private Set<Task> buildDependencies(Buildable buildable) {
			return (Set<Task>) buildable.getBuildDependencies().getDependencies(null);
		}
	}

	abstract class AbstractContentTester {
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
			val subject = subject().from(sourceDirectory("srcs"));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
		}

		@Test
		void canAddDirectoryThatGetsCreatedLater() throws IOException {
			val subject = subject().from(sourceDirectory("srcs"));
			newFiles(a.newDirectory("srcs")); // create after was added
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f2")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
		}

		@Test
		void canAddMissingDirectory() throws IOException {
			val subject = subject().from(sourceDirectory("a-missing-directory"));
			assertThat(subject.getAsFileTree(), emptyIterable());
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("a-missing-directory")));
		}

		@Test
		void canAddRegularFile() throws IOException {
			a.newFile("src/a-file");
			val subject = subject().from(sourceFile("src/a-file"));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/a-file")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src")));
		}

		@Test
		void canAddRegularFileThatGetsCreatedLater() throws IOException {
			val subject = subject().from(sourceFile("src/a-file"));
			a.newFile("src/a-file"); // create after was added
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/a-file")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src")));
		}

		@Test
		void canAddMissingRegularFile() throws IOException {
			val subject = subject().from(sourceFile("src/a-missing-file"));
			assertThat(subject.getAsFileTree(), emptyIterable());
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/a-missing-file")));
		}

		@Test
		void canCherryPickRegularFilesOutOfSingleDirectory() throws IOException {
			newFiles(a.newDirectory("srcs"), 5);
			val subject = subject().from(sourceFiles("srcs/f1", "srcs/f4"));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("srcs/f1"), a.file("srcs/f4")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("srcs")));
		}

		@Test
		void canCherryPickRegularFilesOutOfMultipleDirectories() throws IOException {
			newFiles(a.newDirectory("src/main"), 5);
			newFiles(a.newDirectory("src/common"), 5);
			val subject = subject().from(sourceFiles("src/main/f2", "src/common/f1", "src/main/f4", "src/common/f3"));
			assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("src/main/f2"), a.file("src/main/f4"), a.file("src/common/f1"), a.file("src/common/f3")));
			assertThat(subject.getSourceDirectories(), containsInAnyOrder(a.file("src/main"), a.file("src/common")));
		}
	}

	@Nested
	class ContentTest {
		@Nested
		class AsFile extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return Arrays.stream(paths).map(a::file).toArray();
			}
		}

		@Nested
		class AsPath extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return Arrays.stream(paths).map(a::file).map(File::toPath).toArray();
			}
		}

		@Nested
		class AsRelativePath extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return paths;
			}
		}

		@Nested
		class AsFileTree extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return new Object[] { a.fileCollection(Arrays.stream(paths).map(a::file).toArray()).getAsFileTree() };
			}
		}

		@Nested
		class AsFileProvider extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return Arrays.stream(paths).map(a::file).map(it -> ProjectTestUtils.providerFactory().provider(() -> it)).toArray();
			}
		}

		@Nested
		class AsFileCallable extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return Arrays.stream(paths).map(a::file).map(Callables::returning).toArray();
			}
		}

		@Nested
		class AsFileCollectionElementsProvider extends AbstractContentTester {
			@Override
			Object[] source(String... paths) {
				return new Object[] { a.fileCollection(Arrays.stream(paths).map(a::file).toArray())/*.getAsFileTree().getElements()*/ };
			}
		}

		@Nested
		class AsRegularFileAndDirectory extends AbstractContentTester {
			private ProjectLayout projectLayout;

			@BeforeEach
			void createProjectLayout() throws IOException {
				projectLayout = ProjectTestUtils.createRootProject(getTemporaryDirectory()).getLayout();
			}

			@Override
			Object[] source(String... paths) {
				// TODO: Should we have this or always use source Directory and source file .... I think it would be better
				throw new UnsupportedOperationException();
			}

			@Override
			protected Object sourceDirectory(String path) {
				return projectLayout.getProjectDirectory().dir(path);
			}

			@Override
			protected Object sourceFile(String path) {
				return projectLayout.getProjectDirectory().file(path);
			}
		}

		@Test
		void canAddFilesFluently() throws IOException {
			assertThat(subject()
					.from(a.newFile("f1"))
					.from(a.newFile("f2"))
					.getAsFileTree(),
				containsInAnyOrder(a.file("f1"), a.file("f2")));
		}

		@Test
		void canAddSingleFileToSourceSet() throws IOException {
			assertThat(subject().from(a.newFile("f3")).getAsFileTree(), containsInAnyOrder(a.file("f3")));
		}

		@Test
		void canAddFilesFromMultipleFiles() throws IOException {
			assertThat(subject().from(a.newFile("f4"), a.newFile("f5")).getAsFileTree(),
				containsInAnyOrder(a.file("f4"), a.file("f5")));
		}

		@Test
		void canAddFilesFromDirectory() throws IOException {
			assertThat(subject().from(newFiles(a.newDirectory("d"))).getAsFileTree(),
				containsInAnyOrder(a.file("d/f1"), a.file("d/f2")));
		}

		@Test
		void canAddFilesFromNestedDirectories() throws IOException {
			assertThat(subject().from(newFiles(a.file("root")), newFiles(a.newDirectory("root/nested"))).getAsFileTree(),
				containsInAnyOrder(a.file("root/f1"), a.file("root/f2"), a.file("root/nested/f1"), a.file("root/nested/f2")));
		}

		@Test
		void canAddFilesFromMultipleDistinctDirectories() throws IOException {
			assertThat(subject().from(
					newFiles(a.newDirectory("d1")),
					newFiles(a.newDirectory("d2"))
				).getAsFileTree(),
				containsInAnyOrder(a.file("d1/f1"), a.file("d1/f2"), a.file("d2/f1"), a.file("d2/f2")));
		}

		@Test
		void canAddFilesFromNonExistingDirectory() throws IOException {
			assertThat(subject().from(a.file("src/main/cpp")).getAsFileTree().getFiles(), empty());
		}

		@Test
		void canAddFilesFromFileTreeWithNonExistingRootDirectory() throws IOException {
			assertThat(subject().from(a.fileTree(a.file("src/main/headers"))).getAsFileTree().getFiles(), empty());
		}

		@Test
		void canAddFilesFromFileCollectionOfFiles() throws IOException {
			assertThat(subject().from(
				a.fileCollection(
					a.newFile("f6"),
					a.newFile("f7")
				)).getAsFileTree(),
				containsInAnyOrder(a.file("f6"), a.file("f7")));
		}

		@Test
		void canAddFilesFromFileTreeOfDirectory() throws IOException {
			assertThat(subject().from(a.fileTree(newFiles(a.newDirectory("d3")))).getAsFileTree(),
				containsInAnyOrder(a.file("d3/f1"), a.file("d3/f2")));
		}

		@Test
		void honorChangesToSourceSetInContentFileTree() throws IOException {
			val subject = subject().from(a.newFile("f8"));
			val fileTree = subject.getAsFileTree();
			assertThat(fileTree, containsInAnyOrder(a.file("f8")));
			subject.from(a.newFile("f9"));
			assertThat(fileTree, containsInAnyOrder(a.file("f8"), a.file("f9")));
		}
	}
}
