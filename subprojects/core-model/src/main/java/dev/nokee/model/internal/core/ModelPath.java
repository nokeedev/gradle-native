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
package dev.nokee.model.internal.core;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * A path of a model node.
 */
// TODO: There should be a way to warn when path 'a.b.c' is compared as 3 components vs 1 components.
@EqualsAndHashCode
public final class ModelPath implements Iterable<String> {
	private static final char PATH_SEPARATOR = '.';
	private static final int PATH_SEPARATOR_LENGTH = 1;
	private static final Splitter PATH_SPLITTER = Splitter.on(PATH_SEPARATOR).omitEmptyStrings();
	private static final ModelPath ROOT = new ModelPath("", new String[0]);
	@EqualsAndHashCode.Exclude private final String path;
	private final String[] components;

	@Nullable
	@EqualsAndHashCode.Exclude
	private final ModelPath parent;

	private ModelPath(String path, String[] components) {
		this.path = path;
		this.components = components;
		this.parent = doGetParent();
	}

	public static ModelPath root() {
		return ROOT;
	}

	public static ModelPath path(String path) {
		return new ModelPath(path, splitPath(path));
	}

	public String get() {
		return path;
	}

	public ModelPath child(String name) {
		if (components.length == 0) {
			return new ModelPath(name, new String[]{name});
		}
		return new ModelPath(path + "." + name, pushElement(components, name));
	}

	@Override
	public String toString() {
		if (components.length == 0) {
			return "<root>";
		}
		return path;
	}

	public Optional<ModelPath> getParent() {
		return Optional.ofNullable(parent);
	}

	public boolean isDirectDescendant(ModelPath other) {
		Objects.requireNonNull(other);

		if (other.components.length != components.length + 1) {
			return false;
		}

		return other.getParent().map(this::equals).orElse(false);
	}

	public boolean isDescendant(ModelPath other) {
		Objects.requireNonNull(other);

		int length = components.length;
		if (other.components.length <= components.length) {
			return false;
		}

		for (int i = 0; i < length; i++) {
			if (!components[i].equals(other.components[i])) {
				return false;
			}
		}
		return true;
	}

	private static String[] splitPath(String path) {
		return PATH_SPLITTER.splitToStream(path).toArray(String[]::new);
	}

	private ModelPath doGetParent() {
		if (components.length == 0) {
			return null;
		}

		if (components.length == 1) {
			return ROOT;
		}

		return new ModelPath(dropRight(path, nameOf(components).length() + PATH_SEPARATOR_LENGTH), removeLastElement(components));
	}

	private static String[] pushElement(String[] components, String name) {
		val result = Arrays.copyOf(components, components.length + 1);
		result[result.length - 1] = name;
		return result;
	}

	private static String[] removeLastElement(String[] components) {
		return Arrays.copyOf(components, components.length - 1);
	}

	private static String dropRight(String self, int count) {
		return self.substring(0, self.length() - count);
	}

	private static String nameOf(String[] components) {
		return components[components.length - 1];
	}

	@Override
	public Iterator<String> iterator() {
		return Iterators.forArray(components);
	}

	/**
	 * Returns the name of the path, which is the last segment of the path.
	 *
	 * @return the name of the path, never null.
	 */
	public String getName() {
		if (components.length == 0) {
			return "";
		}
		return components[components.length - 1];
	}
}
