package dev.nokee.publish.bintray.internal;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.utils.TestUtils.rootProject;

public class BintrayTestUtils {
	private static final Consumer<MavenArtifactRepository> DEFAULT_ACTION = withName("test").andThen(withUrl(defaultUrl())).andThen(withPackageName(defaultPackageName())).andThen(withCredentials(defaultUsername(), defaultPassword()));

	public static URI defaultUrl() {
		return bintrayUrl("nokeedev/examples");
	}

	public static String defaultPackageName() {
		return "test";
	}

	public static String defaultUsername() {
		return "test";
	}

	public static String defaultPassword() {
		return "test";
	}

	public static MavenArtifactRepository repository() {
		return rootProject().getRepositories().maven(DEFAULT_ACTION::accept);
	}

	public static MavenArtifactRepository repository(Consumer<? super MavenArtifactRepository> action) {
		return rootProject().getRepositories().maven(DEFAULT_ACTION.andThen(action)::accept);
	}

	public static Consumer<MavenArtifactRepository> forBintray() {
		return DEFAULT_ACTION;
	}

	public static Consumer<MavenArtifactRepository> withoutPackageName() {
		return withPackageName(null);
	}

	public static Consumer<MavenArtifactRepository> withoutCredentials() {
		return withCredentials(null, null);
	}

	private static Consumer<MavenArtifactRepository> withName(String name) {
		return repository -> repository.setName(name);
	}

	public static Consumer<MavenArtifactRepository> withUrl(Object url) {
		return repository -> repository.setUrl(url);
	}

	public static URI bintrayUrl(String repositorySlug) {
		try {
			return new URI("https://dl.bintray.com/" + repositorySlug);
		} catch (URISyntaxException e) {
			return ExceptionUtils.rethrow(e);
		}
	}

	public static Consumer<MavenArtifactRepository> withPackageName(String packageName) {
		return repository -> {
			((ExtensionAware) repository).getExtensions().getByType(ExtraPropertiesExtension.class).set("packageName", packageName);
		};
	}

	public static Consumer<MavenArtifactRepository> withCredentials(String username, String password) {
		return repository -> {
			repository.credentials(credentials -> {
				credentials.setUsername(username);
				credentials.setPassword(password);
			});
		};
	}
}
