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
