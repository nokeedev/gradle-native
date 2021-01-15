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
