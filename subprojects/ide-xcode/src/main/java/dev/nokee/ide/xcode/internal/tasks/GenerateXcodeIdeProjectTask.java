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

		for (PBXTarget xcodeTarget : project.getTargets().stream().filter(it -> !isTestingTarget(it)).collect(Collectors.toList())) {
			ImmutableList.Builder<Scheme.BuildAction.BuildActionEntry> buildActionBuilder = ImmutableList.builder();
			buildActionBuilder.add(new Scheme.BuildAction.BuildActionEntry(false, true, false, false, false, newBuildableReference(xcodeTarget)));

			ImmutableList.Builder<Scheme.TestAction.TestableReference> testActionBuilder = ImmutableList.builder();

			project.getTargets().stream().filter(this::isTestingTarget).forEach(it -> {
				buildActionBuilder.add(new Scheme.BuildAction.BuildActionEntry(true, false, false, false, false, newBuildableReference(it)));

				testActionBuilder.add(new Scheme.TestAction.TestableReference(newBuildableReference(it)));
			});

			xmlMapper.writeValue(new File(schemesDirectory, xcodeTarget.getName() + ".xcscheme"), new Scheme(
				new Scheme.BuildAction(buildActionBuilder.build()),
				new Scheme.TestAction(testActionBuilder.build()),
				new Scheme.LaunchAction(new Scheme.LaunchAction.BuildableProductRunnable(newBuildableReference(xcodeTarget)))
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

	private boolean isTestingTarget(PBXTarget xcodeTarget) {
		return isTestingProductType(xcodeTarget.getProductType());
	}

	private boolean isTestingProductType(XcodeIdeProductType productType) {
		return productType.equals(XcodeIdeProductTypes.UNIT_TEST) || productType.equals(XcodeIdeProductTypes.UI_TEST);
	}

	private Scheme.BuildableReference newBuildableReference(PBXTarget xcodeTarget) {
		return new Scheme.BuildableReference(xcodeTarget.getGlobalID(), xcodeTarget.getProductName(), xcodeTarget.getName(), "container:" + getProjectLocation().get().getAsFile().getName());
	}

	private boolean isIndexableTarget(XcodeIdeTarget xcodeTarget) {
		// TODO: Use a white list of target instead of all known values
		return !xcodeTarget.getProductType().get().equals(XcodeIdeProductTypes.UNIT_TEST) && !xcodeTarget.getProductType().get().equals(XcodeIdeProductTypes.UI_TEST) && Arrays.stream(XcodeIdeProductTypes.getKnownValues()).anyMatch(xcodeTarget.getProductType().get()::equals);
	}

	private PBXTarget toTarget(XcodeIdeTarget xcodeTarget) {
		if (isTestingProductType(xcodeTarget.getProductType().get())) {
			return toGradleXCTestTarget(xcodeTarget);
		}
		return toGradleTarget(xcodeTarget);
	}

	/**
	 * = Generating XCTest Target (Unit and UI testing)
	 * It is required to use native targets to integrate XCTest components with Xcode to allow a _vanilla_ test experience.
	 * The _vanilla_ test experience is defined by the auto-discovering tests.
	 * The tests can then be interacted with via the Test navigator tab and inline with the code via the side dot.
	 *
	 * == Xcode love triangle
	 * The native targets pose certain limitation which creates a love triangle with the following edges:
	 * 1- The _vanilla_ test experience
	 * 2- The code indexing
	 * 3- The delegation to Gradle
	 *
	 * === 1- The _vanilla_ test experience
	 * Xcode provide a nice testing experience only in the presence of a native target with the test product types (e.g. ui-testing/unit-test).
	 * Using a com.sun.tools.javac.resources.legacy target to allow org.gradle.api.invocation.Gradle delegation won't work here.
	 * The test target also needs to be part of a scheme.
	 * Typically, the tests are part of the same scheme as the tested component.
	 * For example, if we are testing an iOS application, the tests would be part of the iOS application scheme.
	 *
	 * === 2- The code indexing
	 * Xcode uses SourceKit to index the code on the fly as well as `-index-store-path` flag for indexing during compilation.
	 * On the fly indexing only works for native targets and requires a compile source build phase to be configured.
	 * SourceKit will build the compile flags based on the target's build configuration.
	 * When delegating to Gradle via legacy targets, we can add a matching native target for indexing.
	 * This works as Xcode will ignore the legacy target and will use the native target for indexing.
	 * As we reference the legacy target for building and launching, the indexing target is only ever used by SourceKit.
	 * It never invoke the compile source build phased configured on the native target for indexing.
	 * For testing target, the same strategy won't work as Xcode will use the target providing _vanilla_ test experience.
	 * It will ignore the matching indexing target is present.
	 * It is not a problem in itself, but it's becomes an issue when delegating to Gradle.
	 * We have to use the native target for building in the scheme.
	 * Xcode will end up invoking the compile source build phase.
	 *
	 * === 3- The delegation to Gradle
	 * There are two different ways to delegate to Gradle.
	 * The first one is via legacy targets.
	 * The second one is to use a script build phase on a native target.
	 * Both works more or less the same.
	 * A "script" is written that just delegate to Gradle via a bridge task.
	 *
	 * == Additional problems
	 * === Keeping SourceKit active
	 * Some integration would use two different build configurations: the original one and a prefixed one, i.e. __NokeeTestRunner_.
	 * The original one would be used for indexing and never referenced while the other one would disable the compile source phase by setting
	 * `OTHER_LDFLAGS`, `OTHER_CFLAGS`, and `OTHER_SWIFT_FLAGS` build settings with `--version` (less output) or `-help` (more output).
	 * When using the target inside the tested target scheme, the UI test were failing due to a permission issue.
	 * (NOTE: I looked at everything and couldn't figure out why it was happening)
	 * It seems the other integration are using separate schemes for the test target which may work around the issue.
	 * Regardless, it's also troublesome during Gradle delegation as the BUILT_PRODUCT_DIR value needs to be adjusted.
	 * The received value points at the original build configuration and the product needs to be copied to the prefixed build configuration.
	 * It is also worth nothing the prefixed build configuration *AND* the build settings configuration are needed.
	 * Only using the build settings configuration trick will disable SourceKit as it seems to interpret the `--version` or `-help` flags.
	 * SourceKit won't produce anything useful and jumping to definition for #import/#include won't work.
	 *
	 * === Xcode build process
	 * Xcode implies additional steps during compile source build phase which can clash with Gradle.
	 * For example, it will process the Info.plist files and copy it to the final location where Gradle copies the product that it built.
	 * Care must be taken to ensure the file is processed by Xcode before Gradle copies it's product.
	 * Xcode will also sign, copy Swift stdlib, generate debug symbols, copy frameworks into the bundle, etc.
	 * Some of those steps can be disabled via more build settings.
	 *
	 * == Solution
	 * What's the problem then?
	 * We need:
	 * - a native target (for _vanilla_ test experience),
	 * - configured with both a compile source (for indexing) and script (for Gradle delegation) build phase
	 * - where the compile source build phase is somehow ignored to avoid 1) duplicated work and 2) clashing with Gradle's work
	 * - while allowing tests to execute properly.
	 *
	 * The decision for the Nokee plugins is to:
	 * - use a native target (for _vanilla_ test experience),
	 * - configure a script build phase (for Gradle delegation),
	 * - configure a compile source build phase (for indexing)
	 * - configure undocumented compiler/linker build settings to disable the indexing build phase while keeping SourceKit active.
	 * - add test target to the tested target scheme
	 * - disable as much as possible the *normal* Xcode build process
	 * The undocumented build settings are `CC`, `LD`, `CPLUSPLUS`, and `LDPLUSPLUS`.
	 * See http://lists.llvm.org/pipermail/cfe-dev/2014-March/035816.html
	 * The build settings for the Swift compiler is an open question.
	 * (NOTE: I suggest going after DevToolsCore.framework and dump the strings to identify all possible build settings.)
	 */
	private PBXTarget toGradleXCTestTarget(XcodeIdeTarget xcodeTarget) {
		PBXNativeTarget target = new PBXNativeTarget(xcodeTarget.getName(), xcodeTarget.getProductType().get());

		// Configure build phases
		target.getBuildPhases().add(newGradleBuildPhase());
		// Tulsi integration uses a script phase here to generate the dependency files, we use a Gradle task action instead.
		// See XcodeIdeObjectiveCIosApplicationPlugin
		target.getBuildPhases().add(newSourcesBuildPhase(xcodeTarget.getSources()));

		target.setProductName(xcodeTarget.getProductName().get());
		target.setGlobalID(getGidGenerator().get().generateGid("PBXNativeTarget", xcodeTarget.getName().hashCode()));

		// Configures the product reference.
		// We only configure the .xctest, the -Runner.app and co. are an implementation detail.
		PBXFileReference productReference = new PBXFileReference(xcodeTarget.getProductReference().get(), xcodeTarget.getProductReference().get(), PBXReference.SourceTree.BUILT_PRODUCTS_DIR);
		target.setProductReference(productReference);

		xcodeTarget.getBuildConfigurations().forEach(buildConfiguration -> {
			// TODO: Set default PRODUCT_NAME if not set
			NSDictionary settings = target.getBuildConfigurationList().getBuildConfigurationsByName().getUnchecked(buildConfiguration.getName()).getBuildSettings();
			settings.put("__DO_NOT_CHANGE_ANY_VALUE_HERE__", "Instead, use the build.gradle[.kts] files.");

			for (Map.Entry<String, Object> entry : buildConfiguration.getBuildSettings().getElements().get().entrySet()) {
				settings.put(entry.getKey(), toValue(entry.getValue()));
			}

			// Prevent Xcode from attempting to create a fat binary with lipo from artifacts that were
			// never generated by the linker nop's.
			settings.put("ONLY_ACTIVE_ARCH", "YES");

			// Fixes an Xcode "Upgrade to recommended settings" warning. Technically the warning only
			// requires this to be added to the Debug build configuration but as code is never compiled
			// anyway it doesn't hurt anything to set it on all configs.
			settings.put("ENABLE_TESTABILITY", "YES");

			// Assume sources are ARC by default and uses per-file flags to override the default.
			settings.put("CLANG_ENABLE_OBJC_ARC", "YES");

			// FIXME: We rely on Xcode signing capability.
			//  When Nokee plugin can replace signing from Xcode, we should prevent Xcode from signing.
			//  We should also move the delegate build phase after the source compile build phase.
//			// Disable Xcode's signing as the applications are already signed by Nokee.
//			settings.put("CODE_SIGNING_REQUIRED", "NO");
//			settings.put("CODE_SIGN_IDENTITY", "");

			// TODO: This is most likely not required
			// Explicitly setting the FRAMEWORK_SEARCH_PATHS will allow Xcode to resolve references to the
			// XCTest framework when performing Live issues analysis.
//			settings.put("FRAMEWORK_SEARCH_PATHS", "$(PLATFORM_DIR)/Developer/Library/Frameworks");

			// Prevent Xcode from replacing the Swift StdLib dylibs already packaged by Nokee.
			settings.put("DONT_RUN_SWIFT_STDLIB_TOOL", "YES");

			// Disable Xcode's attempts at generating dSYM bundles as it conflicts with the operation of the
			// special test runner build configurations (which have associated sources but don't actually
			// compile anything).
			settings.put("DEBUG_INFORMATION_FORMAT", "dwarf");

			// Disable compilers/linkers by using a command that will accept all flags and return a successful exit code.
			settings.put("CC", "true");
			settings.put("LD", "true");
			settings.put("CPLUSPLUS", "true");
			settings.put("LDPLUSPLUS", "true");
		});

		return target;
	}

	private PBXTarget toIndexTarget(XcodeIdeTarget xcodeTarget) {
		PBXFileReference productReference = new PBXFileReference(xcodeTarget.getProductReference().get(), xcodeTarget.getProductReference().get(), PBXReference.SourceTree.BUILT_PRODUCTS_DIR);

		PBXNativeTarget target = new PBXNativeTarget("__indexer_" + xcodeTarget.getName(), INDEXER_PRODUCT_TYPE);
		target.setProductName(xcodeTarget.getProductName().get());
		target.getBuildPhases().add(newSourcesBuildPhase(xcodeTarget.getSources()));
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

	/**
	 * Create a new sources build phase.
	 * Sources build phase should only include compilation units.
	 * When including other type of files, there will be issues with the indexing.
	 *
	 * For indexer target, the behaviour seems to be inconsistent indexing of the files.
	 * For example, some files will allows following #import/#include while others won't.
	 * There is not consistency between each compilation units.
	 * (NOTE: I noted the first file after clearing the derived data would properly follow the #import/#include while the other files wouldn't.)
	 * (      Sometime, two of the three files would behave properly.)
	 *
	 * For XCTest targets, including the Info.plist file would cause the build to fail because of duplicated entries:
	 * one entry in the sources build phase and one entry implied by the `INFOPLIST_FILE` build setting.
	 *
	 * @param sourceFiles All source files for the target.
	 * @return a new sources build phase, never null.
	 */
	private PBXSourcesBuildPhase newSourcesBuildPhase(FileCollection sourceFiles) {
		PBXSourcesBuildPhase result = new PBXSourcesBuildPhase();
		for (File file : sourceFiles.filter(GenerateXcodeIdeProjectTask::keepingOnlyCompilationUnits)) {
			result.getFiles().add(new PBXBuildFile(toAbsoluteFileReference(file)));
		}
		return result;
	}
	private static boolean keepingOnlyCompilationUnits(File sourceFile) {
		return sourceFile.getName().endsWith(".m");
	}

	private PBXTarget toGradleTarget(XcodeIdeTarget xcodeTarget) {
		PBXFileReference productReference = toBuildProductFileReference(xcodeTarget.getProductReference().get());

		PBXLegacyTarget target = new PBXLegacyTarget(xcodeTarget.getName(), xcodeTarget.getProductType().get());
		target.setProductName(xcodeTarget.getProductName().get());
		target.setBuildToolPath(getGradleCommand().get());
		target.setBuildArgumentsString(getGradleBuildArgumentsString());
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
	private PBXShellScriptBuildPhase newGradleBuildPhase() {
		PBXShellScriptBuildPhase result = new PBXShellScriptBuildPhase();

		// Gradle startup script is sh compatible.
		result.setShellPath("/bin/sh");

		// We nullify the stdin as Xcode console is non-interactive.
		result.setShellScript("exec \"" + getGradleCommand().get() + "\" " + getGradleBuildArgumentsString() + " < /dev/null");

		// When using a native target, Xcode process the Info.plist files to the same destination than Nokee.
		// To ensure we always use Nokee's artifact, we use the Info.plist as an input which force Xcode process the Info.plist before us.
		result.getInputPaths().add("$(TARGET_BUILD_DIR)/$(INFOPLIST_PATH)");

		return result;
	}
	private String getGradleBuildArgumentsString() {
		return String.join(" ", Iterables.concat(XcodeIdePropertyAdapter.getAdapterCommandLine(), getAdditionalGradleArguments().get())) + " " + getBridgeTaskPath().get();
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
		TestAction testAction;

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
			List<TestableReference> testables;

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
				@JacksonXmlProperty(localName = "BuildableReference")
				BuildableReference buildableReference;

				@JacksonXmlProperty(isAttribute = true)
				public boolean getSkipped() {
					return false;
				}
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
