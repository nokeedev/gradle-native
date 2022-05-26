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
package dev.nokee.nvm;

import org.gradle.api.Transformer;
import org.gradle.api.provider.ProviderFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static dev.nokee.nvm.ProviderUtils.forUseAtConfigurationTime;

// TODO: Allow override of the Nokee repositories
final class InferNokeeRepositoryUrl implements Transformer<URI, NokeeVersion> {
	private final ProviderFactory providers;

	public InferNokeeRepositoryUrl(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public URI transform(NokeeVersion version) {
		if (version.isSnapshot()) {
			return uri(forUseAtConfigurationTime(providers.systemProperty("dev.nokee.repository.snapshot.url.override"))
				.orElse("https://repo.nokee.dev/snapshot").get());
		} else {
			return uri(forUseAtConfigurationTime(providers.systemProperty("dev.nokee.repository.release.url.override"))
				.orElse("https://repo.nokee.dev/release").get());
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
