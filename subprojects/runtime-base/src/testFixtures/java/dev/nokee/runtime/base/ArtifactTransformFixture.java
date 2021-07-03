package dev.nokee.runtime.base;

public interface ArtifactTransformFixture {
	static boolean doesNotTransformArtifacts(String output) {
		return !output.contains("Transforming");
	}

//	static boolean transform(String artifactName, String usingTransformRule) {
//
//	}
}
