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
package dev.nokee.xcode;

import dev.nokee.util.internal.NotPredicate;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public abstract class XCFileReference {
	public abstract Path resolve(ResolveContext context);

	public abstract XCFileType getType();

	public interface ResolveContext {
		Path getBuiltProductsDirectory();

		Path get(String name);
	}

	public enum XCFileType {
		ABSOLUTE, BUILT_PRODUCT, BUILD_SETTING
	}

	public static XCFileReference absoluteFile(String... paths) {
		final String path = Arrays.stream(paths) //
			.map(Objects::requireNonNull) //
			.map(String::trim) //
			.filter(NotPredicate.not(String::isEmpty)) //
			.collect(Collectors.joining("/"));
		return new AbsoluteFileReference(path);
	}

	public static XCFileReference builtProduct(String... paths) {
		final String path = Arrays.stream(paths) //
			.map(Objects::requireNonNull) //
			.map(String::trim) //
			.filter(NotPredicate.not(String::isEmpty)) //
			.collect(Collectors.joining("/"));
		return new BuiltProductReference(path);
	}

	public static XCFileReference fromBuildSetting(String buildSetting, String path) {
		return new BuildSettingFileReference(requireNonNull(buildSetting), requireNonNull(path));
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class AbsoluteFileReference extends XCFileReference {
		private final XCString path;

		private AbsoluteFileReference(String path) {
			this.path = XCString.of(path);
		}

		@Override
		public Path resolve(ResolveContext context) {
			return Paths.get(requireNonNull(path.resolve(variableName -> {
				// FIXME: the resolve context for XCFileReference is opinionated to return Path
				return context.get(variableName).toString();
			})));
		}

		@Override
		public XCFileType getType() {
			return XCFileType.ABSOLUTE;
		}

		@Override
		public String toString() {
			return path.toString();
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class BuiltProductReference extends XCFileReference {
		private final XCString path;

		private BuiltProductReference(String path) {
			this.path = XCString.of(path);
		}

		@Override
		public Path resolve(ResolveContext context) {
			return context.getBuiltProductsDirectory().resolve(requireNonNull(path.resolve(variableName -> {
				// FIXME: the resolve context for XCFileReference is opinionated to return Path
				return context.get(variableName).toString();
			})));
		}

		@Override
		public XCFileType getType() {
			return XCFileType.BUILT_PRODUCT;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			builder.append("$(BUILT_PRODUCT_DIR)");
			if (!path.toString().isEmpty()) {
				builder.append('/').append(path);
			}
			return builder.toString();
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class BuildSettingFileReference extends XCFileReference {
		private final String buildSetting;
		private final XCString path;

		private BuildSettingFileReference(String buildSetting, String path) {
			this.buildSetting = buildSetting;
			this.path = XCString.of(path);
		}

		@Override
		public Path resolve(ResolveContext context) {
			return context.get(buildSetting).resolve(requireNonNull(path.resolve(variableName -> {
				// FIXME: the resolve context for XCFileReference is opinionated to return Path
				return context.get(variableName).toString();
			})));
		}

		@Override
		public XCFileType getType() {
			return XCFileType.BUILD_SETTING;
		}

		@Override
		public String toString() {
			return "$(" + buildSetting + ")/" + path;
		}
	}
}
