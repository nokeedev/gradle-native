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
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.io.FileMatchers;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.nio.file.Files.walkFileTree;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;

public final class FileSystemMatchers {
	/**
	 * Matches a file-like object with the specified {@link File} matcher.
	 *
	 * @param matcher  the file matcher, must not be null
	 * @return a file matcher, never null
	 */
	public static <T> Matcher<T> aFile(Matcher<? super File> matcher) {
		return new FeatureMatcher<T, File>(matcher, "a file", "the file") {
			@Override
			protected File featureValueOf(T actual) {
				if (actual instanceof FileSystemLocation) {
					return ((FileSystemLocation) actual).getAsFile();
				} else if (actual instanceof File) {
					return (File) actual;
				} else if (actual instanceof Path) {
					return ((Path) actual).toFile();
				} else if (actual instanceof DescendantFile) {
					return ((DescendantFile) actual).getFile();
				} else if (actual instanceof String) {
					return Paths.get((String) actual).toFile(); // TODO: we should validate the string represent a path
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

	public static Matcher<File> hasAbsolutePath(String expectedAbsolutePath) {
		if (SystemUtils.IS_OS_WINDOWS) {
			// On Windows, it prepends the drive letter
			return withAbsolutePath(endsWith(FilenameUtils.separatorsToUnix(expectedAbsolutePath)));
		} else {
			return withAbsolutePath(equalTo(FilenameUtils.separatorsToUnix(expectedAbsolutePath)));
		}
	}

	public static Matcher<Path> absolutePath(Matcher<String> matcher) {
		return new FeatureMatcher<Path, String>(matcher, "a path with absolute", "path") {
			@Override
			protected String featureValueOf(Path actual) {
				return FilenameUtils.separatorsToUnix(actual.toAbsolutePath().toString());
			}
		};
	}

	public static Matcher<Object> aFileNamed(String fileName) {
		return aFile(FileMatchers.aFileNamed(equalTo(fileName)));
	}

	public static Matcher<Object> aFileNamed(Matcher<String> matcher) {
		return aFile(FileMatchers.aFileNamed(matcher));
	}

	public static Matcher<Object> aFileBaseNamed(String baseName) {
		return aFileNamed(withoutExtension(equalTo(baseName)));
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

	public static Matcher<String> normalizePaths(Matcher<? super String> matcher) {
		return new FeatureMatcher<String, String>(matcher, "", "") {
			@Override
			protected String featureValueOf(String actual) {
				// Replace Windows absolute path to *nix absolute path
				return actual.replaceAll("[A-Z]:\\\\", "/").replace('\\', '/');
			}
		};
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

	public static Matcher<String> ofLines(String... lines) {
		return new FeatureMatcher<String, Iterable<String>>(contains(lines), "", "") {
			@Override
			protected Iterable<String> featureValueOf(String actual) {
				return Arrays.asList(actual.split("\r?\n"));
			}
		};
	}

	/**
	 * Matches a directory contains exactly the given set of descendants relative to the base directory.
	 */
	public static Matcher<Object> hasRelativeDescendants(String... descendants) {
		return allOf(anExistingDirectory(), hasDescendants(stream(descendants).map(FileSystemMatchers::withRelativePath).collect(toList())));
	}

	/**
	 * Matches a directory contains exactly the given set of descendant files.
	 *
	 * @param descendantMatchers  a list of matchers, each of which must be satisfied by a {@link DescendantFile}
	 * @return a matcher satisfied by all descendant files of a base directory, never null
	 */
	@SafeVarargs
	@SuppressWarnings("varargs")
	public static Matcher<Object> hasDescendants(Matcher<? super DescendantFile>... descendantMatchers) {
		return hasDescendants(copyOf(descendantMatchers));
	}

	/**
	 * Matches a directory contains exactly the given set of descendant files.
	 *
	 * @param descendantMatchers  a list of matchers, each of which must be satisfied by a {@link DescendantFile}
	 * @return a matcher satisfied by all descendant files of a base directory, never null
	 */
	public static Matcher<Object> hasDescendants(Collection<Matcher<? super DescendantFile>> descendantMatchers) {
		return allOf(anExistingDirectory(), aFile(new DescendantsFeature(containsInAnyOrder(descendantMatchers))));
	}

	public static Matcher<DescendantFile> withRelativePath(String path) {
		return new FeatureMatcher<DescendantFile, String>(equalTo(path), "", "") {
			@Override
			protected String featureValueOf(DescendantFile actual) {
				return actual.getRelativePath();
			}
		};
	}

	private static Set<DescendantFile> descendants(File self) {
		final Set<DescendantFile> result = new LinkedHashSet<>();
		try {
			walkFileTree(self.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					result.add(new DescendantFile(self, file.toFile()));
					return FileVisitResult.CONTINUE;
				}
			});
			return result;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static final class DescendantsFeature extends FeatureMatcher<File, Iterable<DescendantFile>> {
		public DescendantsFeature(Matcher<? super Iterable<DescendantFile>> subMatcher) {
			super(subMatcher, "", "");
		}

		@Override
		protected Iterable<DescendantFile> featureValueOf(File actual) {
			return descendants(actual);
		}
	}

	public static final class DescendantFile {
		private final File baseDirectory;
		private final File actualFile;

		public DescendantFile(File baseDirectory, File actualFile) {
			this.baseDirectory = baseDirectory;
			this.actualFile = actualFile;
		}

		public String getRelativePath() {
			return baseDirectory.toURI().relativize(actualFile.toURI()).toString();
		}

		public File getFile() {
			return actualFile;
		}
	}
}
