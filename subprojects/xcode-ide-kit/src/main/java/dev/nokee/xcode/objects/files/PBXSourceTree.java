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
package dev.nokee.xcode.objects.files;

import com.google.common.base.CharMatcher;
import lombok.EqualsAndHashCode;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents the "Location" dropdown in Xcode's File Inspector.
 */
@EqualsAndHashCode
public final class PBXSourceTree {
	private static final ConcurrentMap<String, PBXSourceTree> knownValues = new ConcurrentHashMap<>();

	/**
	 * Relative to the path of the group containing this.
	 */
	public static final PBXSourceTree GROUP = of("<group>");

	/**
	 * Absolute system path.
	 */
	public static final PBXSourceTree ABSOLUTE = of("<absolute>");
	/**
	 * Relative to the build setting {@code BUILT_PRODUCTS_DIR}.
	 */
	public static final PBXSourceTree BUILT_PRODUCTS_DIR = of("BUILT_PRODUCTS_DIR");

	/**
	 * Relative to the build setting {@code SDKROOT}.
	 */
	public static final PBXSourceTree SDKROOT = of("SDKROOT");

	/**
	 * Relative to the directory containing the project file {@code SOURCE_ROOT}.
	 */
	public static final PBXSourceTree SOURCE_ROOT = of("SOURCE_ROOT");

	/**
	 * Relative to the Developer content directory inside the Xcode application
	 * (e.g. {@code /Applications/Xcode.app/Contents/Developer}).
	 */
	public static final PBXSourceTree DEVELOPER_DIR = of("DEVELOPER_DIR");

	private final String rep;

	private PBXSourceTree(String str) {
		rep = str;
	}

	/**
	 * Return a sourceTree given a build setting that is typically used as a source tree prefix.
	 * <p>
	 * The build setting may be optionally prefixed by '$' which will be stripped.
	 *
	 * @param buildSetting  build setting to convert into source tree, must not be null
	 * @return source tree type from build setting or empty optional
	 */
	public static Optional<PBXSourceTree> fromBuildSetting(String buildSetting) {
		String data = CharMatcher.is('$').trimLeadingFrom(buildSetting);
		switch (data) {
			case "BUILT_PRODUCTS_DIR":
				return Optional.of(BUILT_PRODUCTS_DIR);
			case "SDKROOT":
				return Optional.of(SDKROOT);
			case "SOURCE_ROOT":
				return Optional.of(SOURCE_ROOT);
			case "DEVELOPER_DIR":
				return Optional.of(DEVELOPER_DIR);
			default:
				return Optional.empty();
		}
	}

	public static PBXSourceTree of(String name) {
		return knownValues.computeIfAbsent(name, PBXSourceTree::new);
	}

	@Override
	public String toString() {
		return rep;
	}
}
