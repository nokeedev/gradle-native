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

import com.google.common.collect.ImmutableMap;
import dev.nokee.utils.Optionals;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXReferenceProxy;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.EqualsAndHashCode;
import lombok.val;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static dev.nokee.xcode.objects.files.PBXSourceTree.ABSOLUTE;
import static dev.nokee.xcode.objects.files.PBXSourceTree.BUILT_PRODUCTS_DIR;
import static dev.nokee.xcode.objects.files.PBXSourceTree.GROUP;
import static dev.nokee.xcode.objects.files.PBXSourceTree.SOURCE_ROOT;

@EqualsAndHashCode
public final class XCFileReferencesLoader implements XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference>, Serializable {
	private final XCLoader<PBXProject, XCProjectReference> loader;

	public XCFileReferencesLoader(XCLoader<PBXProject, XCProjectReference> loader) {
		this.loader = loader;
	}

	@Override
	public XCFileReferences load(XCProjectReference reference) {
		return walk(loader.load(reference), reference);
	}

	public static XCFileReferences walk(PBXProject project, XCProjectReference reference) {
		val builder = XCFileReferences.builder();
		builder.ref(reference);
		walk(builder, new FileNode(SOURCE_ROOT, null, null), project.getMainGroup());
		return builder.build();
	}

	private static void walk(XCFileReferences.Builder builder, FileNode previousNodes, PBXGroup group) {
		val node = new FileNode(group.getSourceTree(), previousNodes, group.getPath().orElse(null));
		for (GroupChild child : group.getChildren()) {
			// TODO: Should have some support for PBXVariantGroup or XCVersionGroup
			if (child instanceof PBXGroup) {
				walk(builder, node, (PBXGroup) child);
			} else if (child instanceof PBXReference) {
				walk(builder, node, (PBXReference) child);
			}
		}
	}

	private static void walk(XCFileReferences.Builder builder, FileNode previousNodes, PBXReference fileRef) {
		builder.put(fileRef, parse(new FileNode(fileRef.getSourceTree(), previousNodes, pathOf(fileRef))));
	}

	@Nullable
	private static String pathOf(PBXReference fileRef) {
		// FIXME: Clear up the rules to rebuild the full file reference path.
		//   This is a bit messy. Xcode seems to sometime take the path while other time use the name...
		return Optionals.or(fileRef.getPath().map(path -> fileRef.getName().filter(name -> !path.contains("/") && !path.contains(".")).orElse(path)), fileRef::getName).orElse(null);
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

	private static final class FileNode {
		private final PBXSourceTree sourceTree;
		@Nullable
		private final FileNode previous;
		@Nullable private final String path;

		private FileNode(PBXSourceTree sourceTree, @Nullable FileNode previous, @Nullable String path) {
			this.sourceTree = sourceTree;
			this.previous = previous;
			this.path = path;
		}
	}

	public static final class XCFileReferences {
		private final Map<PBXReference, XCFileReference> fileRefs;
		private final XCProjectReference reference;

		public XCFileReferences(Map<PBXReference, XCFileReference> fileRefs, XCProjectReference reference) {
			this.fileRefs = fileRefs;
			this.reference = reference;
		}

		public XCFileReference get(PBXReference fileRef) {
			// FIXME: When PBXFileReference comes from two different instance of the same project, they don't align.
			//   We should find a way to normalize the file reference so they can be compared between project instance
			if (fileRef instanceof PBXReferenceProxy) {
				if (((PBXReferenceProxy) fileRef).getRemoteReference().getContainerPortal() instanceof PBXFileReference) {
					val builder = PBXFileReference.builder();
					fileRef.getName().ifPresent(builder::name);
					fileRef.getPath().ifPresent(builder::path);
					builder.sourceTree(fileRef.getSourceTree());

					return XCLoaders.fileReferences().load(XCProjectReference.of(get((PBXReference) ((PBXReferenceProxy) fileRef).getRemoteReference().getContainerPortal()).resolve(new XCFileReference.ResolveContext() {
						@Override
						public Path getBuiltProductsDirectory() {
							throw new UnsupportedOperationException();
						}

						@Override
						public Path get(String name) {
							if (name.equals("SOURCE_ROOT")) {
								return reference.getLocation().getParent();
							}
							throw new UnsupportedOperationException(name);
						}
					}))).get(builder.build());
				}
			}

			if (fileRef instanceof PBXFileReference) {
				if (fileRef.getSourceTree().equals(ABSOLUTE)) {
					return XCFileReference.absoluteFile(pathOf(fileRef));
				} else if (fileRef.getSourceTree().equals(BUILT_PRODUCTS_DIR)) {
					return XCFileReference.builtProduct(pathOf(fileRef));
				} else if (!fileRef.getSourceTree().equals(GROUP)) {
					return XCFileReference.fromBuildSetting(fileRef.getSourceTree().toString(), pathOf(fileRef));
				}
			}
			return Objects.requireNonNull(fileRefs.get(fileRef), () -> "no reference for '" + fileRef + "'");
		}

		public static Builder builder() {
			return new Builder();
		}

		public static final class Builder {
			private final ImmutableMap.Builder<PBXReference, XCFileReference> fileRefs = ImmutableMap.builder();
			private XCProjectReference reference;

			public Builder put(PBXReference fileRef, XCFileReference file) {
				fileRefs.put(fileRef, file);
				return this;
			}

			public Builder ref(XCProjectReference reference) {
				this.reference = reference;
				return this;
			}

			public XCFileReferences build() {
				return new XCFileReferences(fileRefs.build(), reference);
			}
		}
	}
}
