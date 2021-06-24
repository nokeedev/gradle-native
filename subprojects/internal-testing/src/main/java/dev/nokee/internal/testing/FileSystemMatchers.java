package dev.nokee.internal.testing;

import org.gradle.api.file.FileSystemLocation;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.io.FileMatchers;

import java.io.File;
import java.nio.file.Path;

public final class FileSystemMatchers {
	/**
	 * Matches a file-like object with the specified {@link File} matcher.
	 *
	 * @param matcher  the file matcher, must not be null
	 * @return a file matcher, never null
	 */
	public static Matcher<Object> aFile(Matcher<? super File> matcher) {
		return new FeatureMatcher<Object, File>(matcher, "a file", "the file") {
			@Override
			protected File featureValueOf(Object actual) {
				if (actual instanceof FileSystemLocation) {
					return ((FileSystemLocation) actual).getAsFile();
				} else if (actual instanceof File) {
					return (File) actual;
				} else if (actual instanceof Path) {
					return ((Path) actual).toFile();
				}
				throw new UnsupportedOperationException();
			}
		};
	}

	public static Matcher<File> withAbsolutePath(Matcher<String> matcher) {
		return FileMatchers.aFileWithAbsolutePath(matcher);
	}

	public static Matcher<Object> aFileNamed(String fileName) {
		return aFile(FileMatchers.aFileNamed(Matchers.equalTo(fileName)));
	}
}
