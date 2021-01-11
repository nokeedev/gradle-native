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

public class SignIosApplicationBundleTask extends DefaultTask {
	private final Property<FileSystemLocation> unsignedApplicationBundle;
	private final Property<FileSystemLocation> signedApplicationBundle;
	private final Property<CommandLineTool> codeSignatureTool;
	private final FileSystemOperations fileOperations;
	private final ObjectFactory objects;

	@SkipWhenEmpty
	@InputDirectory
	public Property<FileSystemLocation> getUnsignedApplicationBundle() {
		return unsignedApplicationBundle;
	}

	@OutputDirectory
	public Property<FileSystemLocation> getSignedApplicationBundle() {
		return signedApplicationBundle;
	}

	@Nested
	public Property<CommandLineTool> getCodeSignatureTool() {
		return codeSignatureTool;
	}

	@Inject
	public SignIosApplicationBundleTask(ObjectFactory objects, FileSystemOperations fileOperations) {
		this.unsignedApplicationBundle = objects.property(FileSystemLocation.class);
		this.signedApplicationBundle = objects.property(FileSystemLocation.class);
		this.codeSignatureTool = objects.property(CommandLineTool.class);
		this.fileOperations = fileOperations;
		this.objects = objects;
	}

	@TaskAction
	private void sign() {
		fileOperations.sync(spec -> {
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
			.buildAndSubmit(objects.newInstance(GradleWorkerExecutorEngine.class));
	}
}
