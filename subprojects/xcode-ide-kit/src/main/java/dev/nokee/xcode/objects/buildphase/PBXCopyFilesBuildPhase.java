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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;
import static org.apache.commons.io.FilenameUtils.concat;

public final class PBXCopyFilesBuildPhase extends PBXBuildPhase {
	private final String dstPath;
	private final SubFolder dstSubfolderSpec;

	public enum SubFolder {
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
	}

	private PBXCopyFilesBuildPhase(ImmutableList<PBXBuildFile> files, String dstPath, SubFolder dstSubfolderSpec) {
		super(files);
		this.dstPath = dstPath;
		this.dstSubfolderSpec = dstSubfolderSpec;
	}

	public String getDstPath() {
		return dstPath;
	}

	public SubFolder getDstSubfolderSpec() {
		return dstSubfolderSpec;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final List<PBXBuildFile> files = new ArrayList<>();
		private SubFolder dstSubfolderSpec;
		private String dstPath;

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

		public PBXCopyFilesBuildPhase build() {
			return new PBXCopyFilesBuildPhase(ImmutableList.copyOf(files), Objects.requireNonNull(dstPath, "'dstPath' must not be null"), Objects.requireNonNull(dstSubfolderSpec, "'dstSubfolderSpec' must not be null"));
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
