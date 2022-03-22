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

import java.io.File;
import java.nio.file.Path;

/**
 * A file fixture which provides a unique temporary folder for the test.
 */
public final class TestNameTestDirectoryProvider extends AbstractTestDirectoryProvider {
	public TestNameTestDirectoryProvider(Class<?> klass) {
		// NOTE: the space in the directory name is intentional
		super(new File("build/tmp/test files").toPath(), klass);
	}

	public TestNameTestDirectoryProvider(Path root, Class<?> klass) {
		super(root, klass);
	}

	public static TestNameTestDirectoryProvider newInstance(Class<?> testClass) {
		return new TestNameTestDirectoryProvider(testClass);
	}

	public static TestNameTestDirectoryProvider newInstance(String methodName, Object target) {
		final TestNameTestDirectoryProvider testDirectoryProvider =	new TestNameTestDirectoryProvider(target.getClass());
		testDirectoryProvider.init(methodName);
		return testDirectoryProvider;
	}
}
