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
