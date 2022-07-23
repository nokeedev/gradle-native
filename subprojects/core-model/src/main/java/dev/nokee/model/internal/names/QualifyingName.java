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
package dev.nokee.model.internal.names;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * A qualifying name represent a subset of the parents' element names.
 * Although the name typically represent the domain object's ownership hierarchy, users should not use it as such.
 * Typically, the qualifying name will be use as a prefix or suffix to the element name to create {@link FullyQualifiedName} or {@link RelativeName}.
 */
@EqualsAndHashCode
public final class QualifyingName {
	private static final QualifyingName EMPTY = new QualifyingName("");
	private final String name;

	private QualifyingName(String name) {
		this.name = name;
	}

	public static QualifyingName empty() {
		return EMPTY;
	}

	public static QualifyingName of(String name) {
		return new QualifyingName(name);
	}

	public boolean isEmpty() {
		return name.isEmpty();
	}

	public String toString() {
		return name;
	}

	public static final class Builder {
		private String value = "";

		public Builder prepend(String suffix) {
			if (!suffix.isEmpty()) {
				this.value = suffix + StringUtils.capitalize(value);
			}
			return this;
		}

		public QualifyingName build() {
			return new QualifyingName(value);
		}
	}
}
