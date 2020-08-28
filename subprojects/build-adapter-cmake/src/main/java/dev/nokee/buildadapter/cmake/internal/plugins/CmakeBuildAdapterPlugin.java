package dev.nokee.buildadapter.cmake.internal.plugins;

import dev.nokee.buildadapter.cmake.internal.GitBasedGroupIdSupplier;
import dev.nokee.buildadapter.cmake.internal.fileapi.CodeModelClientImpl;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.nativebase.internal.locators.CmakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.MSBuildLocator;
import dev.nokee.runtime.nativebase.internal.locators.MakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.VswhereLocator;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.buildadapter.cmake.internal.DeferUtils.asToStringObject;

public class CmakeBuildAdapterPlugin implements Plugin<Settings> {
	private final ProviderFactory providerFactory;

	@Inject
	public CmakeBuildAdapterPlugin(ProviderFactory providerFactory) {
		this.providerFactory = providerFactory;
	}

	@Override
	public void apply(Settings settings) {
		val repository = new ToolRepository();
		repository.register("cmake", new CmakeLocator());
		repository.register("make", new MakeLocator());
		repository.register("msbuild", new MSBuildLocator(() -> CommandLineTool.of(repository.findAll("vswhere").iterator().next().getPath())));
		repository.register("vswhere", new VswhereLocator());

		settings.getGradle().allprojects(this::configureProjectGroup);

		val client = new CodeModelClientImpl(() -> CommandLineTool.of(repository.findAll("cmake").iterator().next().getPath()));
		val replyFiles = client.query(settings.getRootDir());

		replyFiles.visit(new CMakeBuildAdapterCodeModelVisitor(settings, providerFactory, repository));
	}

	private void configureProjectGroup(Project project) {
		project.setGroup(asToStringObject(new GitBasedGroupIdSupplier(project.getRootDir())));
	}
}
