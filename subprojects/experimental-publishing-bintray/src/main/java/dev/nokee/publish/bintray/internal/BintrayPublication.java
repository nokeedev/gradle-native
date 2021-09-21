/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.publish.bintray.internal;

import com.google.common.collect.ImmutableList;
import lombok.val;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;

import java.util.List;

public final class BintrayPublication {
	private final StagingDirectory stagingDirectory;
	private final BintrayPublicationVersion version;

	public BintrayPublication(StagingDirectory stagingDirectory, BintrayPublicationVersion version) {
		this.stagingDirectory = stagingDirectory;
		this.version = version;
	}

	public List<BintrayArtifact> getArtifacts() {
		if (!stagingDirectory.exists()) {
			return ImmutableList.of();
		}

		val result = ImmutableList.<BintrayArtifact>builder();
		stagingDirectory.get().getAsFileTree().visit(new FileVisitor() {
			@Override
			public void visitDir(FileVisitDetails details) {}

			@Override
			public void visitFile(FileVisitDetails details) {
				if (!details.getName().startsWith("maven-metadata.xml") && !details.getName().endsWith(".md5") && !details.getName().endsWith(".sha256") && !details.getName().endsWith(".sha1") && !details.getName().endsWith(".sha512")) {
					val relativePath = details.getRelativePath().getPathString();
					result.add(new BintrayArtifact(details.getFile(), relativePath, version.get()));
				}
			}
		});
		return result.build();
	}
}
