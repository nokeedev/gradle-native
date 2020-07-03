package dev.nokee.ide.visualstudio.internal.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeTarget;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeProject;
import dev.nokee.ide.visualstudio.internal.VisualStudioIdePropertyAdapter;
import dev.nokee.ide.visualstudio.internal.vcxproj.*;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.*;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenerateVisualStudioIdeProjectTask extends DefaultTask {
	private final DefaultVisualStudioIdeProject visualStudioProject;

	@Internal
	public abstract RegularFileProperty getProjectLocation();

	@Internal
	protected abstract RegularFileProperty getFiltersLocation();

	@Internal
	public abstract ListProperty<String> getAdditionalGradleArguments();

	@Internal
	public abstract Property<String> getGradleCommand();

	@Internal
	public abstract Property<String> getBridgeTaskPath();

	@Inject
	public GenerateVisualStudioIdeProjectTask(VisualStudioIdeProject visualStudioProject) {
		this.visualStudioProject = (DefaultVisualStudioIdeProject) visualStudioProject;
		getFiltersLocation().fileProvider(getProjectLocation().map(it -> new File(it.getAsFile().getAbsolutePath() + ".filters")));
	}

	@TaskAction
	private void doGenerate() throws Exception {
		Style style = new CamelCaseStyle(true, true);
		Format format = new Format(3, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", style);
		Registry registry = new Registry();
		Strategy strategy = new RegistryStrategy(registry);
		Serializer serializer = new Persister(strategy, format);
		registry.bind(VCXProperty.class, VCXProperty.Serializer.class);

		serializer.write(getVcxProject(), getProjectLocation().get().getAsFile());
		serializer.write(getVcxFilters(), getFiltersLocation().get().getAsFile());
	}

	private VCXProject getVcxProject() {
		val nodes = ImmutableList.builder();
		nodes.add(getItemGroupProjectConfigurations());
		nodes.add(getPropertyGroupGlobals());
		nodes.add(VCXImport.of("$(VCTargetsPath)\\Microsoft.Cpp.Default.props"));
		nodes.addAll(getPropertyGroupConfiguration());
		nodes.add(VCXImport.of("$(VCTargetsPath)\\Microsoft.Cpp.props"));
		nodes.addAll(getImportGroupPropertySheets());
		nodes.addAll(getItemDefinitionGroupConfiguration());
		nodes.add(getItemGroupSourceFiles());
		nodes.add(getItemGroupHeaderFiles());
		nodes.add(getItemGroupBuildFiles());
		nodes.add(VCXImport.of("$(VCTargetsPath)\\Microsoft.Cpp.targets"));
		nodes.add(getBuildTarget());
		nodes.add(getCleanTarget());
		nodes.add(new VCXTarget("PrepareForBuild", "$(PrepareForBuildDependsOn)", null));
		return new VCXProject("Build", null, nodes.build());
	}

	private VCXItemGroup getItemGroupProjectConfigurations() {
		return VCXItemGroup.of(visualStudioProject.getTargets().stream().map(VisualStudioIdeTarget::getProjectConfiguration).map(VCXProjectConfiguration::of).collect(Collectors.toList())).withLabel("ProjectConfigurations");
	}

	private VCXPropertyGroup getPropertyGroupGlobals() {
		return VCXPropertyGroup.of(
			VCXProperty.of("VCProjectVersion", "16.0"),
			VCXProperty.of("Keyword", "Win32Proj"),
			VCXProperty.of("ProjectGuid", "{" + visualStudioProject.getProjectGuid().get().toString() + "}"),
			VCXProperty.of("RootNamespace", "ConsoleApplication1"),
			VCXProperty.of("WindowsTargetPlatformVersion", "10.0")
		).withLabel("Globals");
	}

	private List<VCXPropertyGroup> getPropertyGroupConfiguration() {
		return visualStudioProject.getTargets().stream().map(target -> {
			return VCXPropertyGroup.of(target.getProperties().getElements().get().entrySet().stream().map(it -> VCXProperty.of(it.getKey(), it.getValue().toString())).collect(Collectors.toList())).withCondition(conditionOf(target.getProjectConfiguration()));
		}).collect(Collectors.toList());
	}

	private List<VCXImportGroup> getImportGroupPropertySheets() {
		return visualStudioProject.getTargets().stream().map(target -> {
			return VCXImportGroup.of(
				VCXImport.of("$(UserRootDir)\\Microsoft.Cpp.$(Platform).user.props").withCondition("exists('$(UserRootDir)\\Microsoft.Cpp.$(Platform).user.props')").withLabel("LocalAppDataPlatform")
			).withLabel("PropertySheets").withCondition(conditionOf(target.getProjectConfiguration()));
		}).collect(Collectors.toList());
	}

	private String conditionOf(VisualStudioIdeProjectConfiguration projectConfiguration) {
		return String.format("'$(Configuration)|$(Platform)'=='%s|%s'", projectConfiguration.getConfiguration().getIdentifier(), projectConfiguration.getPlatform().getIdentifier());
	}

	private List<VCXItemDefinitionGroup> getItemDefinitionGroupConfiguration() {
		return visualStudioProject.getTargets().stream().map(target -> {
			val names = new HashSet<String>(target.getItemProperties().getNames());
			names.removeAll(ImmutableSet.of("ClCompile", "Link"));
			if (!names.isEmpty()) {
				throw new UnsupportedOperationException();
			}

			List<VCXItemDefinition> definitions = new ArrayList<>();
			val clCompile = target.getItemProperties().findByName("ClCompile");
			if (clCompile != null) {
				definitions.add(VCXClCompile.Definition.of(clCompile.getElements().get().entrySet().stream().map(it -> VCXProperty.of(it.getKey(), it.getValue().toString())).collect(Collectors.toList())));
			}

			val link = target.getItemProperties().findByName("Link");
			if (link != null) {
				definitions.add(VCXLink.Definition.of(link.getElements().get().entrySet().stream().map(it -> VCXProperty.of(it.getKey(), it.getValue().toString())).collect(Collectors.toList())));
			}
			return VCXItemDefinitionGroup.of(definitions).withCondition(conditionOf(target.getProjectConfiguration()));
		}).collect(Collectors.toList());
	}

	private VCXItemGroup getItemGroupSourceFiles() {
		return VCXItemGroup.of(visualStudioProject.getSourceFiles().getFiles().stream().map(it -> VCXClCompile.Item.of(it.getAbsolutePath())).collect(Collectors.toList()));
	}

	private VCXItemGroup getItemGroupHeaderFiles() {
		return VCXItemGroup.of(visualStudioProject.getHeaderFiles().getFiles().stream().map(it -> VCXClInclude.Item.of(it.getAbsolutePath())).collect(Collectors.toList()));
	}

	private VCXItemGroup getItemGroupBuildFiles() {
		return VCXItemGroup.of(visualStudioProject.getBuildFiles().getFiles().stream().map(it -> new VCXNone(it.getAbsolutePath())).collect(Collectors.toList()));
	}

	private VCXTarget getBuildTarget() {
		return new VCXTarget("Build", null, ImmutableList.of(new VCXExec(String.format("\"%s\" %s", getGradleCommand().get(), getGradleBuildArgumentsString("build")), "", ".")));
	}

	private VCXTarget getCleanTarget() {
		return new VCXTarget("Clean", null, ImmutableList.of(new VCXExec(String.format("\"%s\" %s", getGradleCommand().get(), getGradleBuildArgumentsString("clean")), "", ".")));
	}

	private String getGradleBuildArgumentsString(String action) {
		return String.join(" ", Iterables.concat(VisualStudioIdePropertyAdapter.getAdapterCommandLine(), getAdditionalGradleArguments().get())) + " " + String.format(getBridgeTaskPath().get(), action);
	}

	private VCXProject getVcxFilters() {
		val nodes = ImmutableList.builder();
		nodes.add(VCXItemGroup.of(VCXFilter.DEFAULT_FILTERS));
		nodes.add(VCXItemGroup.of(visualStudioProject.getSourceFiles().getFiles().stream().map(it -> VCXClCompile.Item.of(it.getAbsolutePath()).withFilter("Source Files")).collect(Collectors.toList())));
		nodes.add(VCXItemGroup.of(visualStudioProject.getHeaderFiles().getFiles().stream().map(it -> VCXClCompile.Item.of(it.getAbsolutePath()).withFilter("Header Files")).collect(Collectors.toList())));
		nodes.add(VCXItemGroup.of(visualStudioProject.getBuildFiles().getFiles().stream().map(it -> new VCXNone(it.getAbsolutePath())).collect(Collectors.toList())));
		return new VCXProject(null, "4.0", nodes.build());
	}
}
