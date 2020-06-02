package dev.nokee.platform.ios.tasks.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.GradleWorkerExecutorEngine;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;

public abstract class SignIosApplicationBundleTask extends DefaultTask {
	@SkipWhenEmpty
	@InputDirectory
	public abstract Property<FileSystemLocation> getUnsignedApplicationBundle();

	@OutputDirectory
	public abstract Property<FileSystemLocation> getSignedApplicationBundle();

	@Nested
	public abstract Property<CommandLineTool> getCodeSignatureTool();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@Inject
	protected abstract ObjectFactory getObjects();

	@TaskAction
	private void sign() {
		getFileOperations().sync(spec -> {
			spec.from(getUnsignedApplicationBundle());
			spec.into(getSignedApplicationBundle());
		});

		getCodeSignatureTool().get()
			.withArguments(
				"--force",
				"--sign", "-",
				"--timestamp=none",
				getSignedApplicationBundle().get().getAsFile().getAbsolutePath())
			.newInvocation()
			.appendStandardStreamToFile(new File(getTemporaryDir(), "outputs.txt"))
			.buildAndSubmit(getObjects().newInstance(GradleWorkerExecutorEngine.class));
	}
}
