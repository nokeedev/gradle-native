package dev.nokee.platform.ios.tasks.internal;

import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;

public abstract class SignIosApplicationBundleTask extends DefaultTask {
	@InputDirectory
	public abstract Property<FileSystemLocation> getUnsignedApplicationBundle();

	@OutputDirectory
	public abstract Property<FileSystemLocation> getSignedApplicationBundle();

	@Inject
	protected abstract ExecOperations getExecOperations();

	@Inject
	protected abstract FileSystemOperations getFileOperations();

	@TaskAction
	private void sign() {
		getFileOperations().sync(spec -> {
			spec.from(getUnsignedApplicationBundle());
			spec.into(getSignedApplicationBundle());
		});

		getExecOperations().exec(spec -> {
			spec.setExecutable(getCodesignExecutable().getAbsolutePath());
			spec.args("--force", "--sign", "-", "--timestamp=none", getSignedApplicationBundle().get().getAsFile().getAbsolutePath());
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"), true));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@InputFile
	protected File getCodesignExecutable() {
		return new File(getCodesignPath());
	}

	private static String getCodesignPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--find", "codesign").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
