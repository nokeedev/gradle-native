package dev.nokee.publish.bintray.internal;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.concurrent.Callable;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Iterables.getOnlyElement;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.evaluate;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.publish.bintray.internal.BintrayTestUtils.*;
import static dev.nokee.utils.DeferredUtils.realize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(BintrayPublishPlugin.class)
class BintrayPublishPluginTest {
	private final Project project = rootProject();

	@BeforeEach
	void applyPluginAndCreatePublication() {
		applyBintrayPublishPlugin(project);
		publishingOf(project).publications(createMavenPublication());
	}

	@Test
	void appliesMavenPublishPlugin() {
		assertThat(project.getPluginManager().hasPlugin("maven-publish"), equalTo(true));
	}

	private static PublishingExtension publishingOf(Project project) {
		return project.getExtensions().getByType(PublishingExtension.class);
	}

	@Test
	void doesNotExtendsPublishingToOtherRepositories() {
		publishingOf(project).getRepositories().maven(withUrl("https://example.com/")::accept);

		evaluate(project); // TODO: REMOVE

		assertThat(getOnlyElement(project.getTasks().withType(PublishToMavenRepository.class)).getActions(), iterableWithSize(1));
	}

	@Test
	void extendsPublishingToBintray() {
		publishingOf(project).getRepositories().maven(forBintray()::accept);

		evaluate(project); // TODO: REMOVE

		assertThat(getOnlyElement(project.getTasks().withType(PublishToMavenRepository.class)).getDependsOn(), hasItem(isA(Callable.class)));
	}

	@Test
	void doesNotThrowExceptionDuringConfigurationForMissingBintrayPackageName() {
		publishingOf(project).getRepositories().maven(forBintray().andThen(withoutPackageName())::accept);

		assertDoesNotThrow(() -> {
			evaluate(project);
			realize(project.getTasks().withType(PublishToMavenRepository.class));
		});
	}

	@Test
	void doesNotThrowExceptionDuringConfigurationForMissingBintrayCredentials() {
		publishingOf(project).getRepositories().maven(forBintray().andThen(withoutCredentials())::accept);

		assertDoesNotThrow(() -> {
			evaluate(project);
			realize(project.getTasks().withType(PublishToMavenRepository.class));
		});
	}

	@Test
	void doesNotThrowExceptionWhenConfiguringPublishingTaskForFileRepository() {
		publishingOf(project).getRepositories().maven(withUrl(project.getLayout().getBuildDirectory().dir("repositories/staging"))::accept);

		assertDoesNotThrow(() -> {
			evaluate(project);
			realize(project.getTasks().withType(PublishToMavenRepository.class));
		});
	}

	private static Action<PublicationContainer> createMavenPublication() {
		return publications -> publications.create("test", MavenPublication.class, publication -> {
			publication.setGroupId("com.examples");
			publication.setArtifactId("foo");
			publication.setVersion("4.2");
		});
	}

	private static void applyBintrayPublishPlugin(Project project) {
		project.apply(of("plugin", "dev.nokee.experimental.bintray-publish"));
	}
}
