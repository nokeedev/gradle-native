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
