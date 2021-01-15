package dev.nokee.publish.bintray.internal;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;

import java.net.URI;

public final class BintrayRepository {
	private final URI url;
	private final BintrayPackageName packageName;
	private final BintrayCredentials credentials;

	public BintrayRepository(URI url, BintrayPackageName packageName, BintrayCredentials credentials) {
		this.url = url;
		this.packageName = packageName;
		this.credentials = credentials;
	}

	@Nested
	public BintrayCredentials getCredentials() {
		return credentials;
	}

	@Nested
	public BintrayPackageName getPackageName() {
		return packageName;
	}

	@Input
	public URI getUrl() {
		return url;
	}
}
