package dev.nokee.language.base.testers;

import com.google.common.util.concurrent.Callables;
import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.gradle.api.file.ProjectLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

public abstract class LanguageSourceSetContentTester<T extends LanguageSourceSet> {
	public abstract T createSubject();
	public abstract T createSubject(File temporaryDirectory);

	@Nested
	class AsFile extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return Arrays.stream(paths).map(a::file).toArray();
		}
	}

	@Nested
	class AsPath extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return Arrays.stream(paths).map(a::file).map(File::toPath).toArray();
		}
	}

	@Nested
	class AsRelativePath extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return paths;
		}
	}

	@Nested
	class AsFileTree extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return new Object[] { a.fileCollection(Arrays.stream(paths).map(a::file).toArray()).getAsFileTree() };
		}
	}

	@Nested
	class AsFileProvider extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return Arrays.stream(paths).map(a::file).map(it -> TestUtils.providerFactory().provider(() -> it)).toArray();
		}
	}

	@Nested
	class AsFileCallable extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return Arrays.stream(paths).map(a::file).map(Callables::returning).toArray();
		}
	}

	@Nested
	class AsFileCollectionElementsProvider extends AbstractLanguageSourceSetContentTester {
		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
		}

		@Override
		Object[] source(String... paths) {
			return new Object[] { a.fileCollection(Arrays.stream(paths).map(a::file).toArray())/*.getAsFileTree().getElements()*/ };
		}
	}

	@Nested
	class AsRegularFileAndDirectory extends AbstractLanguageSourceSetContentTester {
		private ProjectLayout projectLayout;

		@BeforeEach
		void createProjectLayout() {
			projectLayout = TestUtils.createRootProject(temporaryDirectory).getLayout();
		}

		@Override
		LanguageSourceSet createSubject() {
			return LanguageSourceSetContentTester.this.createSubject(temporaryDirectory);
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
	void canAddFilesFluently(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject()
				.from(a.newFile("f1"))
				.from(a.newFile("f2"))
				.getAsFileTree(),
			containsInAnyOrder(a.file("f1"), a.file("f2")));
	}

	@Test
	void canAddSingleFileToSourceSet(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.newFile("f3")).getAsFileTree(), containsInAnyOrder(a.file("f3")));
	}

	@Test
	void canAddFilesFromMultipleFiles(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.newFile("f4"), a.newFile("f5")).getAsFileTree(),
			containsInAnyOrder(a.file("f4"), a.file("f5")));
	}

	@Test
	void canAddFilesFromDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(newFiles(a.newDirectory("d"))).getAsFileTree(),
			containsInAnyOrder(a.file("d/f1"), a.file("d/f2")));
	}

	@Test
	void canAddFilesFromNestedDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(newFiles(a.rootDirectory()), newFiles(a.newDirectory("nested"))).getAsFileTree(),
			containsInAnyOrder(a.file("f1"), a.file("f2"), a.file("nested/f1"), a.file("nested/f2")));
	}

	@Test
	void canAddFilesFromMultipleDistinctDirectories(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(
				newFiles(a.newDirectory("d1")),
				newFiles(a.newDirectory("d2"))
			).getAsFileTree(),
			containsInAnyOrder(a.file("d1/f1"), a.file("d1/f2"), a.file("d2/f1"), a.file("d2/f2")));
	}

	@Test
	void canAddFilesFromNonExistingDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.file("src/main/cpp")).getAsFileTree().getFiles(), empty());
	}

	@Test
	void canAddFilesFromFileTreeWithNonExistingRootDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.fileTree(a.file("src/main/headers"))).getAsFileTree().getFiles(), empty());
	}

	@Test
	void canAddFilesFromFileCollectionOfFiles(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(
			a.fileCollection(
				a.newFile("f6"),
				a.newFile("f7")
			)).getAsFileTree(),
			containsInAnyOrder(a.file("f6"), a.file("f7")));
	}

	@Test
	void canAddFilesFromFileTreeOfDirectory(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.fileTree(newFiles(a.newDirectory("d3")))).getAsFileTree(),
			containsInAnyOrder(a.file("d3/f1"), a.file("d3/f2")));
	}

	@Test
	void honorChangesToSourceSetInContentFileTree(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().from(a.newFile("f8"));
		val fileTree = subject.getAsFileTree();
		assertThat(fileTree, containsInAnyOrder(a.file("f8")));
		subject.from(a.newFile("f9"));
		assertThat(fileTree, containsInAnyOrder(a.file("f8"), a.file("f9")));
	}
}
