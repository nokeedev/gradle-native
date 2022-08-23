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
package dev.nokee.xcode.project;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;

public final class PBXObjectReference {
	private final String globalID;
	private final PBXObjectFields fields;

	public PBXObjectReference(String globalID, PBXObjectFields fields) {
		this.globalID = globalID;
		this.fields = fields;
	}

	public String getGlobalID() {
		return globalID;
	}

	public String isa() {
		return Objects.requireNonNull(fields.get("isa"), () -> String.format("no 'isa' field, found: %s", fields.entrySet().stream().map(Map.Entry::getKey).collect(joining(", ")))).toString();
	}

	public PBXObjectFields getFields() {
		return fields;
	}

	public static PBXObjectReference of(String globalID, Consumer<? super PBXObjectFields.Builder> builderConsumer) {
		final PBXObjectFields.Builder builder = PBXObjectFields.builder();
		builderConsumer.accept(builder);
		return new PBXObjectReference(globalID, builder.build());
	}
}
