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

import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("UnstableApiUsage")
abstract class CurrentNokeeVersionSource implements ValueSource<NokeeVersion, CurrentNokeeVersionSource.Parameters> {
	private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\"\\s*:\\s*\"(.+)\"");

	interface Parameters extends ValueSourceParameters {
		Property<NetworkStatus> getNetworkStatus();
		Property<URI> getCurrentReleaseUrl();

		enum NetworkStatus {
			ALLOWED, DISALLOWED
		}
	}

	@Inject
	public CurrentNokeeVersionSource() {}

	@Nullable
	@Override
	public NokeeVersion obtain() {
		if (getParameters().getNetworkStatus().getOrElse(Parameters.NetworkStatus.ALLOWED) == Parameters.NetworkStatus.DISALLOWED) {
			return null; // no network, no version
		}

		try {
			final URLConnection connection = getParameters().getCurrentReleaseUrl().get().toURL().openConnection();
			connection.connect();
			final Scanner s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
			if (!s.hasNext()) {
				return null; // no data, no version
			}
			final String content = s.next();
			final Matcher matcher = VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return NokeeVersion.version(matcher.group(1));
			} else {
				return null; // malformed data, assume no version
			}
		} catch (IOException e) {
			return null; // exception, assume no version
		}
	}
}
