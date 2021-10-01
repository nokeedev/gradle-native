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

package dev.nokee.internal.testing.runnerkit;

import com.google.common.collect.Streams;

import java.io.File;
import java.util.Objects;
import java.util.stream.Collectors;

public final class DependencyNotation implements CodeSegment {
	public static DependencyNotation files(Iterable<? extends File> files) {
		return new DependencyNotation(new GenericSection(
			() -> "files(" + Streams.stream(files).map(File::toURI).map(Objects::toString).
				map(DependencyNotation::singleQuote).collect(Collectors.joining(", ")) + ")",
			() -> "files(" + Streams.stream(files).map(File::toURI).map(Objects::toString)
				.map(DependencyNotation::doubleQuote).collect(Collectors.joining(", ")) + ")"
		));
	}

	public static DependencyNotation fromString(String s) {
		return new DependencyNotation(new GenericSection(
			() -> singleQuote(s),
			() -> doubleQuote(s)
		));
	}

	private final Section delegate;

	private DependencyNotation(Section delegate) {
		this.delegate = delegate;
	}

	@Override
	public String toString(GradleDsl dsl) {
		return delegate.generateSection(dsl);
	}

	@Override
	public String toString() {
		return toString(GradleDsl.GROOVY);
	}

	private static String singleQuote(String s) {
		return "'" + s + "'";
	}

	private static String doubleQuote(String s) {
		return "\"" + s + "\"";
	}
}
