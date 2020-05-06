package dev.nokee.platform.ios.tasks.internal;

import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;

public abstract class AssetCatalogCompileTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@InputDirectory
	public abstract RegularFileProperty getSource();

	@Input
	public abstract Property<String> getIdentifier();

	@InputFile
	protected File getActoolExecutable() {
		return new File(getActoolPath());
	}

	private static String getActoolPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--find", "actool").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Inject
	public abstract ExecOperations getExecOperations();

	@TaskAction
	private void compile() {
		getExecOperations().exec(spec -> {
			spec.executable(getActoolExecutable().getAbsolutePath());
			spec.args("--output-format", "human-readable-text", "--notices", "--warnings", "--export-dependency-info", getTemporaryDir().getAbsolutePath() + "/assetcatalog_dependencies", "--output-partial-info-plist", getTemporaryDir().getAbsolutePath() + "/assetcatalog_generated_info.plist", "--app-icon", "AppIcon", "--compress-pngs", "--enable-on-demand-resources", "YES", "--filter-for-device-model", "iPhone10,5", "--filter-for-device-os-version", "13.2", "--sticker-pack-identifier-prefix", getIdentifier().get() + ".sticker-pack.", "--target-device", "iphone", "--target-device", "ipad", "--minimum-deployment-target", "11.2", "--platform", "iphonesimulator", "--product-type", "com.apple.product-type.application", "--compile", getDestinationDirectory().get().getAsFile().getAbsoluteFile(), getSource().get().getAsFile().getAbsolutePath());
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt")));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
