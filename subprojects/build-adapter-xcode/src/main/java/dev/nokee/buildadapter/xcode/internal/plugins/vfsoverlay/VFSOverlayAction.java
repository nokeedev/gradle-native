/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import com.google.common.collect.ImmutableList;
import dev.nokee.buildadapter.xcode.internal.plugins.BuildSettingsResolveContext;
import dev.nokee.util.provider.ZipProviderBuilder;
import dev.nokee.utils.Optionals;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCFileReference;
import dev.nokee.xcode.XCFileReferencesLoader;
import dev.nokee.xcode.XCLoaders;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXHeadersBuildPhase;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.ProductTypes;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class VFSOverlayAction implements Action<ConfigurableMapContainer<VFSOverlaySpec>> {
	private final ObjectFactory objects;
	private final Provider<XCTargetReference> targetReferenceProvider;
	private final Provider<XCBuildSettings> buildSettingsProvider;
	private final Provider<Path> derivedDataPathProvider;

	public VFSOverlayAction(ObjectFactory objects, Provider<XCTargetReference> targetReferenceProvider, Provider<XCBuildSettings> buildSettingsProvider, Provider<Path> derivedDataPathProvider) {
		this.objects = objects;
		this.targetReferenceProvider = targetReferenceProvider;
		this.buildSettingsProvider = buildSettingsProvider;
		this.derivedDataPathProvider = derivedDataPathProvider;
	}

	@Override
	public void execute(ConfigurableMapContainer<VFSOverlaySpec> specs) {
		specs.addAll(ZipProviderBuilder.newBuilder(objects)
			.value(targetReferenceProvider)
			.value(buildSettingsProvider)
			.value(derivedDataPathProvider)
			.zip(values -> combine(values.get(0), values.get(1), values.get(2))));
	}

	public Iterable<? extends VFSOverlaySpec> combine(XCTargetReference targetReference, XCBuildSettings buildSettings, Path derivedDataPath) {
		val context = new BuildSettingsResolveContext(FileSystems.getDefault(), buildSettings);
		val fileRefs = XCLoaders.fileReferences().load(targetReference.getProject());
		val target = XCLoaders.pbxtargetLoader().load(targetReference);


		val remapping = Stream.of(target)
			.filter(frameworkProducts())
			.flatMap(headersBuildPhases())
			.flatMap(publicHeaders())
			.flatMap(localFileReferences())
			.map(findReference(fileRefs))
			.map(resolveReference(context))
			.map(it -> newFileEntry(it.getFileName().toString(), it))
			.collect(ImmutableList.toImmutableList());

		if (!remapping.isEmpty()) {
			val frameworkProductHeaderDir = XCFileReference.builtProduct("$(PUBLIC_HEADERS_FOLDER_PATH)").resolve(context);

			val result = objects.newInstance(VFSOverlaySpec.class);
			result.getName().set(derivedDataPath.relativize(frameworkProductHeaderDir).toString());
			result.getEntries().addAll(remapping);
			return ImmutableList.of(result);
		} else {
			return ImmutableList.of();
		}
	}

	private VFSOverlaySpec.EntrySpec newFileEntry(String name, Path location) {
		val result = objects.newInstance(VFSOverlaySpec.EntrySpec.class);
		result.getName().set(name);
		result.getLocation().set(location.toString());
		return result;
	}

	private static Function<PBXReference, XCFileReference> findReference(XCFileReferencesLoader.XCFileReferences references) {
		return references::get;
	}

	private static Function<XCFileReference, Path> resolveReference(XCFileReference.ResolveContext context) {
		return it -> it.resolve(context);
	}

	private static Function<PBXTarget, Stream<PBXBuildPhase>> headersBuildPhases() {
		return target -> target.getBuildPhases().stream().filter(PBXHeadersBuildPhase.class::isInstance);
	}

	private static Function<PBXBuildPhase, Stream<PBXBuildFile>> publicHeaders() {
		return buildPhase -> buildPhase.getFiles().stream()
			.filter(buildFile -> {
				val attributes = buildFile.getSettings().get("ATTRIBUTES");
				if (attributes instanceof List) {
					return ((List<?>) attributes).contains("Public");
				}
				return false;
			});
	}

	private static Function<PBXBuildFile, Stream<PBXFileReference>> localFileReferences() {
		return buildFile -> Optionals.stream(buildFile.getFileRef()
			.filter(it -> it instanceof PBXFileReference)
			.map(PBXFileReference.class::cast));
	}

	private static Predicate<PBXTarget> frameworkProducts() {
		return target -> target instanceof PBXNativeTarget && target.getProductType().map(ProductTypes.FRAMEWORK::equals).orElse(false);
	}
}
