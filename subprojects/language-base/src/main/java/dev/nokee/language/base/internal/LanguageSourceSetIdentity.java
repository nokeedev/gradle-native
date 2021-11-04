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
package dev.nokee.language.base.internal;

import dev.nokee.model.HasName;
import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class LanguageSourceSetIdentity implements HasName {
	private final LanguageSourceSetName name;
	private final String displayName;

	private LanguageSourceSetIdentity(LanguageSourceSetName name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}

	public static LanguageSourceSetIdentity of(String name, String displayName) {
		return new LanguageSourceSetIdentity(LanguageSourceSetName.of(name), requireNonNull(displayName));
	}

	@Override
	public LanguageSourceSetName getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return "sources '" + name + "'";
	}
}
