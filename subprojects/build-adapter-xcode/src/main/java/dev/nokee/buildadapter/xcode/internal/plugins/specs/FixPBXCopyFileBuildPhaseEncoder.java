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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.ValueEncoder;
import dev.nokee.xcode.project.coders.CoderType;
import dev.nokee.xcode.project.coders.Select;
import lombok.val;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.AbsolutePath;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.Executables;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.Frameworks;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.JavaResources;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.PlugIns;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.ProductsDirectory;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.Resources;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.SharedFrameworks;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.SharedSupport;
import static dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase.SubFolder.Wrapper;

public final class FixPBXCopyFileBuildPhaseEncoder<IN extends PBXBuildPhase & Encodeable> implements ValueEncoder<Encodeable, IN> {
	@Override
	public Encodeable encode(IN value, Context context) {
		if (value instanceof PBXCopyFilesBuildPhase) {
			final PBXCopyFilesBuildPhase buildPhase = (PBXCopyFilesBuildPhase) value;
			val r = DES.select(buildPhase);
			return DefaultKeyedObject.builder().parent(((CodeablePBXCopyFilesBuildPhase) value).delegate)
				.put(XCBuildSpecCodingKeyCoders.DESTINATION, r)
				.build();
		} else {
			return value;
		}
	}

	private static final Select<PBXCopyFilesBuildPhase, List<PBXFileReference>> DES = Select.<PBXCopyFilesBuildPhase, PBXCopyFilesBuildPhase.SubFolder, List<PBXFileReference>>newInstance(PBXCopyFilesBuildPhase::getDstSubfolderSpec)
		.forCase(PlugIns, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(PLUGINS_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(Wrapper, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(WRAPPER_NAME)", dstPath, path).toString())))
		.forCase(Resources, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(UNLOCALIZED_RESOURCES_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(Frameworks, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(FRAMEWORKS_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(Executables, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(EXECUTABLE_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(JavaResources, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(JAVA_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(SharedSupport, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(SHARED_SUPPORT_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(SharedFrameworks, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get("$(SHARED_FRAMEWORKS_FOLDER_PATH)", dstPath, path).toString())))
		.forCase(ProductsDirectory, outputFiles((dstPath, path) -> PBXFileReference.ofBuiltProductsDir(Paths.get(dstPath, path).toString())))
		.forCase(AbsolutePath, outputFiles((dstPath, path) -> PBXFileReference.ofAbsolutePath(Paths.get(dstPath, path).toString())))
		;

	private static Function<PBXCopyFilesBuildPhase, List<PBXFileReference>> outputFiles(BiFunction<String, String, PBXFileReference> factory) {
		return buildPhase -> {
			ImmutableList.Builder<PBXFileReference> builder = ImmutableList.builder();
			for (PBXBuildFile file : buildPhase.getFiles()) {
				assert file.getFileRef().isPresent();
				builder.add(factory.apply(buildPhase.getDstPath(), ((PBXFileReference) file.getFileRef().get()).getPath().get()));
			}
			return builder.build();
		};
	}

	@Override
	public CoderType<?> getEncodeType() {
		return CoderType.of(PBXBuildPhase.class);
	}
}
