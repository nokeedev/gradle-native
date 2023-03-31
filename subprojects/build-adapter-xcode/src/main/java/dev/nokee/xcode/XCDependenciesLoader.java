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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableSet;
import dev.nokee.utils.Optionals;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.targets.PBXTarget;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public final class XCDependenciesLoader implements XCLoader<Set<XCDependency>, XCTargetReference> {
	private final XCLoader<PBXTarget, XCTargetReference> targetLoader;
	private final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;
	private final XCDependencyFactory dependencyFactory;

	public XCDependenciesLoader(XCLoader<PBXTarget, XCTargetReference> targetLoader, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader) {
		this(targetLoader, fileReferencesLoader, new XCDependencyFactory() {
			@Nullable
			@Override
			public XCDependency create(XCFileReference reference) {
				throw new UnsupportedOperationException();
			}

			@Nullable
			@Override
			public XCDependency create(XCTargetReference reference) {
				throw new UnsupportedOperationException();
			}
		});
	}

	public XCDependenciesLoader(XCLoader<PBXTarget, XCTargetReference> targetLoader, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader, XCDependencyFactory dependencyFactory) {
		this.targetLoader = targetLoader;
		this.fileReferencesLoader = fileReferencesLoader;
		this.dependencyFactory = dependencyFactory;
	}

	public interface XCDependencyFactory {
		@Nullable
		XCDependency create(XCFileReference reference);

		@Nullable
		XCDependency create(XCTargetReference reference);
	}

	@Override
	public Set<XCDependency> load(XCTargetReference reference) {
		PBXTarget target = targetLoader.load(reference);

		return Stream.concat( //
			target.getBuildPhases().stream() //
				.flatMap(it -> it.getFiles().stream()) //
				.flatMap(it -> Optionals.stream(it.getFileRef())) //
				.map(it -> fileReferencesLoader.load(reference.getProject()).get((PBXReference) it)) //
				.map(dependencyFactory::create), //
			target.getDependencies().stream() //
				.map(it -> it.getTarget().map(t -> toTargetReference(reference.getProject(), t)).orElseGet(() -> toTargetReference(reference.getProject(), it.getTargetProxy()))) //
				.map(dependencyFactory::create) //
			).filter(Objects::nonNull).collect(ImmutableSet.toImmutableSet());
	}

	private XCTargetReference toTargetReference(XCProjectReference project, PBXTarget target) {
		return new DefaultXCTargetReference(project, target.getName());
	}

	private XCTargetReference toTargetReference(XCProjectReference project, PBXContainerItemProxy targetProxy) {
		checkArgument(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE.equals(targetProxy.getProxyType()), "'targetProxy' is expected to be a target reference");

		if (targetProxy.getContainerPortal() instanceof PBXProject) {
			return new DefaultXCTargetReference(project, targetProxy.getRemoteInfo()
				.orElseThrow(XCDependenciesLoader::missingRemoteInfoException));
		} else if (targetProxy.getContainerPortal() instanceof PBXFileReference) {
			return new DefaultXCTargetReference(new DefaultXCProjectReference(project.load(fileReferencesLoader).get((PBXFileReference) targetProxy.getContainerPortal()).resolve(new XCFileReference.ResolveContext() {
				@Override
				public Path getBuiltProductsDirectory() {
					throw new UnsupportedOperationException("Should not call");
				}

				@Override
				public Path get(String name) {
					if ("SOURCE_ROOT".equals(name)) {
						return project.getLocation().getParent();
					}
					throw new UnsupportedOperationException(String.format("Could not resolve '%s' build setting.", name));
				}
			})), targetProxy.getRemoteInfo().orElseThrow(XCDependenciesLoader::missingRemoteInfoException));
		} else {
			throw new UnsupportedOperationException("Unknown container portal.");
		}
	}

	private static RuntimeException missingRemoteInfoException() {
		return new RuntimeException("Missing 'remoteInfo' on 'targetProxy'.");
	}
}
