package dev.nokee.ide.visualstudio.internal.tasks;

import com.google.common.collect.ImmutableList;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeProject;
import lombok.Value;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.stream.*;

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenerateVisualStudioIdeProjectTask extends DefaultTask {
	private final DefaultVisualStudioIdeProject visualStudioProject;

	@Internal
	public abstract RegularFileProperty getProjectLocation();

	@Internal
	protected abstract RegularFileProperty getFilterLocation();

	@Internal
	public abstract ListProperty<String> getAdditionalGradleArguments();

	@Internal
	public abstract Property<String> getGradleCommand();

	@Inject
	public GenerateVisualStudioIdeProjectTask(VisualStudioIdeProject visualStudioProject) {
		this.visualStudioProject = (DefaultVisualStudioIdeProject) visualStudioProject;
		getFilterLocation().fileProvider(getProjectLocation().map(it -> new File(it.getAsFile().getAbsolutePath() + ".filter")));
	}

	@TaskAction
	private void doGenerate() throws Exception {
		val projectNodes = ImmutableList.builder();

		val projConf = ImmutableList.of(new Project.ProjectConfiguration("Default", "x64"));
		projectNodes.add(new Project.ItemGroup("ProjectConfigurations", projConf));

		projectNodes.add(new Project.PropertyGroup(null, "Globals", ImmutableList.of(new Project.KeyValuePair("VCProjectVersion", "16.0"), new Project.KeyValuePair("Keyword", "Win32Proj"), new Project.KeyValuePair("ProjectGuid", "{" + visualStudioProject.getProjectGuid().get().toString() + "}"), new Project.KeyValuePair("RootNamespace", "ConsoleApplication1"), new Project.KeyValuePair("WindowsTargetPlatformVersion", "10.0"))));

		projectNodes.add(new Project.Import(null, "$(VCTargetsPath)\\Microsoft.Cpp.Default.props", null));

		projectNodes.add(new Project.PropertyGroup("'$(Configuration)|$(Platform)'=='Default|Win32'", "Configuration", ImmutableList.of(new Project.KeyValuePair("ConfigurationType", "Application"), new Project.KeyValuePair("UseDebugLibraries", "true"), new Project.KeyValuePair("PlatformToolset", "v142"), new Project.KeyValuePair("CharacterSet", "Unicode"))));

		projectNodes.add(new Project.Import(null, "$(VCTargetsPath)\\Microsoft.Cpp.props", null));

		projectNodes.add(new Project.ImportGroup("ExtensionSettings", null, null));
		projectNodes.add(new Project.ImportGroup("Shared", null, null));

		projectNodes.add(new Project.ImportGroup("PropertySheets", "'$(Configuration)|$(Platform)'=='Default|Win32'", ImmutableList.of(new Project.Import("LocalAppDataPlatform", "$(UserRootDir)\\Microsoft.Cpp.$(Platform).user.props", "exists('$(UserRootDir)\\Microsoft.Cpp.$(Platform).user.props')"))));

		projectNodes.add(new Project.PropertyGroup(null, "UserMacros", null));
		projectNodes.add(new Project.PropertyGroup("'$(Configuration)|$(Platform)'=='Default|x64'", null, ImmutableList.of(new Project.KeyValuePair("LinkIncremental", "true"))));

		projectNodes.add(new Project.ItemDefinitionGroup("'$(Configuration)|$(Platform)'=='Default|x64'", ImmutableList.of(new Project.ClCompileDefinition(ImmutableList.of(new Project.KeyValuePair("WarningLevel", "Level3"), new Project.KeyValuePair("SDLCheck", "true"))), new Project.LinkDefinition(ImmutableList.of(new Project.KeyValuePair("SubSystem", "Console"))))));

		projectNodes.add(new Project.ItemGroup(null, visualStudioProject.getSourceFiles().getFiles().stream().map(it -> new Project.ClCompileItem(it.getAbsolutePath(), null)).collect(Collectors.toList())));

		projectNodes.add(new Project.ItemGroup(null, ImmutableList.of(new Project.NoneItem("build.gradle"), new Project.NoneItem("settings.gradle"))));
		projectNodes.add(new Project.Import(null, "$(VCTargetsPath)\\Microsoft.Cpp.targets", null));
		projectNodes.add(new Project.ImportGroup("ExtensionTargets", null, null));

		projectNodes.add(new Project.Target("Build", null, ImmutableList.of(new Project.Exec("\"gradle\" build", "", "."))));
		projectNodes.add(new Project.Target("Clean", null, ImmutableList.of(new Project.Exec("\"gradle\" clean", "", "."))));
		projectNodes.add(new Project.Target("PrepareForBuild", "$(PrepareForBuildDependsOn)", null));

		val proj = new Project("Build", null, projectNodes.build());


		val filterNodes = ImmutableList.builder();
		filterNodes.add(new Project.ItemGroup(null, Project.Filter.DEFAULT_FILTERS));

		filterNodes.add(new Project.ItemGroup(null, visualStudioProject.getSourceFiles().getFiles().stream().map(it -> new Project.ClCompileItem(it.getAbsolutePath(), "Source Files")).collect(Collectors.toList())));
		filterNodes.add(new Project.ItemGroup(null, visualStudioProject.getHeaderFiles().getFiles().stream().map(it -> new Project.ClCompileItem(it.getAbsolutePath(), "Header Files")).collect(Collectors.toList())));

		filterNodes.add(new Project.ItemGroup(null, visualStudioProject.getBuildFiles().getFiles().stream().map(it -> new Project.NoneItem(it.getAbsolutePath())).collect(Collectors.toList())));
		val filterProj = new Project(null, "4.0", filterNodes.build());

		Style style = new CamelCaseStyle();
		Format format = new Format(3, "<?xml version=\"1.0\" encoding=\"utf-8\"?>", style);
		Registry registry = new Registry();
		Strategy strategy = new RegistryStrategy(registry);
		Serializer serializer = new Persister(strategy, format);

		registry.bind(Project.KeyValuePair.class, Project.ExternalConverter.class);

		serializer.write(proj, getProjectLocation().get().getAsFile());
		serializer.write(filterProj, new File(getProjectLocation().get().getAsFile().getAbsolutePath() + ".filter"));
	}

	@Value
	@Root
	@Namespace(reference = "http://schemas.microsoft.com/developer/msbuild/2003")
	private static class Project {
		@Attribute(required = false)
		String defaultTargets;

		@Attribute(required = false)
		String toolVersions;

		@ElementListUnion({
			@ElementList(inline = true, type = ItemGroup.class),
			@ElementList(inline = true, type = PropertyGroup.class),
			@ElementList(inline = true, type = Import.class),
			@ElementList(inline = true, type = ImportGroup.class),
			@ElementList(inline = true, type = ItemDefinitionGroup.class),
			@ElementList(inline = true, type = Target.class)
		})
		List<Object> nodes;

		@Value
		@Root
		public static class ItemGroup {
			@Attribute(required = false)
			String label;

			@ElementListUnion({
				@ElementList(inline = true, type = ProjectConfiguration.class),
				@ElementList(inline = true, type = ClCompileItem.class),
				@ElementList(inline = true, type = NoneItem.class),
				@ElementList(inline = true, type = Filter.class)
			})
			List<? extends Item> items;
		}

		public interface Item {

		}

		@Value
		public static class ProjectConfiguration implements Item {
			@Attribute
			public String getInclude() {
				return configuration + "|" + platform;
			}

			@Element
			String configuration;

			@Element
			String platform;
		}

		@Value
		public static class Filter implements Item {
			@Attribute
			String include;

			@Element
			String uniqueIdentifier;

			@Element
			String extensions;

			public static final Filter SOURCE_FILES = new Project.Filter("Source Files", "{4FC737F1-C7A5-4376-A066-2A32D752A2FF}", "cpp;c;cc;cxx;c++;def;odl;idl;hpj;bat;asm;asmx");
			public static final Filter HEADER_FILES = new Project.Filter("Header Files", "{93995380-89BD-4b04-88EB-625FBE52EBFB}", "h;hh;hpp;hxx;h++;hm;inl;inc;ipp;xsd");
			public static final Filter RESOURCE_FILES = new Project.Filter("Resource Files", "{67DA6AB6-F800-4c08-8B7A-83BB121AAD01}", "rc;ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe;resx;tiff;tif;png;wav;mfcribbon-ms");
			public static final List<Filter> DEFAULT_FILTERS = ImmutableList.of(SOURCE_FILES, HEADER_FILES, RESOURCE_FILES);
		}

		@Value
		@Root(name = "ClCompile")
		public static class ClCompileDefinition implements Item {
			@ElementList(inline = true)
			List<KeyValuePair> properties;
		}

		@Value
		@Root(name = "ClCompile")
		public static class ClCompileItem implements Item {
			@Attribute
			String include;

			@Element(required = false)
			String filter;
		}

		@Value
		@Root(name = "None")
		public static class NoneItem implements Item {
			@Attribute
			String include;
		}

		@Value
		@Root(name = "Link")
		public static class LinkDefinition implements Item {
			@ElementList(inline = true)
			List<KeyValuePair> properties;
		}

		@Value
		public static class KeyValuePair {
			String key;
			String value;
		}

		public static class ExternalConverter implements Converter<KeyValuePair> {

			public KeyValuePair read(InputNode node) {
				throw new UnsupportedOperationException();
			}

			public void write(OutputNode node, KeyValuePair external) {
				node.setName(external.getKey());
				node.setValue(external.getValue());
			}
		}

		@Value
		@Root
		public static class PropertyGroup {
			@Attribute(required = false)
			String condition;

			@Attribute(required = false)
			String label;

			@ElementList(inline = true, required = false)
			List<KeyValuePair> properties;
		}

		@Value
		@Root
		public static class Import {
			@Attribute(required = false)
			String label;

			@Attribute
			String project;

			@Attribute(required = false)
			String condition;
		}

		@Value
		@Root
		public static class ImportGroup {
			@Attribute
			String label;

			@Attribute(required = false)
			String condition;

			@ElementList(inline = true, required = false)
			List<Import> imports;
		}

		@Value
		@Root
		public static class ItemDefinitionGroup {
			@Attribute
			String condition;

			@ElementListUnion({
				@ElementList(inline = true, type = ClCompileDefinition.class),
				@ElementList(inline = true, type = LinkDefinition.class)
			})
			List<Object> nodes;
		}

		@Value
		@Root
		public static class Target {
			@Attribute
			String name;

			@Attribute(required = false)
			String dependsOnTargets;

			@ElementListUnion({
				@ElementList(inline = true, required = false, type = Exec.class)
			})
			List<? extends Task> tasks;
		}

		public interface Task {

		}

		@Value
		@Root
		public static class Exec implements Task {
			@Attribute
			String command;

			@Attribute(required = false)
			String outputs;

			@Attribute
			String workingDirectory;
		}
	}
}
