package dev.nokee.runtime.base.internal;

import lombok.val;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

public abstract class IdentityTransform implements TransformAction<TransformParameters.None> {
	@PathSensitive(PathSensitivity.ABSOLUTE)
	@InputArtifact
	public abstract Provider<FileSystemLocation> getInputArtifact();

	@Override
	public void transform(TransformOutputs outputs) {
		val input = getInputArtifact().get().getAsFile();
		if (input.isDirectory()) {
			outputs.dir(input);
		} else if (input.isFile()) {
			outputs.file(input);
		} else {
			throw new IllegalArgumentException(String.format("File/directory does not exist: %s", input.getAbsolutePath()));
		}
	}
}
