package dev.nokee.buildadapter.cmake.internal.plugins;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import dev.nokee.buildadapter.cmake.internal.GitBasedGroupIdSupplier;
import dev.nokee.buildadapter.cmake.internal.fileapi.*;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMSBuildAdapterTask;
import dev.nokee.buildadapter.cmake.internal.tasks.CMakeMakeAdapterTask;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.base.internal.tools.ToolRepository;
import dev.nokee.runtime.nativebase.internal.locators.CmakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.MSBuildLocator;
import dev.nokee.runtime.nativebase.internal.locators.MakeLocator;
import dev.nokee.runtime.nativebase.internal.locators.VswhereLocator;
import lombok.SneakyThrows;
import lombok.val;
import lombok.var;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.internal.os.OperatingSystem;

import javax.inject.Inject;
import java.io.File;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import static dev.nokee.buildadapter.cmake.internal.DeferUtils.asToStringObject;

public abstract class CmakeBuildAdapterPlugin implements Plugin<Settings> {
	@SneakyThrows
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

		replyFiles.visit(new CMakeBuildAdapterCodeModelVisitor(settings, getProviders(), repository));
	}

	@Inject
	protected abstract ProviderFactory getProviders();

	private void configureProjectGroup(Project project) {
		project.setGroup(asToStringObject(new GitBasedGroupIdSupplier(project.getRootDir())));
	}
}
