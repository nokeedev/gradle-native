package dev.nokee.publish.bintray.internal;

import lombok.Value;

import java.io.File;

@Value
public class BintrayArtifact {
	File file;
	String relativePath;
	String version;
}
