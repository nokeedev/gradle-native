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
package dev.nokee.platform.base.internal;

import dev.nokee.model.HasName;
import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BinaryIdentity implements HasName {
	private final BinaryName name;
	private final String displayName;

	BinaryIdentity(BinaryName name, String displayName) {
		this.name = requireNonNull(name);
		this.displayName = requireNonNull(displayName);
	}

	@Override
	public BinaryName getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static BinaryIdentity of(String name) {
		return new BinaryIdentity(BinaryName.of(name), "binary");
	}

	public static BinaryIdentity of(String name, String displayName) {
		return new BinaryIdentity(BinaryName.of(name), displayName);
	}
}
