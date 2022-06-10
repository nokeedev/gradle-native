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
		Path getSourceRoot();

		Path getBuiltProductDirectory();

		Path getSdkRoot();
	}

	public enum XCFileType {
		ABSOLUTE, SOURCE_ROOT, BUILT_PRODUCT, SDKROOT
	}

	public static XCFileReference absoluteFile(String path) {
		return new AbsoluteFileReference(Objects.requireNonNull(path));
	}

	public static XCFileReference sourceRoot(String path) {
		return new SourceRootFileReference(Objects.requireNonNull(path));
	}

	public static XCFileReference builtProduct(String path) {
		return new BuiltProductReference(Objects.requireNonNull(path));
	}

	public static XCFileReference sdkRoot(String path) {
		return new SdkRootFileReference(Objects.requireNonNull(path));
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
	private static final class SourceRootFileReference extends XCFileReference {
		private final String path;

		private SourceRootFileReference(String path) {
			this.path = path;
		}

		@Override
		public Path resolve(ResolveContext context) {
			return context.getSourceRoot().resolve(path);
		}

		@Override
		public XCFileType getType() {
			return XCFileType.SOURCE_ROOT;
		}

		@Override
		public String toString() {
			return "$(SOURCE_ROOT)/" + path;
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class SdkRootFileReference extends XCFileReference {
		private final String path;

		private SdkRootFileReference(String path) {
			this.path = path;
		}

		@Override
		public Path resolve(ResolveContext context) {
			return context.getSdkRoot().resolve(path);
		}

		@Override
		public XCFileType getType() {
			return XCFileType.SDKROOT;
		}

		@Override
		public String toString() {
			return "$(SDKROOT)/" + path;
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
}
