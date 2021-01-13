package dev.nokee.publish.bintray.internal;

import lombok.val;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

import static org.apache.commons.lang3.exception.ExceptionUtils.rethrow;

public final class BintrayRestClient {
	private final WorkerExecutor workers;

	@Inject
	public BintrayRestClient(WorkerExecutor workers) {
		this.workers = workers;
	}

	public void put(BintrayArtifact artifact, BintrayRepository repository) {
		workers.noIsolation().submit(HttpPutWorkAction.class, parameters -> {
			parameters.getFile().fileValue(artifact.getFile());
			parameters.getUsername().set(repository.getCredentials().getBintrayUser());
			parameters.getPassword().set(repository.getCredentials().getBintrayKey());
			parameters.getUrl().set(newApiUrl(repository, artifact));
			parameters.getRelativePath().set(artifact.getRelativePath());
		});
	}

	private URL newApiUrl(BintrayRepository repository, BintrayArtifact artifact) {
		val publish = true;
		val override = System.getProperty("force") != null;
		try {
			return new URL("https://api.bintray.com/content" + repository.getUrl().getPath() + "/" + repository.getPackageName().get() + "/" + artifact.getVersion() + "/" + artifact.getRelativePath() + "?publish=" + (publish ? "1" : "0") + "&override=" + (override ? "1" : "0"));
		} catch (MalformedURLException e) {
			return rethrow(e);
		}
	}
}
