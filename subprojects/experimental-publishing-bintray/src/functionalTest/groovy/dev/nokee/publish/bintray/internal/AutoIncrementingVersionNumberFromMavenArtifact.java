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

import lombok.Data;
import lombok.Value;
import lombok.val;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.net.HttpURLConnection;
import java.net.URL;

@Value
public class AutoIncrementingVersionNumberFromMavenArtifact {
	URL mavenMetadataUrl;

	@Override
	public String toString() {
		try {
			val httpConnection = (HttpURLConnection) mavenMetadataUrl.openConnection();
			val inStream = httpConnection.getInputStream();

			Serializer serializer = new Persister();

			val mavenMetadata = serializer.read(MavenMetadata.class, inStream);
			val latestVersion = org.gradle.util.VersionNumber.parse(mavenMetadata.getVersioning().getLatest());
			return String.format("%d.%d.%d", latestVersion.getMajor(), latestVersion.getMinor(), latestVersion.getMicro() + 1);
		} catch (Throwable e) {
			e.printStackTrace();
			return "0.0.0";
		}
	}

	@Data
	@Root(name = "metadata", strict = false)
	public static class MavenMetadata {
		@Element
		String version;

		@Element(type = Versioning.class)
		Versioning versioning;

		@Data
		@Root(name = "versioning", strict = false)
		public static class Versioning {
			@Element
			String latest;
		}
	}
}
