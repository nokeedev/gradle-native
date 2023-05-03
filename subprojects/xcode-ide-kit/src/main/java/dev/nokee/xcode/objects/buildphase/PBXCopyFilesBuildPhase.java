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
package dev.nokee.xcode.objects.buildphase;

import dev.nokee.xcode.objects.LenientAwareBuilder;
import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;
import static org.apache.commons.io.FilenameUtils.concat;

public interface PBXCopyFilesBuildPhase extends PBXBuildPhase {
	enum SubFolder {
		AbsolutePath(0),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)}.
		 */
		ProductsDirectory(16),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(WRAPPER_NAME)}.
		 */
		Wrapper(1),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(EXECUTABLE_FOLDER_PATH)}.
		 */
		Executables(6),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(UNLOCALIZED_RESOURCES_FOLDER_PATH)}.
		 */
		Resources(7),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(JAVA_FOLDER_PATH)}.
		 */
		JavaResources(15),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(FRAMEWORKS_FOLDER_PATH)}.
		 */
		Frameworks(10),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(SHARED_FRAMEWORKS_FOLDER_PATH)}.
		 */
		SharedFrameworks(11),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(SHARED_SUPPORT_FOLDER_PATH)}.
		 */
		SharedSupport(12),

		/**
		 * Based on {@code $(BUILT_PRODUCTS_DIR)/$(PLUGINS_FOLDER_PATH)}.
		 */
		PlugIns(13)
		;

		private final int value;

		SubFolder(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static SubFolder valueOf(int value) {
			for (SubFolder candidate : values()) {
				if (candidate.value == value) {
					return candidate;
				}
			}
			throw new IllegalArgumentException(String.format("value '%d' is not known", value));
		}
	}

	Optional<String> getName();

	String getDstPath();

	SubFolder getDstSubfolderSpec();

	@Override
	Builder toBuilder();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXCopyFilesBuildPhase>, BuildFileAwareBuilder<Builder>, LenientAwareBuilder<Builder>, PBXBuildPhase.Builder {
		private final DefaultKeyedObject.Builder builder;

		public Builder() {
			this(new DefaultKeyedObject.Builder().knownKeys(KeyedCoders.ISA).knownKeys(CodeablePBXCopyFilesBuildPhase.CodingKeys.values()));
		}

		public Builder(KeyedObject parent) {
			this(new DefaultKeyedObject.Builder().parent(parent));
		}

		private Builder(DefaultKeyedObject.Builder builder) {
			this.builder = builder;
			builder.put(KeyedCoders.ISA, "PBXCopyFilesBuildPhase");
			builder.requires(key(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath));
			builder.requires(key(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec));
		}

		@Override
		public Builder lenient() {
			builder.lenient();
			return this;
		}

		@Override
		public Builder file(PBXBuildFile file) {
			builder.add(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, file);
			return this;
		}

		@Override
		public Builder files(Iterable<? extends PBXBuildFile> files) {
			builder.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, files);
			return this;
		}

		public Builder dstPath(String dstPath) {
			builder.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, dstPath);
			return this;
		}

		public Builder dstSubfolderSpec(SubFolder dstSubfolderSpec) {
			builder.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, dstSubfolderSpec);
			return this;
		}

		public Builder destination(Consumer<? super DestinationBuilder> builderConsumer) {
			builderConsumer.accept(new DestinationBuilder());
			return this;
		}

		@Override
		public PBXCopyFilesBuildPhase build() {
			return new CodeablePBXCopyFilesBuildPhase(builder.build());
		}

		public final class DestinationBuilder {
			public void absolutePath(Path path) {
				dstSubfolderSpec(SubFolder.AbsolutePath).dstPath(path.toAbsolutePath().toString());
			}

			public void productsDirectory(String subpath) {
				dstSubfolderSpec(SubFolder.ProductsDirectory).dstPath(subpath);
			}

			public void wrapper(String subpath) {
				dstSubfolderSpec(SubFolder.Wrapper).dstPath(subpath);
			}

			public void executables(String subpath) {
				dstSubfolderSpec(SubFolder.Executables).dstPath(subpath);
			}

			public void resources(String subpath) {
				dstSubfolderSpec(SubFolder.Resources).dstPath(subpath);
			}

			public void javaResources(String subpath) {
				dstSubfolderSpec(SubFolder.JavaResources).dstPath(subpath);
			}

			public void frameworks(String subpath) {
				dstSubfolderSpec(SubFolder.Frameworks).dstPath(subpath);
			}

			public void sharedFrameworks(String subpath) {
				dstSubfolderSpec(SubFolder.SharedFrameworks).dstPath(subpath);
			}

			public void sharedSupport(String subpath) {
				dstSubfolderSpec(SubFolder.SharedSupport).dstPath(subpath);
			}

			public void plugIns(String subpath) {
				dstSubfolderSpec(SubFolder.PlugIns).dstPath(subpath);
			}

			public void xpcServices(String subpath) {
				dstSubfolderSpec(SubFolder.ProductsDirectory).dstPath(concat("$(CONTENTS_FOLDER_PATH)/XPCServices", subpath));
			}

			public void systemExtensions(String subpath) {
				dstSubfolderSpec(SubFolder.ProductsDirectory).dstPath(concat("$(SYSTEM_EXTENSIONS_FOLDER_PATH)", subpath));
			}

			public void appClips(String subpath) {
				dstSubfolderSpec(SubFolder.ProductsDirectory).dstPath(concat("$(CONTENTS_FOLDER_PATH)/AppClips", subpath));
			}
		}
	}
}
