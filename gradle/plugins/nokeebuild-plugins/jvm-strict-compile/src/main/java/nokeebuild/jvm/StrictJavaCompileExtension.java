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

package nokeebuild.jvm;

import org.gradle.api.tasks.compile.JavaCompile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class StrictJavaCompileExtension {
	private static final Iterable<String> STRICT_COMPILER_ARGS = Arrays.asList("-Werror", "-Xlint:all");
	private final Set<XlintWarning> ignoredWarnings = new HashSet<>();
	private final JavaCompile target;

	public StrictJavaCompileExtension(JavaCompile target) {
		this.target = target;
		STRICT_COMPILER_ARGS.forEach(target.getOptions().getCompilerArgs()::add);
	}

	public void ignore(XlintWarning warningToIgnore) {
		if (ignoredWarnings.add(warningToIgnore)) {
			target.getOptions().getCompilerArgs().add("-Xlint:-" + warningToIgnore.getName());
		}
	}

	public static StrictJavaCompileExtension strictCompile(JavaCompile task) {
		return (StrictJavaCompileExtension) task.getExtensions().getByName("strictCompile");
	}
}
