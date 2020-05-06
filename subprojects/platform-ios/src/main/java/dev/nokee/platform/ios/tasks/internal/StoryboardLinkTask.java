package dev.nokee.platform.ios.tasks.internal;

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
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class StoryboardLinkTask extends DefaultTask {
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
	private void doLink() {
		String ibtoolExecutable = getIbtoolExecutable().getAbsolutePath();

		getExecOperations().exec(spec -> {
			spec.setExecutable(ibtoolExecutable);
			spec.args("--errors", "--warnings", "--notices", "--module", getModule().get(), "--auto-activate-custom-fonts", "--target-device", "iphone", "--target-device", "ipad", "--minimum-deployment-target", "13.2", "--output-format", "human-readable-text", "--link", getDestinationDirectory().get().getAsFile().getAbsolutePath(), getSources().getFiles().stream().flatMap(it -> Arrays.stream(it.listFiles())).map(File::getAbsolutePath).collect(Collectors.joining(" ")));
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt")));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});
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
