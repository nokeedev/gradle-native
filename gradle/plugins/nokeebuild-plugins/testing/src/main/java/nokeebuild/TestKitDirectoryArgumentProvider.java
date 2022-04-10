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
package nokeebuild;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Internal;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.Collections;
import java.util.Objects;

final class TestKitDirectoryArgumentProvider implements CommandLineArgumentProvider, Named {
	private final Provider<Directory> testKitDirectory;

	TestKitDirectoryArgumentProvider(Project project) {
		this.testKitDirectory = project.getLayout().getBuildDirectory().dir("gradle-runner-kit");
	}

	@Internal
	public Provider<Directory> getTestKitDirectory() {
		return testKitDirectory;
	}

	@Override
	public Iterable<String> asArguments() {
		return Collections.singletonList("-Dorg.gradle.testkit.dir=" + testKitDirectory.get().getAsFile().getAbsolutePath());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		return o instanceof TestKitDirectoryArgumentProvider;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass());
	}

	@Internal
	@Override
	public String getName() {
		return "testKitDirectory";
	}
}
