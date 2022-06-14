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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DefaultNokeeVersionLoader implements NokeeVersionLoader {
	private static final Pattern VERSION_PATTERN = Pattern.compile("\"version\"\\s*:\\s*\"(.+)\"");
	public static final DefaultNokeeVersionLoader INSTANCE = new DefaultNokeeVersionLoader(NokeeVersion::version);
	private final NokeeVersionParser parser;

	public DefaultNokeeVersionLoader(NokeeVersionParser parser) {
		this.parser = parser;
	}

	@Nullable
	@Override
	public NokeeVersion fromFile(Path versionFile) {
		if (Files.exists(versionFile)) {
			try {
				return parser.parse(new String(Files.readAllBytes(versionFile)).trim());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return null;
	}

	@Nullable
	@Override
	public NokeeVersion fromUrl(URL versionUrl) {
		try {
			final URLConnection connection = versionUrl.openConnection();
			connection.setRequestProperty("User-Agent", "Nokee Version Management plugin"); // required by GitHub hosting
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
