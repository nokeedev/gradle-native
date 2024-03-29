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
package dev.nokee.core.exec;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@EqualsAndHashCode
public final class CommandLineToolExecutable implements Serializable {
	private final String location;

	public CommandLineToolExecutable(Path location) {
		this.location = Objects.requireNonNull(location, "'location' must not be null").toString();
	}

	public Path getLocation() {
		return Paths.get(location);
	}

	@Override
	public String toString() {
		return "executable '" + location + "'";
	}
}
