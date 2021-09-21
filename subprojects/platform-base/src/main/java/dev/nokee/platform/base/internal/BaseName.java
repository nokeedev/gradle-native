/*
 * Copyright 2020 the original author or authors.
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

import lombok.EqualsAndHashCode;
import org.gradle.util.GUtil;

@EqualsAndHashCode
public final class BaseName {
	private final String baseName;

	private BaseName(String baseName) {
		this.baseName = baseName;
	}

	public static BaseName of(String baseName) {
		return new BaseName(baseName);
	}

	public String getAsString() {
		return baseName;
	}

	public String getAsKebabCase() {
		return GUtil.toWords(baseName.replace("_", ""), '-');
	}

	public String getAsCamelCase() {
		return GUtil.toCamelCase(baseName.replace("_", ""));
	}

	public String getAsLowerCamelCase() {
		return GUtil.toLowerCamelCase(baseName.replace("_", ""));
	}

	@Override
	public String toString() {
		return baseName;
	}
}
