package dev.nokee.ide.xcode.internal.tasks;

import com.dd.plist.*;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.ide.xcode.*;
import dev.nokee.ide.xcode.internal.XcodeIdePropertyAdapter;
import dev.nokee.ide.xcode.internal.services.XcodeIdeGidGeneratorService;
import dev.nokee.ide.xcode.internal.xcodeproj.*;
import lombok.Value;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GenerateXcodeIdeProjectTask extends DefaultTask {
	public static final XcodeIdeProductType INDEXER_PRODUCT_TYPE = XcodeIdeProductType.of("dev.nokee.product-type.indexer");
	private static final String PRODUCTS_GROUP_NAME = "Products";
	private Map<String, PBXFileReference> pathToFileReferenceMapping = new HashMap<>();
	private final XcodeIdeProject xcodeProject;

	@Internal
	public abstract Property<FileSystemLocation> getProjectLocation();

	@Internal
	public abstract Property<XcodeIdeGidGeneratorService> getGidGenerator();

	@Internal
	public abstract Property<String> getGradleCommand();

	@Internal
	public abstract Property<String> getBridgeTaskPath();

	@Internal
	public abstract ListProperty<String> getAdditionalGradleArguments();

	@Internal
	public abstract ConfigurableFileCollection getSources();

	@Inject
	public GenerateXcodeIdeProjectTask(XcodeIdeProject xcodeProject) {
		this.xcodeProject = xcodeProject;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	// TODO: Ensure that half generated projects are deleted (avoid leaking files)
	@TaskAction
	private void generate() throws IOException {
		File projectDirectory = getProjectLocation().get().getAsFile();
		FileUtils.deleteDirectory(projectDirectory);
		projectDirectory.mkdirs();

		PBXProject project = new PBXProject(getProject().getPath());

		// Convert all Gradle Xcode IDE targets to PBXTarget
		project.getTargets().addAll(xcodeProject.getTargets().stream().map(this::toTarget).collect(Collectors.toList()));

		// Configure sources
		getSources().forEach(file -> {
			project.getMainGroup().getChildren().add(toAbsoluteFileReference(file));
		});
		xcodeProject.getTargets().forEach(target -> {
			List<PBXReference> fileReferences = project.getMainGroup().getOrCreateChildGroupByName(target.getName()).getChildren();
			target.getSources().forEach(file -> fileReferences.add(toAbsoluteFileReference(file)));
		});

		// Add all target product reference to Products source group
		project.getMainGroup().getOrCreateChildGroupByName(PRODUCTS_GROUP_NAME).getChildren().addAll(project.getTargets().stream().map(PBXTarget::getProductReference).collect(Collectors.toList()));

		// Create all build configuration at the project level, Xcode expect that.
		xcodeProject.getTargets().stream().flatMap(it -> it.getBuildConfigurations().stream()).map(XcodeIdeBuildConfiguration::getName).forEach(name -> {
			project.getBuildConfigurationList().getBuildConfigurationsByName().getUnchecked(name);
		});


		// Do the schemes...
		File schemesDirectory = new File(projectDirectory, "xcshareddata/xcschemes");
		schemesDirectory.mkdirs();
		XmlMapper xmlMapper = new XmlMapper();
		SimpleModule simpleModule = new SimpleModule("BooleanAsYesNoString", new Version(1, 0, 0, null, null, null));
		simpleModule.addSerializer(Boolean.class,new XcodeIdeBooleanSerializer());
		simpleModule.addSerializer(boolean.class,new XcodeIdeBooleanSerializer());
		xmlMapper.registerModule(simpleModule);
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

		for (PBXTarget xcodeTarget : project.getTargets()) {
			xmlMapper.writeValue(new File(schemesDirectory, xcodeTarget.getName() + ".xcscheme"), new Scheme(
				new Scheme.BuildAction(
					ImmutableList.of(
						new Scheme.BuildAction.BuildActionEntry(false, true, false, false, false, new Scheme.BuildableReference(xcodeTarget.getGlobalID(), xcodeTarget.getProductName(), xcodeTarget.getName(), "container:" + projectDirectory.getName())))),
				new Scheme.LaunchAction(new Scheme.LaunchAction.BuildableProductRunnable(new Scheme.BuildableReference(xcodeTarget.getGlobalID(), xcodeTarget.getProductName(), xcodeTarget.getName(), "container:" + projectDirectory.getName())))
			));
		}


		// Lastly, create the indexing target
		project.getTargets().addAll(xcodeProject.getTargets().stream().filter(this::isIndexableTarget).map(this::toIndexTarget).collect(Collectors.toList()));

		XcodeprojSerializer serializer = new XcodeprojSerializer(getGidGenerator().get(), project);
		NSDictionary rootObject = serializer.toPlist();
		PropertyListParser.saveAsASCII(rootObject, new File(projectDirectory, "project.pbxproj"));


		// Write the WorkspaceSettings file
		File workspaceSettingsFile = new File(projectDirectory, "project.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings");
		workspaceSettingsFile.getParentFile().mkdirs();
		FileUtils.writeStringToFile(workspaceSettingsFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
			"<plist version=\"1.0\">\n" +
			"<dict>\n" +
			"\t<key>IDEWorkspaceSharedSettings_AutocreateContextsIfNeeded</key>\n" +
			"\t<false/>\n" +
			"</dict>\n" +
			"</plist>", Charset.defaultCharset());
	}

	private boolean isIndexableTarget(XcodeIdeTarget xcodeTarget) {
		return Arrays.stream(XcodeIdeProductTypes.getKnownValues()).anyMatch(xcodeTarget.getProductType().get()::equals);
	}

	private PBXTarget toTarget(XcodeIdeTarget xcodeTarget) {
		return toGradleTarget(xcodeTarget);
	}

	private PBXTarget toGradleTarget(XcodeIdeTarget xcodeTarget) {
		PBXFileReference productReference = toBuildProductFileReference(xcodeTarget.getProductReference().get());

		PBXLegacyTarget target = new PBXLegacyTarget(xcodeTarget.getName(), xcodeTarget.getProductType().get());
		target.setProductName(xcodeTarget.getProductName().get());
		target.setBuildToolPath(getGradleCommand().get());
		target.setBuildArgumentsString(String.join(" ", Iterables.concat(XcodeIdePropertyAdapter.getAdapterCommandLine(), getAdditionalGradleArguments().get())) + " " + getBridgeTaskPath().get());
		target.setGlobalID(getGidGenerator().get().generateGid("PBXLegacyTarget", xcodeTarget.getName().hashCode()));
		target.setProductReference(productReference);

		// For now, we want unidirectional configuration of the build logic, that is Gradle -> Xcode IDE.
		// It's not impossible to allow changes to build settings inside Xcode IDE to tickle down into Gradle for a directional configuration.
		// However, there are a lot of things to consider and it's not a priority at the moment.
		// If you are a user of the Nokee plugins reading this, feel free to open an feature request with your use cases.
		target.setPassBuildSettingsInEnvironment(false);

		xcodeTarget.getBuildConfigurations().forEach(buildConfiguration -> {
			NSDictionary settings = target.getBuildConfigurationList().getBuildConfigurationsByName().getUnchecked(buildConfiguration.getName()).getBuildSettings();
			settings.put("__DO_NOT_CHANGE_ANY_VALUE_HERE__", "Instead, use the build.gradle[.kts] files.");
			for (Map.Entry<String, Object> entry : buildConfiguration.getBuildSettings().getElements().get().entrySet()) {
				settings.put(entry.getKey(), toValue(entry.getValue()));
			}

			// We use the product reference here because Xcode uses the product type on PBXNativeTarget to infer an extension.
			// It is bolted onto the product name to form the path to the file under Products group.
			// With PBXLegacyTarget Xcode ignores the product reference on the target for the path.
			// Instead, it uses the PRODUCT_NAME settings to infer the path inside the BUILT_PRODUCT_DIR.
			settings.put("PRODUCT_NAME", xcodeTarget.getProductReference().get());
		});

		return target;
	}

	private PBXTarget toIndexTarget(XcodeIdeTarget xcodeTarget) {
		PBXFileReference productReference = new PBXFileReference(xcodeTarget.getProductReference().get(), xcodeTarget.getProductReference().get(), PBXReference.SourceTree.BUILT_PRODUCTS_DIR);

		PBXNativeTarget target = new PBXNativeTarget("__indexer_" + xcodeTarget.getName(), INDEXER_PRODUCT_TYPE);
		target.setProductName(xcodeTarget.getProductName().get());
		target.getBuildPhases().add(newSourceBuildPhase(xcodeTarget.getSources()));
		target.setGlobalID(getGidGenerator().get().generateGid("PBXNativeTarget", xcodeTarget.getName().hashCode()));
		target.setProductReference(productReference);

		xcodeTarget.getBuildConfigurations().forEach(buildConfiguration -> {
			NSDictionary settings = target.getBuildConfigurationList().getBuildConfigurationsByName().getUnchecked(buildConfiguration.getName()).getBuildSettings();
			settings.put("__DO_NOT_CHANGE_ANY_VALUE_HERE__", "Instead, use the build.gradle[.kts] files.");
			for (Map.Entry<String, Object> entry : buildConfiguration.getBuildSettings().getElements().get().entrySet()) {
				settings.put(entry.getKey(), toValue(entry.getValue()));
			}

			// TODO: Set default PRODUCT_NAME if not set
		});

		return target;
	}

	private PBXSourcesBuildPhase newSourceBuildPhase(FileCollection sourceFiles) {
		PBXSourcesBuildPhase result = new PBXSourcesBuildPhase();
		for (File file : sourceFiles) {
			result.getFiles().add(new PBXBuildFile(toAbsoluteFileReference(file)));
		}
		return result;
	}

	private static class XcodeIdeBooleanSerializer extends JsonSerializer<Boolean> {
		@Override
		public void serialize(Boolean value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
			if (value) {
				jgen.writeString("YES");
			} else {
				jgen.writeString("NO");
			}
		}
	}

	private static NSObject toValue(Object o) {
		if (o instanceof Integer) {
			return new NSNumber((Integer) o);
		} else if (o instanceof Long) {
			return new NSNumber((Long) o);
		} else if (o instanceof Double) {
			return new NSNumber((Double) o);
		} else if (o instanceof Collection) {
			NSArray result = new NSArray(((Collection<?>) o).size());
			int key = 0;
			for (Object obj : (Collection<?>)o) {
				result.setValue(key, toValue(obj));
				key++;
			}
			return result;
		}
		return new NSString(o.toString());
	}

	private PBXFileReference toAbsoluteFileReference(File file) {
		return computeFileReferenceIfAbsent(file.getAbsolutePath(),
			path -> new PBXFileReference(file.getName(), file.getAbsolutePath(), PBXReference.SourceTree.ABSOLUTE));
	}

	private PBXFileReference toBuildProductFileReference(String name) {
		return computeFileReferenceIfAbsent(name,
			key -> new PBXFileReference(name, name, PBXReference.SourceTree.BUILT_PRODUCTS_DIR));
	}

	// FIXME: Multiple group using the same code is only included in one place...
	private PBXFileReference computeFileReferenceIfAbsent(String key, Function<String, PBXFileReference> provider) {
		return pathToFileReferenceMapping.computeIfAbsent(key, provider);
	}

	/**
	 * <Scheme
	 *   LastUpgradeVersion = "0830"
	 *   version = "1.3">
	 *   ...
	 * </Scheme>
	 */
	@Value
	private static class Scheme {
		@JacksonXmlProperty(localName = "BuildAction")
		BuildAction buildAction;

		@JacksonXmlProperty(localName = "TestAction")
		TestAction testAction = new TestAction();

		@JacksonXmlProperty(localName = "LaunchAction")
		LaunchAction launchAction;

		@JacksonXmlProperty(localName = "ProfileAction")
		ProfileAction profileAction = new ProfileAction();

		@JacksonXmlProperty(localName = "AnalyzeAction")
		AnalyzeAction analyzeAction = new AnalyzeAction();

		@JacksonXmlProperty(localName = "ArchiveAction")
		ArchiveAction archiveAction = new ArchiveAction();

		@JacksonXmlProperty( localName = "LastUpgradeVersion", isAttribute = true)
		public String getLastUpgradeVersion() {
			return "0830";
		}
		@JacksonXmlProperty(isAttribute = true)
		public String getVersion() {
			return "1.3";
		}

		/**
		 * <BuildAction
		 *   parallelizeBuildables = "YES"
		 *   buildImplicitDependencies = "YES">
		 *    <BuildActionEntries>
		 *    </BuildActionEntries>
		 * </BuildAction>
		 */
		@Value
		public static class BuildAction {
			@JacksonXmlElementWrapper(localName = "BuildActionEntries")
			@JacksonXmlProperty(localName = "BuildActionEntry")
			List<BuildActionEntry> buildActionEntries;

			@JacksonXmlProperty(isAttribute = true)
			public boolean getParallelizeBuildables() {
				// Gradle takes care of executing the build in parallel.
				return false;
			}

			@JacksonXmlProperty(isAttribute = true)
			public boolean getBuildImplicitDependencies() {
				// Gradle takes care of the project dependencies.
				return false;
			}

			@Value
			public static class BuildActionEntry {
				@JacksonXmlProperty(isAttribute = true)
				boolean buildForTesting;

				@JacksonXmlProperty(isAttribute = true)
				boolean buildForRunning;

				@JacksonXmlProperty(isAttribute = true)
				boolean buildForProfiling;

				@JacksonXmlProperty(isAttribute = true)
				boolean buildForArchiving;

				@JacksonXmlProperty(isAttribute = true)
				boolean buildForAnalyzing;

				@JacksonXmlProperty(localName = "BuildableReference")
				BuildableReference buildableReference;
			}
		}

		@Value
		public static class TestAction {
			@JacksonXmlElementWrapper(localName = "Testables")
			@JacksonXmlProperty(localName = "TestableReference")
			List<TestableReference> testables = new ArrayList<>();

			@JacksonXmlProperty(isAttribute = true)
			public String getBuildConfiguration() {
				return "Default";
			}

			@JacksonXmlProperty(isAttribute = true)
			public String getSelectedDebuggerIdentifier() {
				return "Xcode.DebuggerFoundation.Debugger.LLDB";
			}

			@JacksonXmlProperty(isAttribute = true)
			public String getSelectedLauncherIdentifier() {
				return "Xcode.DebuggerFoundation.Launcher.LLDB";
			}

			@JacksonXmlProperty(isAttribute = true)
			public boolean getShouldUseLaunchSchemeArgsEnv() {
				return true;
			}
			// TODO: AdditionalOptions

			/**
			 * Note: The attributes are lowerCamelCase.
			 */
			@Value
			public static class TestableReference {

			}
		}

		@Value
		public static class LaunchAction {
			@JacksonXmlProperty(localName = "BuildableProductRunnable")
			BuildableProductRunnable buildableProductRunnable;

			@JacksonXmlProperty(isAttribute = true)
			public String getBuildConfiguration() {
				return "Default";
			}
			@JacksonXmlProperty(isAttribute = true)
			public String getSelectedDebuggerIdentifier() {
				return "Xcode.DebuggerFoundation.Debugger.LLDB";
			}
			@JacksonXmlProperty(isAttribute = true)
			public String getSelectedLauncherIdentifier() {
				return "Xcode.DebuggerFoundation.Launcher.LLDB";
			}
			@JacksonXmlProperty(isAttribute = true)
			public String getLaunchStyle() {
				return "0";
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getUseCustomWorkingDirectory() {
				return false;
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getIgnoresPersistentStateOnLaunch() {
				return false;
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getDebugDocumentVersioning() {
				return true;
			}
			@JacksonXmlProperty(isAttribute = true)
			public String getDebugServiceExtension() {
				return "internal";
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getAllowLocationSimulation() {
				return true;
			}

			// TODO: AdditionalOptions

			@Value
			public static class BuildableProductRunnable {
				@JacksonXmlProperty(localName = "BuildableReference")
				BuildableReference buildableReference;

				@JacksonXmlProperty(isAttribute = true)
				public String getRunnableDebuggingMode() {
					return "0";
				}

			}
		}

		@Value
		public static class ProfileAction {
			@JacksonXmlProperty(isAttribute = true)
			public String getBuildConfiguration() {
				return "Default";
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getShouldUseLaunchSchemeArgsEnv() {
				return true;
			}
			@JacksonXmlProperty(isAttribute = true)
			public String getSavedToolIdentifier() {
				return "";
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getUseCustomWorkingDirectory() {
				return false;
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getDebugDocumentVersioning() {
				return true;
			}
		}

		@Value
		public static class AnalyzeAction {
			@JacksonXmlProperty(isAttribute = true)
			public String getBuildConfiguration() {
				return "Default";
			}
		}

		@Value
		public static class ArchiveAction {
			@JacksonXmlProperty(isAttribute = true)
			public String getBuildConfiguration() {
				return "Default";
			}
			@JacksonXmlProperty(isAttribute = true)
			public boolean getRevealArchiveInOrganizer() {
				return true;
			}
		}

		@Value
		@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
		public static class BuildableReference {
			@JacksonXmlProperty(isAttribute = true)
			String blueprintIdentifier;

			@JacksonXmlProperty(isAttribute = true)
			String buildableName;

			@JacksonXmlProperty(isAttribute = true)
			String blueprintName;

			@JacksonXmlProperty(isAttribute = true)
			String referencedContainer;

			@JacksonXmlProperty(isAttribute = true)
			public String getBuildableIdentifier() {
				return "primary";
			}
		}
	}
}
