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
package dev.nokee.internal.testing;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.io.FileMatchers;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

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
				} else {
					try {
						return (File) actual.getClass().getMethod("getAsFile").invoke(actual);
					} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
						// ignores
					}
				}
				throw new UnsupportedOperationException(String.format("Unsupported file-like type of '%s'.", actual.getClass().getSimpleName()));
			}
		};
	}

	public static Matcher<Object> aFile(File instance) {
		return aFile(is(instance));
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

	public static Matcher<Object> aFileNamed(Matcher<String> matcher) {
		return aFile(FileMatchers.aFileNamed(matcher));
	}

	public static Matcher<Object> aFileBaseNamed(String baseName) {
		return aFileNamed(withoutExtension(Matchers.equalTo(baseName)));
	}

	public static Matcher<Object> aFileBaseNamed(Matcher<String> matcher) {
		return aFileNamed(withoutExtension(matcher));
	}

	private static Matcher<String> withoutExtension(Matcher<String> matcher) {
		return new FeatureMatcher<String, String>(matcher, "", "") {
			@Override
			protected String featureValueOf(String actual) {
				return FilenameUtils.removeExtension(actual);
			}
		};
	}

	public static Matcher<String> containsPath(String path) {
		return matchesPattern(Pattern.compile(".*" + path.replace("/", "[/\\\\]") + ".*"));
	}

	public static Matcher<Object> anExistingFile() {
		return aFile(FileMatchers.anExistingFile());
	}

	public static Matcher<Object> anExistingDirectory() {
		return aFile(FileMatchers.anExistingDirectory());
	}

	public static Matcher<File> withTextContent(Matcher<? super String> matcher) {
		return new FeatureMatcher<File, String>(matcher, "", "") {
			@Override
			protected String featureValueOf(File actual) {
				try {
					return new String(Files.readAllBytes(actual.toPath()));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		};
	}
}
