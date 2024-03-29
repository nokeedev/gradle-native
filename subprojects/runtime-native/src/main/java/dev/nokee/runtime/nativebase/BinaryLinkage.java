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
package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.core.CoordinateAxis;
import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

import java.util.Objects;

/**
 * Specify how a native component is linked into a binary.
 * Common binary linkages are 'shared' and 'static', but others may be defined.
 *
 * @since 0.5
 */
public abstract class BinaryLinkage implements Named {
	/**
	 * The binary linkage attribute for dependency resolution.
	 */
	public static final Attribute<BinaryLinkage> BINARY_LINKAGE_ATTRIBUTE = Attribute.of("dev.nokee.linkage", BinaryLinkage.class);

	/**
	 * The binary linkage coordinate axis for variant calculation.
	 */
	public static final CoordinateAxis<BinaryLinkage> BINARY_LINKAGE_COORDINATE_AXIS = CoordinateAxis.of(BinaryLinkage.class, "linkage");

	/**
	 * Creates an binary linkage instance of the specified name.
	 *
	 * @param name the name of the binary linkage, must not be null
	 * @return a binary linkage instance representing the specified name, never null
	 */
	public static BinaryLinkage named(String name) {
		return new DefaultBinaryLinkage(name);
	}

	/**
	 * The shared library linkage.
	 */
	public static final String SHARED = "shared";

	/**
	 * @return {@code true} if shared library binary linkage or {@code false} otherwise
	 */
	public boolean isShared() {
		return is(SHARED);
	}

	/**
	 * The static library linkage.
	 */
	public static final String STATIC = "static";

	/**
	 * @return {@code true} if static library binary linkage or {@code false} otherwise
	 */
	public boolean isStatic() {
		return is(STATIC);
	}

	/**
	 * The executable linkage.
	 */
	public static final String EXECUTABLE = "executable";

	/**
	 * @return {@code true} if executable binary linkage or {@code false} otherwise
	 */
	public boolean isExecutable() {
		return is(EXECUTABLE);
	}

	/**
	 * The bundle linkage.
	 */
	public static final String BUNDLE = "bundle";

	/**
	 * @return {@code true} if bundle binary linkage or {@code false} otherwise
	 */
	public boolean isBundle() {
		return is(BUNDLE);
	}

	private boolean is(String linkage) {
		return getName().equals(linkage);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof BinaryLinkage)) {
			return false;
		}

		BinaryLinkage other = (BinaryLinkage) obj;
		return Objects.equals(getName(), other.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getName());
	}

	@Override
	public String toString() {
		return getName();
	}
}
