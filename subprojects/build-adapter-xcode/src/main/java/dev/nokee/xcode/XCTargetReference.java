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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			val outputFile = target.getProductReference().map(resolver::get).orElseThrow(() -> new RuntimeException("for target " + target.getName() + " in project " + project.getLocation()));
			val dependencies = target.getDependencies().stream().map(it -> XCTargetReference.of(project, it.getTarget().getName())).collect(ImmutableList.toImmutableList());

			return new XCTarget(name, project, inputFiles, dependencies, outputFile);
		});
	}

	public static Stream<PBXFileReference> findInputFiles(PBXTarget target) {
		return target.getBuildPhases().stream().flatMap(it -> it.getFiles().stream()).map(it -> it.getFileRef()).flatMap(it -> {
			if (it instanceof PBXFileReference) {
				return Stream.of((PBXFileReference) it);
			} else {
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
		for (PBXReference child : group.getChildren()) {
			if (child instanceof PBXGroup) {
				walk(builder, node, (PBXGroup) child);
			} else if (child instanceof PBXFileReference) {
				walk(builder, node, (PBXFileReference) child);
			}
		}
	}

	private static void walk(XCFileReferences.Builder builder, FileNode previousNodes, PBXFileReference fileRef) {
		builder.put(fileRef, parse(new FileNode(fileRef.getSourceTree(), previousNodes, fileRef.getPath().orElse(null))));
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

			switch (node.sourceTree) {
				case GROUP: break; // continue looping
				case ABSOLUTE: return XCFileReference.absoluteFile(path);
				case BUILT_PRODUCTS_DIR: return XCFileReference.builtProduct(path);
				case SOURCE_ROOT: return XCFileReference.sourceRoot(path);
				case SDKROOT: return XCFileReference.sdkRoot(path);
				case DEVELOPER_DIR: return XCFileReference.developer(path);
				default: throw new UnsupportedOperationException("Source tree not supported! (" + node.sourceTree + ")");
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
