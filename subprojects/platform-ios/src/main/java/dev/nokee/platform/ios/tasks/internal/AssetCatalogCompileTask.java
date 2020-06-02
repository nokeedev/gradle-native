package dev.nokee.platform.ios.tasks.internal;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.GradleWorkerExecutorEngine;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;

public abstract class AssetCatalogCompileTask extends DefaultTask {
	@OutputDirectory
	public abstract DirectoryProperty getDestinationDirectory();

	@SkipWhenEmpty
	@InputDirectory
	public abstract RegularFileProperty getSource();

	@Input
	public abstract Property<String> getIdentifier();

	@Nested
	public abstract Property<CommandLineTool> getAssetCompilerTool();

	@Inject
	protected abstract ObjectFactory getObjects();

	@TaskAction
	private void compile() {
		getAssetCompilerTool().get()
			.withArguments(
				"--output-format", "human-readable-text",
				"--notices", "--warnings",
				"--export-dependency-info", getTemporaryDir().getAbsolutePath() + "/assetcatalog_dependencies",
				"--output-partial-info-plist", getTemporaryDir().getAbsolutePath() + "/assetcatalog_generated_info.plist",
				"--app-icon", "AppIcon",
				"--compress-pngs",
				"--enable-on-demand-resources", "YES",
				"--filter-for-device-model", "iPhone10,5",
				"--filter-for-device-os-version", "13.2",
				"--sticker-pack-identifier-prefix", getIdentifier().get() + ".sticker-pack.",
				"--target-device", "iphone", "--target-device", "ipad",
				"--minimum-deployment-target", "11.2",
				"--platform", "iphonesimulator",
				"--product-type", "com.apple.product-type.application",
				"--compile", getDestinationDirectory().get().getAsFile().getAbsolutePath(), getSource().get().getAsFile().getAbsolutePath())
			.newInvocation()
			.appendStandardStreamToFile(new File(getTemporaryDir(), "outputs.txt"))
			.buildAndSubmit(getObjects().newInstance(GradleWorkerExecutorEngine.class));
	}
}
