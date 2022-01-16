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

import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public final class StrictJavaCompileExtension {
	private static final Iterable<String> STRICT_COMPILER_ARGS = Arrays.asList("-Werror", "-Xlint:all", "-Xlint:-processing", "-Xlint:-serial");
	private final ArgumentProvider<Deprecation> deprecation = new ArgumentProvider<>(Deprecation.Shows);

	public StrictJavaCompileExtension(JavaCompile target) {
		STRICT_COMPILER_ARGS.forEach(target.getOptions().getCompilerArgs()::add);
		target.getOptions().getCompilerArgumentProviders().add(deprecation);
	}

	public void ignoreDeprecations() {
		this.deprecation.set(Deprecation.Ignores);
	}

	private static final class ArgumentProvider<T extends Iterable<String>> implements CommandLineArgumentProvider {
		private T value;

		public ArgumentProvider(T defaultValue) {
			this.value = defaultValue;
		}

		public void set(T value) {
			this.value = value;
		}

		@Override
		public Iterable<String> asArguments() {
			return value;
		}
	}

	private enum Deprecation implements Iterable<String> {
		Shows(Collections.emptyList()), Ignores(Collections.singletonList("-Xlint:-deprecation"));

		private final Iterable<String> args;

		Deprecation(Iterable<String> args) {
			this.args = args;
		}

		@Override
		public Iterator<String> iterator() {
			return args.iterator();
		}
	}
}
