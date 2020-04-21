package dev.nokee.platform.ios.tasks.internal;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;

public abstract class StoryboardCompileTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@Input
	public abstract Property<String> getModule();

	// TODO: This may need to be richer so we keep the context path
	@InputFiles
	public abstract ConfigurableFileCollection getSources();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@TaskAction
	private void compile() {
		String ibtoolExecutable = getIbtoolExecutable().getAbsolutePath();

		new File(getTemporaryDir(), "outputs.txt").delete();
		for (File source : getSources()) {
			getExecOperations().exec(spec -> {
				spec.setExecutable(ibtoolExecutable);
				spec.args("--errors", "--warnings", "--notices", "--module", getModule().get(), "--output-partial-info-plist", getTemporaryDir().getAbsolutePath() + "/" + FilenameUtils.removeExtension(source.getName()) + "-SBPartialInfo.plist", "--auto-activate-custom-fonts", "--target-device", "iphone", "--target-device", "ipad", "--minimum-deployment-target", "13.3", "--output-format", "human-readable-text", "--compilation-directory", getDestinationDirectory().get().getAsFile().getAbsolutePath() + "/" + source.getParentFile().getName(), source.getAbsolutePath());
				try {
					spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"), true));
				} catch (FileNotFoundException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
	}

	@InputFile
	protected File getIbtoolExecutable() {
		return new File(getIbtoolPath());
	}

	private static String getIbtoolPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--find", "ibtool").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
