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
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProjReader;
import lombok.EqualsAndHashCode;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.xcode.objects.files.PBXSourceTree.ABSOLUTE;
import static dev.nokee.xcode.objects.files.PBXSourceTree.GROUP;

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

	public XCTarget load() {
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(project.getLocation().resolve("project.pbxproj"))))) {
			val pbxproj = reader.read();
			val proj = new PBXObjectUnarchiver().decode(pbxproj);

			val target = Objects.requireNonNull(Iterables.find(proj.getTargets(), it -> it.getName().equals(name)));

			val resolver = FileReferenceResolver.of(project.getLocation().getParent(), proj);

			// Assuming PBXFileReference only
			val inputFiles = findInputFiles(target).map(resolver::toPath).collect(Collectors.toList());

			return new XCTarget(inputFiles);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static Stream<PBXFileReference> findInputFiles(PBXTarget target) {
		return Stream.concat(target.getBuildPhases().stream().flatMap(it -> it.getFiles().stream()).map(it -> it.getFileRef()).flatMap(it -> {
			if (it instanceof PBXFileReference) {
				return Stream.of((PBXFileReference) it);
			} else {
				return Stream.empty();
			}
		}), target.getDependencies().stream().flatMap(it -> findInputFiles(it.getTarget())));
	}

	private static final class FileReferenceResolver {
		private final Map<PBXFileReference, Path> result;

		public FileReferenceResolver(HashMap<PBXFileReference, Path> result) {
			this.result = result;
		}

		public Path toPath(PBXFileReference fileRef) {
			return result.get(fileRef);
		}

		public static FileReferenceResolver of(Path path, PBXProject project) {
			val result = new HashMap<PBXFileReference, Path>();
			compute(result, new Context(path), project.getMainGroup());
			return new FileReferenceResolver(result);
		}

		private static void compute(Map<PBXFileReference, Path> paths, Context context, PBXGroup group) {
			group.getChildren().forEach(it -> {
				if (it.getSourceTree().equals(GROUP) || it.getSourceTree().equals(ABSOLUTE)) {
					if (it instanceof PBXGroup) {
						compute(paths, context.path((PBXGroup) it), (PBXGroup) it);
					} else if (it instanceof PBXFileReference) {
						paths.put((PBXFileReference) it, context.resolve((PBXFileReference) it));
					}
				}
			});
		}

		private static final class Context {
			private final Path path;

			private Context(Path path) {
				this.path = path;
			}

			public Path resolve(PBXFileReference fileRef) {
				switch (fileRef.getSourceTree()) {
					case GROUP:	return path.resolve(fileRef.getPath().orElseThrow(RuntimeException::new)).normalize();
					case ABSOLUTE: return new File(fileRef.getPath().orElseThrow(RuntimeException::new)).toPath();
					default: throw new UnsupportedOperationException();
				}
			}

			public Context path(PBXGroup group) {
				if (!group.getPath().isPresent()) {
					return this; // when no path, don't use name as it's just for the UI
				}

				switch (group.getSourceTree()) {
					case GROUP:	return new Context(path.resolve(group.getPath().get()));
					case ABSOLUTE: return new Context(new File(group.getPath().get()).toPath());
					default: throw new UnsupportedOperationException();
				}
			}
		}
	}

	public static XCTargetReference of(XCProjectReference project, String name) {
		return new XCTargetReference(project, name);
	}
}
