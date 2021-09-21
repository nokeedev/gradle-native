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
package dev.nokee.runtime.darwin.internal.parsers;

import lombok.Value;
import lombok.val;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Value
public class TextAPI {
	Collection<Target> targets;

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<Target> targets = new ArrayList<>();

		public Builder target(Target target) {
			targets.add(target);
			return this;
		}

		public TextAPI build() {
			return new TextAPI(targets);
		}
	}

	@Value
	public static class Target {
		String architecture;
		String operatingSystem;
	}

	public static TextAPIReader newReader(Path path) throws IOException {
		return new TextAPIReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8));
	}

	public static <T> T withReader(Path path, Function<? super TextAPIReader, ? extends T> mapper) throws IOException {
		try (val reader = newReader(path)) {
			return mapper.apply(reader);
		}
	}

	public static TextAPI read(Path path) throws IOException {
		try (val reader = newReader(path)) {
			return reader.read();
		}
	}
}
