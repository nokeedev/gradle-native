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
