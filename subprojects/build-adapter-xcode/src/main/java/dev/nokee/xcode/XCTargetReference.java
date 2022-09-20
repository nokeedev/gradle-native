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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import dev.nokee.utils.Optionals;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.utils.Optionals.stream;
import static dev.nokee.xcode.objects.files.PBXSourceTree.ABSOLUTE;
import static dev.nokee.xcode.objects.files.PBXSourceTree.BUILT_PRODUCTS_DIR;
import static dev.nokee.xcode.objects.files.PBXSourceTree.GROUP;
import static dev.nokee.xcode.objects.files.PBXSourceTree.SOURCE_ROOT;

@EqualsAndHashCode
public final class XCTargetReference implements Serializable {
	private final XCProjectReference project;
	private final String name;

	private XCTargetReference(XCProjectReference project, String name) {
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
		return XCTargetReference.of(project, target.getName());
	}

	private XCTargetReference toTargetReference(PBXContainerItemProxy targetProxy) {
		checkArgument(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE.equals(targetProxy.getProxyType()), "'targetProxy' is expected to be a target reference");

		if (targetProxy.getContainerPortal() instanceof PBXProject) {
			return XCTargetReference.of(project, targetProxy.getRemoteInfo()
				.orElseThrow(XCTargetReference::missingRemoteInfoException));
		} else if (targetProxy.getContainerPortal() instanceof PBXFileReference) {
			return XCTargetReference.of(XCProjectReference.of(project.load().getFileReferences().get((PBXFileReference) targetProxy.getContainerPortal()).resolve(new XCFileReference.ResolveContext() {
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
			})), targetProxy.getRemoteInfo().orElseThrow(XCTargetReference::missingRemoteInfoException));
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

	private static final class FileNode {
		private final PBXSourceTree sourceTree;
		@Nullable private final FileNode previous;
		@Nullable private final String path;

		private FileNode(PBXSourceTree sourceTree, @Nullable FileNode previous, @Nullable String path) {
			this.sourceTree = sourceTree;
			this.previous = previous;
			this.path = path;
		}
	}

	public static XCFileReferences walk(PBXProject project) {
		val builder = XCFileReferences.builder();
		walk(builder, new FileNode(SOURCE_ROOT, null, null), project.getMainGroup());
		return builder.build();
	}

	private static void walk(XCFileReferences.Builder builder, FileNode previousNodes, PBXGroup group) {
		val node = new FileNode(group.getSourceTree(), previousNodes, group.getPath().orElse(null));
		for (GroupChild child : group.getChildren()) {
			// TODO: Should have some support for PBXVariantGroup or XCVersionGroup
			if (child instanceof PBXGroup) {
				walk(builder, node, (PBXGroup) child);
			} else if (child instanceof PBXFileReference) {
				walk(builder, node, (PBXFileReference) child);
			}
		}
	}

	private static void walk(XCFileReferences.Builder builder, FileNode previousNodes, PBXFileReference fileRef) {
		val realPath = Optionals.or(fileRef.getPath().map(path -> fileRef.getName().filter(name -> !path.contains("/")).orElse(path)), fileRef::getName).orElse(null);
		builder.put(fileRef, parse(new FileNode(fileRef.getSourceTree(), previousNodes, realPath)));
	}

	private static XCFileReference parse(FileNode node) {
		String path = null;
		do {
			if (node.path != null) {
				if (path == null) {
					path = node.path;
				} else {
					path = node.path + "/" + path;
				}
			}

			if (GROUP.equals(node.sourceTree)) {
				// continue looping
			} else if (ABSOLUTE.equals(node.sourceTree)) {
				return XCFileReference.absoluteFile(path);
			} else if (BUILT_PRODUCTS_DIR.equals(node.sourceTree)) {
				return XCFileReference.builtProduct(path);
			} else {
				return XCFileReference.fromBuildSetting(node.sourceTree.toString(), path);
			}
		} while ((node = node.previous) != null);

		throw new RuntimeException("Something went wrong.");
	}

	public static final class XCFileReferences {
		private final Map<Integer, XCFileReference> fileRefs;

		public XCFileReferences(Map<Integer, XCFileReference> fileRefs) {
			this.fileRefs = fileRefs;
		}

		public XCFileReference get(PBXFileReference fileRef) {
			return Objects.requireNonNull(fileRefs.get(System.identityHashCode(fileRef)));
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private final ImmutableMap.Builder<Integer, XCFileReference> fileRefs = ImmutableMap.builder();

			public Builder put(PBXFileReference fileRef, XCFileReference file) {
				fileRefs.put(System.identityHashCode(fileRef), file);
				return this;
			}

			public XCFileReferences build() {
				return new XCFileReferences(fileRefs.build());
			}
		}
	}

	public static XCTargetReference of(XCProjectReference project, String name) {
		return new XCTargetReference(project, name);
	}
}
