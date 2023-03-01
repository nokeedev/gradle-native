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
package dev.nokee.buildadapter.xcode.internal.files;

import dev.nokee.utils.DeferredUtils;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RelativePath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public final class PreserveLastModifiedFileSystemOperation implements FileSystemOperation {
	private final FileSystemOperation delegate;

	public PreserveLastModifiedFileSystemOperation(FileSystemOperation delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(Action<? super CopySpec> action) {
		val synchedFiles = new HashSet<CopiedFile>();
		delegate.execute(spec -> {
			val newSpec = new DelegateCopySpec(spec);
			action.execute(newSpec);

			final Path destPath = unpackToPath(newSpec.getDestPath());

			spec.eachFile(details -> {
				RelativePath path = details.getRelativePath();
				do {
					synchedFiles.add(new CopiedFile(details.getFile().toPath(), destPath.resolve(path.getPathString())));
				} while ((path = path.getParent()) != null);
			});
		});

		synchedFiles.forEach(CopiedFile::copyLastModifiedTime);
	}

	private static Path unpackToPath(Object path) {
		return DeferredUtils.<Path>unpack(it -> {
			val t = DeferredUtils.unpack(it);
			if (t instanceof FileSystemLocation) {
				return ((FileSystemLocation) t).getAsFile().toPath();
			} else if (t instanceof File) {
				return ((File) t).toPath();
			} else {
				return t;
			}
		}).until(Path.class).execute(path);
	}

	@EqualsAndHashCode
	private static final class CopiedFile {
		private final Path sourcePath;
		private final Path destinationPath;

		private CopiedFile(Path sourcePath, Path destinationPath) {
			this.sourcePath = sourcePath;
			this.destinationPath = destinationPath;
		}

		public void copyLastModifiedTime() {
			if (Files.exists(destinationPath)) {
				try {
					Files.setLastModifiedTime(destinationPath, Files.getLastModifiedTime(sourcePath));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
