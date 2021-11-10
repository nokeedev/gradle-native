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

import com.google.common.collect.Streams;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.ProjectIdentifier;
import lombok.val;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OutputDirectoryPath {
	private final String value;

	private OutputDirectoryPath(String value) {
		this.value = value;
	}

	public static OutputDirectoryPath fromIdentifier(DomainObjectIdentifier identifier) {
		val result = Streams.stream(identifier)
			.flatMap(it -> {
				if (it instanceof ProjectIdentifier) {
					return Stream.empty();
				} else if (it instanceof BinaryIdentity) {
					return Stream.of((BinaryIdentity) it).filter(t -> !t.isMain()).map(t -> t.getName().get());
				} else if (it instanceof HasName) {
					return Stream.of(((HasName) it).getName().toString()).filter(s -> !s.isEmpty());
				} else {
					throw new UnsupportedOperationException();
				}
			})
			.collect(Collectors.joining("/"));
		return new OutputDirectoryPath(result);
	}

	@Override
	public String toString() {
		return value;
	}
}
