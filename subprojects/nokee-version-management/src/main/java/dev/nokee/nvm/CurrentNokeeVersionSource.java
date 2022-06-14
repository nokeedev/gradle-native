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

import lombok.val;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
abstract class CurrentNokeeVersionSource implements ValueSource<NokeeVersion, CurrentNokeeVersionSource.Parameters> {
	private final NokeeVersionLoader loader;

	interface Parameters extends NokeeVersionParameters {
		Property<NetworkStatus> getNetworkStatus();
		Property<URI> getCurrentReleaseUrl();

		enum NetworkStatus {
			ALLOWED, DISALLOWED
		}
	}

	@Inject
	public CurrentNokeeVersionSource() {
		this(DefaultNokeeVersionLoader.INSTANCE);
	}

	public CurrentNokeeVersionSource(NokeeVersionLoader loader) {
		this.loader = loader;
	}

	@Nullable
	@Override
	public NokeeVersion obtain() {
		val result = Optional.ofNullable(loader.fromFile(getParameters().getNokeeVersionFile().getAsFile().get().toPath())).orElseGet(() -> {

			if (getParameters().getNetworkStatus().getOrElse(Parameters.NetworkStatus.ALLOWED) == Parameters.NetworkStatus.DISALLOWED) {
				return null; // no network, no version
			}

			try {
				return loader.fromUrl(getParameters().getCurrentReleaseUrl()
					.getOrElse(new URI("https://services.nokee.dev/versions/current.json")).toURL());
			} catch (IOException | URISyntaxException e) {
				return null; // exception, assume no version
			}
		});

		if (result != null) {
			try {
				Files.write(getParameters().getNokeeVersionFile().getAsFile().get().toPath(), result.toString().getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				// could not write versionFile
			}
		}

		return result;
	}
}
