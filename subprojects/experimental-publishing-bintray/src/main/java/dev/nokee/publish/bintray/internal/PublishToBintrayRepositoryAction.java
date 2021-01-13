package dev.nokee.publish.bintray.internal;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.Task;

@RequiredArgsConstructor
public final class PublishToBintrayRepositoryAction implements Action<Task> {
	private final BintrayRepository repository;
	private final BintrayPublication publication;
	private final BintrayRestClient client;

	@Override
	public void execute(Task task) {
		publication.getArtifacts().forEach(this::publishFileToBintrayRepository);
	}

	private void publishFileToBintrayRepository(BintrayArtifact artifact) {
		client.put(artifact, repository);
	}
}
