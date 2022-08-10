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

import lombok.EqualsAndHashCode;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public abstract class XCFileReference {
	public abstract Path resolve(ResolveContext context);

	public abstract XCFileType getType();

	public interface ResolveContext {
		Path getBuiltProductDirectory();

		Path get(String name);
	}

	public enum XCFileType {
		ABSOLUTE, BUILT_PRODUCT, BUILD_SETTING
	}

	public static XCFileReference absoluteFile(String path) {
		return new AbsoluteFileReference(Objects.requireNonNull(path));
	}

	public static XCFileReference builtProduct(String path) {
		return new BuiltProductReference(Objects.requireNonNull(path));
	}

	public static XCFileReference fromBuildSetting(String buildSetting, String path) {
		return new BuildSettingFileReference(Objects.requireNonNull(buildSetting), Objects.requireNonNull(path));
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class AbsoluteFileReference extends XCFileReference {
		private final Path path;

		private AbsoluteFileReference(String path) {
			this.path = new File(path).toPath();
		}

		@Override
		public Path resolve(ResolveContext context) {
			return path;
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
		private final String path;

		private BuiltProductReference(String path) {
			this.path = path;
		}

		@Override
		public Path resolve(ResolveContext context) {
			return context.getBuiltProductDirectory().resolve(path);
		}

		@Override
		public XCFileType getType() {
			return XCFileType.BUILT_PRODUCT;
		}

		@Override
		public String toString() {
			return "$(BUILT_PRODUCT_DIR)/" + path;
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class BuildSettingFileReference extends XCFileReference {
		private String buildSetting;
		private final String path;

		private BuildSettingFileReference(String buildSetting, String path) {
			this.buildSetting = buildSetting;
			this.path = path;
		}

		@Override
		public Path resolve(ResolveContext context) {
			return context.get(buildSetting).resolve(path);
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
