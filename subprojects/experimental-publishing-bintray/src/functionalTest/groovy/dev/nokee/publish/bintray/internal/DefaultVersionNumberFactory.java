package dev.nokee.publish.bintray.internal;

import java.net.MalformedURLException;
import java.net.URL;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public class DefaultVersionNumberFactory {
	public AutoIncrementingVersionNumberFromMavenArtifact autoIncrementVersionFromMavenArtifactPublishedToBintray(String repositorySlug, String groupId, String artifactId) {
		try {
			return new AutoIncrementingVersionNumberFromMavenArtifact(new URL("https://dl.bintray.com/" + repositorySlug + "/" + groupId.replace(".", "/") + "/" + artifactId + "/maven-metadata.xml"));
		} catch (MalformedURLException e) {
			return rethrow(e);
		}
	}
}
