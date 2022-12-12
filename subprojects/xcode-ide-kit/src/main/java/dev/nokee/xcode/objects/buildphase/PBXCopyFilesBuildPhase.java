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

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;
import static org.apache.commons.io.FilenameUtils.concat;

public interface PBXCopyFilesBuildPhase extends PBXBuildPhase {
	enum SubFolder {
		AbsolutePath(0),
		ProductsDirectory(16),
		Wrapper(1),
		Executables(6),
		Resources(7),
		JavaResources(15),
		Frameworks(10),
		SharedFrameworks(11),
		SharedSupport(12),
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

	String getDstPath();

	SubFolder getDstSubfolderSpec();

	static Builder builder() {
		return new Builder();
	}

	final class Builder implements org.apache.commons.lang3.builder.Builder<PBXCopyFilesBuildPhase>, BuildFileAwareBuilder<Builder> {
		private final List<PBXBuildFile> files = new ArrayList<>();
		private SubFolder dstSubfolderSpec;
		private String dstPath;

		@Override
		public Builder file(PBXBuildFile file) {
			files.add(Objects.requireNonNull(file, "'file' must not be null"));
			return this;
		}

		@Override
		public Builder files(Iterable<? extends PBXBuildFile> files) {
			this.files.clear();
			stream(files).map(Objects::requireNonNull).forEach(this.files::add);
			return this;
		}

		public Builder dstPath(String dstPath) {
			this.dstPath = Objects.requireNonNull(dstPath);
			return this;
		}

		public Builder dstSubfolderSpec(SubFolder dstSubfolderSpec) {
			this.dstSubfolderSpec = Objects.requireNonNull(dstSubfolderSpec);
			return this;
		}

		public Builder destination(Consumer<? super DestinationBuilder> builderConsumer) {
			builderConsumer.accept(new DestinationBuilder());
			return this;
		}

		@Override
		public PBXCopyFilesBuildPhase build() {
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "PBXCopyFilesBuildPhase");
			builder.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, ImmutableList.copyOf(files));
			builder.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, Objects.requireNonNull(dstPath, "'dstPath' must not be null"));
			builder.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, Objects.requireNonNull(dstSubfolderSpec, "'dstSubfolderSpec' must not be null"));

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
