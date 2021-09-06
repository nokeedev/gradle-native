package dev.nokee.internal.testing;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
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
				} else if (actual instanceof Provider) {
					throw new IllegalArgumentException("Please make sure there is not confusion between Provider#map vs Provider#flatMap. Otherwise, use GradleProviderMatchers#providerOf.");
				}
				throw new UnsupportedOperationException(String.format("Unsupported file-like type of '%s'.", actual.getClass().getSimpleName()));
			}
		};
	}

	public static Matcher<File> parentFile(Matcher<File> matcher) {
		return new FeatureMatcher<File, File>(matcher, "a parent file", "the parent file") {
			@Override
			protected File featureValueOf(File actual) {
				return actual.getParentFile();
			}
		};
	}

	/**
	 * Matches unix-normalized absolute path of the {@literal File}.
	 *
	 * @param matcher a absolute path matcher, must not be null
	 * @return a matcher for file's absolute path, never null
	 */
	public static Matcher<File> withAbsolutePath(Matcher<String> matcher) {
		return new FeatureMatcher<File, String>(matcher, "a file with absolute path", "path") {
			@Override
			protected String featureValueOf(File actual) {
				return FilenameUtils.separatorsToUnix(actual.getAbsolutePath());
			}
		};
	}

	public static Matcher<Object> aFileNamed(String fileName) {
		return aFile(FileMatchers.aFileNamed(Matchers.equalTo(fileName)));
	}
}
