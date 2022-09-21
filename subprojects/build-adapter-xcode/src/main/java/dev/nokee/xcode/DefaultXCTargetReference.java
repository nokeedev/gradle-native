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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.utils.Optionals.stream;

@EqualsAndHashCode
public final class DefaultXCTargetReference implements XCTargetReference, Serializable {
	private final XCProjectReference project;
	private final String name;

	public DefaultXCTargetReference(XCProjectReference project, String name) {
		this.project = project;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public XCProjectReference getProject() {
		return project;
	}

	public XCTarget load() {
		return XCCache.cacheIfAbsent(this, key -> {
			val p = project.load();
			val proj = p.getModel();

			val target = Objects.requireNonNull(Iterables.find(proj.getTargets(), it -> it.getName().equals(name)));

			val resolver = p.getFileReferences();//walk(proj);

			// Assuming PBXFileReference only
			val inputFiles = findInputFiles(target).map(resolver::get).collect(Collectors.toList());

			// TODO: outputFile here is misleading, it's more about the productFile
			// TODO: PBXAggregateTarget has no productFile
			val outputFile = target.getProductReference().map(resolver::get).orElse(null);
			// TODO: Handle cross-project reference
			val dependencies = target.getDependencies().stream()
				.map(it -> it.getTarget().map(this::toTargetReference).orElseGet(() -> toTargetReference(it.getTargetProxy())))
				.collect(ImmutableList.toImmutableList());

			return new XCTarget(name, project, inputFiles, dependencies, outputFile);
		});
	}

	private XCTargetReference toTargetReference(PBXTarget target) {
		return new DefaultXCTargetReference(project, target.getName());
	}

	private XCTargetReference toTargetReference(PBXContainerItemProxy targetProxy) {
		checkArgument(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE.equals(targetProxy.getProxyType()), "'targetProxy' is expected to be a target reference");

		if (targetProxy.getContainerPortal() instanceof PBXProject) {
			return new DefaultXCTargetReference(project, targetProxy.getRemoteInfo()
				.orElseThrow(DefaultXCTargetReference::missingRemoteInfoException));
		} else if (targetProxy.getContainerPortal() instanceof PBXFileReference) {
			return new DefaultXCTargetReference(new DefaultXCProjectReference(project.load().getFileReferences().get((PBXFileReference) targetProxy.getContainerPortal()).resolve(new XCFileReference.ResolveContext() {
				@Override
				public Path getBuiltProductDirectory() {
					throw new UnsupportedOperationException("Should not call");
				}

				@Override
				public Path get(String name) {
					if ("SOURCE_ROOT".equals(name)) {
						return project.getLocation().getParent();
					}
					throw new UnsupportedOperationException(String.format("Could not resolve '%s' build setting.", name));
				}
			})), targetProxy.getRemoteInfo().orElseThrow(DefaultXCTargetReference::missingRemoteInfoException));
		} else {
			throw new UnsupportedOperationException("Unknown container portal.");
		}
	}

	private static RuntimeException missingRemoteInfoException() {
		return new RuntimeException("Missing 'remoteInfo' on 'targetProxy'.");
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
