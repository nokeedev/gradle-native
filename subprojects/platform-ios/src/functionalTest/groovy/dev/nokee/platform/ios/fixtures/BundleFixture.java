package dev.nokee.platform.ios.fixtures;

import dev.gradleplugins.test.fixtures.file.TestFile;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class BundleFixture {
	private final TestFile bundle;

	public BundleFixture(TestFile bundle) {
		bundle.assertIsDirectory();
		this.bundle = bundle;
	}

	public BundleFixture assertHasDescendants(String... descendants) throws IOException {
		Set<String> actual = new TreeSet<String>();
		visit(actual);
		Set<String> expected = new TreeSet<String>(Arrays.asList(descendants));

		Set<String> extras = new TreeSet<String>(actual);
		extras.removeAll(expected);
		Set<String> missing = new TreeSet<String>(expected);
		missing.removeAll(actual);

		assertEquals(String.format("For dir: %s\n extra files: %s, missing files: %s, expected: %s", this, extras, missing, expected), expected, actual);

		return this;
	}

	private void visit(Set<String> actual) throws IOException {
		Path baseDirectory = bundle.toPath();
		Files.walkFileTree(baseDirectory, new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
				if (path.getFileName().toString().endsWith(".storyboardc")) {
					actual.add(baseDirectory.relativize(path).toString());
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
				actual.add(baseDirectory.relativize(path).toString());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
