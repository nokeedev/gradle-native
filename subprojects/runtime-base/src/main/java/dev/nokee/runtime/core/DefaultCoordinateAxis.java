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
package dev.nokee.runtime.core;

import lombok.EqualsAndHashCode;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
final class DefaultCoordinateAxis<T> implements CoordinateAxis<T> {
	private final Class<T> type;
	private final String name;

	public DefaultCoordinateAxis(Class<T> type) {
		this.type = requireNonNull(type);
		this.name = Coordinates.inferCoordinateAxisNameFromType(type);
	}

	public DefaultCoordinateAxis(Class<T> type, String name) {
		requireNonNull(type);
		requireNonNull(name);
		checkArgument(!name.isEmpty(), "coordinate axis name cannot be empty");
		checkArgument(name.chars().noneMatch(Character::isSpaceChar), "coordinate axis name cannot contains spaces");
		this.type = type;
		this.name = name;
	}

	public Class<T> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "axis <" + name + ">";
	}
}
