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

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.names.MainName;

import java.util.ArrayList;
import java.util.List;

public final class OutputDirectoryPath {
	private final String value;

	private OutputDirectoryPath(String value) {
		this.value = value;
	}

	public static OutputDirectoryPath forBinary(ModelObjectIdentifier identifier) {
		final List<String> result = new ArrayList<>();
		if (!(identifier.getName() instanceof MainName)) {
			result.add(0, identifier.getName().toString());
		}

		while ((identifier = identifier.getParent()) != null) {
			if (identifier instanceof ProjectIdentifier) {
				// ignore
			} else {
				final String name = identifier.getName().toString();
				if (!name.isEmpty()) {
					result.add(0, name);
				}
			}
		}

		return new OutputDirectoryPath(String.join("/", result));
	}

	public static OutputDirectoryPath forIdentifier(ModelObjectIdentifier identifier) {
		final List<String> result = new ArrayList<>();

		do {
			if (identifier instanceof ProjectIdentifier) {
				// ignore
			} else {
				final String name = identifier.getName().toString();
				if (!name.isEmpty()) {
					result.add(0, name);
				}
			}
		} while ((identifier = identifier.getParent()) != null);

		return new OutputDirectoryPath(String.join("/", result));
	}

	@Override
	public String toString() {
		return value;
	}
}
