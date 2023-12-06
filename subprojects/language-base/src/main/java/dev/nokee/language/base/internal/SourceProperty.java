/*
 * Copyright 2023 the original author or authors.
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

import org.gradle.api.file.ConfigurableFileCollection;

import javax.inject.Inject;
import java.util.Objects;

public abstract /*final*/ class SourceProperty implements ISourceProperty {
	private final String name;

	@Inject
	public SourceProperty(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public abstract ConfigurableFileCollection getSource();

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SourceProperty that = (SourceProperty) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return "source property '" + name + "'";
	}
}
