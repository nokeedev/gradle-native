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

import com.google.common.collect.Iterables;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.val;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.utils.Optionals.stream;

public final class XCTargetLoader implements XCLoader<XCTarget, XCTargetReference> {
	private final XCLoader<Set<XCDependency>, XCTargetReference> dependenciesLoader;
	private final XCLoader<PBXProject, XCProjectReference> pbxLoader;
	private final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;

	public XCTargetLoader(XCLoader<PBXProject, XCProjectReference> pbxLoader, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader) {
		this.pbxLoader = pbxLoader;
		this.fileReferencesLoader = fileReferencesLoader;
		this.dependenciesLoader = new XCCacheLoader<>(new XCDependenciesLoader(XCLoaders.pbxtargetLoader(), fileReferencesLoader));
	}

	@Override
	public XCTarget load(XCTargetReference reference) {
		val proj = pbxLoader.load(reference.getProject());

		val target = Objects.requireNonNull(Iterables.find(proj.getTargets(), it -> it.getName().equals(reference.getName())));

		val resolver = fileReferencesLoader.load(reference.getProject());//p.getFileReferences();//walk(proj);

		// Assuming PBXFileReference only
		val inputFiles = findInputFiles(target).map(resolver::get).collect(Collectors.toList());

		// TODO: outputFile here is misleading, it's more about the productFile
		// TODO: PBXAggregateTarget has no productFile
		val outputFile = target.getProductReference().map(resolver::get).orElse(null);

		return new DefaultXCTarget(reference.getName(), reference.getProject(), inputFiles, outputFile, dependenciesLoader);
	}

	public static Stream<PBXFileReference> findInputFiles(PBXTarget target) {
		// TODO: Support Swift package (aka getProductRef() vs getFileRef())
		return target.getBuildPhases().stream().flatMap(it -> it.getFiles().stream()).flatMap(it -> stream(it.getFileRef())).flatMap(it -> {
			if (it instanceof PBXFileReference) {
				return Stream.of((PBXFileReference) it);
			} else {
				// TODO: Support PBXReferenceProxy (cross-project reference)
				return Stream.empty();
			}
		});
	}
}
