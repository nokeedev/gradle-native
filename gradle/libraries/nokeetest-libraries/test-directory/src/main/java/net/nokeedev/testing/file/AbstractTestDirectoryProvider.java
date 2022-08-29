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
package net.nokeedev.testing.file;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.commons.io.file.PathUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Random;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;


/**
 * A file fixture which provides a unique temporary folder for the test.
 *
 * Note: to avoid 260 char path length limitation on Windows, keep the test directory path as short as possible,
 * ideally {@literal <} 90 chars (from repo root to test dir root, e.g. "core/build/tmp/test files/{TestClass}/{testMethod}/qqlj8"),
 * or {@literal <} 40 chars for "{TestClass}/{testMethod}/qqlj8"
 */
public abstract class AbstractTestDirectoryProvider implements TestDirectoryProvider, AutoCloseable {
	private static final RetryPolicy<Void> RETRY_POLICY = RetryPolicy.<Void>builder().abortOn(InterruptedException.class).withMaxAttempts(100).withDelay(Duration.ofMillis(100)).build();
	protected final Path root;
	private final String className;

	private static final Random RANDOM = new Random();
	private static final int ALL_DIGITS_AND_LETTERS_RADIX = 36;
	private static final int MAX_RANDOM_PART_VALUE = Integer.valueOf("zzzzz", ALL_DIGITS_AND_LETTERS_RADIX);
	private static final Pattern WINDOWS_RESERVED_NAMES = Pattern.compile("(con)|(prn)|(aux)|(nul)|(com\\d)|(lpt\\d)", Pattern.CASE_INSENSITIVE);

	private String prefix;
	private Path dir;
	private boolean cleanup = true;
	private boolean suppressCleanupErrors = false;

	protected AbstractTestDirectoryProvider(Path root, Class<?> testClass) {
		this.root = root.toAbsolutePath();
		this.className = shortenPath(testClass.getSimpleName(), 16);
	}

	@Override
	public void suppressCleanup() {
		cleanup = false;
	}

	@Override
	public void suppressCleanupErrors() {
		suppressCleanupErrors = true;
	}

	public boolean isCleanup() {
		return cleanup;
	}

	public void cleanup() throws IOException {
		if (cleanup && dir != null && Files.exists(dir)) {
			// NOTE: There is a bug in PathUtils.setReadOnly which is called by FileUtils.forceDelete
			//   commons-io 2.11 has the bug but master seems to have the fix which no release was made, yet
			Failsafe.with(RETRY_POLICY).run(() -> PathUtils.deleteDirectory(dir));
		}
	}

	@Override
	public void close() throws IOException {
		cleanup();
	}

//	@Override
//	public Statement apply(final Statement base, Description description) {
//		init(description.getMethodName());
//
//		return new TestDirectoryCleaningStatement(base, description);
//	}
//
//	private class TestDirectoryCleaningStatement extends Statement {
//		private final Statement base;
//		private final Description description;
//
//		TestDirectoryCleaningStatement(Statement base, Description description) {
//			this.base = base;
//			this.description = description;
//		}
//
//		@Override
//		public void evaluate() throws Throwable {
//			// implicitly don't clean up if this throws
//			base.evaluate();
//
//			try {
//				cleanup();
//			} catch (Exception e) {
//				if (suppressCleanupErrors()) {
//					System.err.println(cleanupErrorMessage());
//					e.printStackTrace(System.err);
//				} else {
//					throw new GradleException(cleanupErrorMessage(), e);
//				}
//			}
//		}
//
//		private boolean suppressCleanupErrors() {
//			return suppressCleanupErrors
//				|| testClass().getAnnotation(LeaksFileHandles.class) != null
//				|| description.getAnnotation(LeaksFileHandles.class) != null;
//		}
//
//		private Class<?> testClass() {
//			return description.getTestClass();
//		}
//
//		private String cleanupErrorMessage() {
//			return "Couldn't delete test dir for `" + displayName() + "` (test is holding files open). "
//				+ "In order to find out which files are held open, you may find `org.gradle.integtests.fixtures.executer.GradleExecuter.withFileLeakDetection` useful.";
//		}
//
//		private String displayName() {
//			return description.getDisplayName();
//		}
//	}

	protected void init(String methodName) {
		if (methodName == null) {
			// must be a @ClassRule; use the rule's class name instead
			methodName = getClass().getSimpleName();
		}
		if (prefix == null) {
			String safeMethodName = shortenPath(methodName.replaceAll("[^\\w]", "_"), 16);
			prefix = String.format("%s/%s", className, safeMethodName);
		}
	}

	/**
	 * Shorten a long name to at most {expectedMaxLength}, replace middle characters with ".".
	 */
	private static String shortenPath(String longName, int expectedMaxLength) {
		if (longName.length() <= expectedMaxLength) {
			return longName;
		} else {
			return longName.substring(0, expectedMaxLength - 5) + "." + longName.substring(longName.length() - 4);
		}
	}

	@Override
	public Path getTestDirectory() {
		if (dir == null) {
			dir = createUniqueTestDirectory();
		}
		return dir;
	}

	private Path createUniqueTestDirectory() {
		while (true) {
			// Use a random prefix to avoid reusing test directories
			String randomPrefix = Integer.toString(RANDOM.nextInt(MAX_RANDOM_PART_VALUE), ALL_DIGITS_AND_LETTERS_RADIX);
			if (WINDOWS_RESERVED_NAMES.matcher(randomPrefix).matches()) {
				continue;
			}
			Path dir = root.resolve(getPrefix()).resolve(randomPrefix);
			try {
				return Files.createDirectories(dir);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private String getPrefix() {
		if (prefix == null) {
			// This can happen if this is used in a constructor or a @Before method. It also happens when using
			// @RunWith(SomeRunner) when the runner does not support rules.
			prefix = className;
		}
		return prefix;
	}

	public Path file(Object... path) {
		return getTestDirectory().resolve(stream(path).map(Object::toString).collect(joining(File.separator)));
	}

	public Path createFile(Object... path) {
		final Path result = file(path);
		try {
			Files.createDirectories(result.getParent());
			return Files.createFile(result);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public Path createDirectory(Object... path) {
		try {
			return Files.createDirectories(file(path));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public Path getRoot() {
		return root;
	}
}
