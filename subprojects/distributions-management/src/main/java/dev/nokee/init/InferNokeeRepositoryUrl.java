/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.init;

import org.gradle.api.Transformer;

import java.net.URI;
import java.net.URISyntaxException;

// TODO: Allow override of the Nokee repositories
final class InferNokeeRepositoryUrl implements Transformer<URI, NokeeVersion> {
	@Override
	public URI transform(NokeeVersion version) {
		if (version.isSnapshot()) {
			return uri("https://repo.nokee.dev/snapshot");
		} else {
			return uri("https://repo.nokee.dev/release");
		}
	}

	private static URI uri(String s) {
		try {
			return new URI(s);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
