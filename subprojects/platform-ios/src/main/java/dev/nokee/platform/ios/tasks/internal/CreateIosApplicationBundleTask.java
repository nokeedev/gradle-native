package dev.nokee.platform.ios.tasks.internal;

import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public abstract class CreateIosApplicationBundleTask extends DefaultTask {
	@OutputDirectory
	public abstract Property<FileSystemLocation> getApplicationBundle();

	@Internal
	public abstract RegularFileProperty getExecutable();

	@Input
	public abstract Property<Boolean> getSwiftSupportRequired();

	@Internal
	public abstract ConfigurableFileCollection getSources();

	@SkipWhenEmpty
	@InputFiles
	@PathSensitive(PathSensitivity.RELATIVE)
	protected FileTree getInputFiles() {
		return getSources().getAsFileTree();
	}

	@InputFiles
	public abstract ConfigurableFileCollection getPlugIns();

	@InputFiles
	public abstract ConfigurableFileCollection getFrameworks();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@TaskAction
	private void create() throws IOException {
		getFileOperations().sync(spec -> {
			spec.from(getSources().getFiles());

			for (File file : getFrameworks().getFiles()) {
				if (file.isDirectory()) {
					spec.from(file, it -> it.into("Frameworks/" + file.getName()));
				} else {
					spec.from(file, it -> it.into("Frameworks"));
				}
			}

			for (File file : getPlugIns().getFiles()) {
				if (file.isDirectory()) {
					spec.from(file, it -> it.into("PlugIns/" + file.getName()));
				} else {
					spec.from(file, it -> it.into("PlugIns"));
				}
			}
			spec.into(getApplicationBundle());
		});

		// Oversimplification of how this file is created
		FileUtils.write(new File(getApplicationBundle().get().getAsFile(), "PkgInfo"), "APPL????", Charset.defaultCharset());

		// TODO: This could probably be a strategy/policy added to the task.
		//  It could also be an doLast action added to the task.
		if (getSwiftSupportRequired().get()) {
			getExecOperations().exec(spec -> {
				File bundleFile = getExecutable().get().getAsFile();
				File bundleDir = getApplicationBundle().get().getAsFile();
				spec.executable(getSwiftStdlibTool());
				spec.args(
					"--copy",
					"--scan-executable", bundleFile.getAbsolutePath(),
					"--destination", new File(bundleDir, "Frameworks").getAbsolutePath(),
					"--platform", "iphonesimulator",
					"--scan-folder", new File(bundleDir, "Frameworks").getAbsolutePath()
				);
			});
		}
	}

	private static String getSwiftStdlibTool() {
		return CommandLine.of("xcrun", "--sdk", "iphonesimulator", "--find", "swift-stdlib-tool")
			.execute(new ProcessBuilderEngine())
			.waitFor()
			.assertNormalExitValue()
			.getStandardOutput()
			.getAsString()
			.trim();
	}
}
