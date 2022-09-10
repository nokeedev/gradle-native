/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.ide.xcode.internal.tasks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.XcodeIdeProductType;
import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.DefaultXcodeIdeBuildSettings;
import dev.nokee.ide.xcode.internal.XcodeIdePropertyAdapter;
import dev.nokee.ide.xcode.internal.services.XcodeIdeGidGeneratorService;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXSourcesBuildPhase;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.objects.targets.ProductTypes;
import dev.nokee.xcode.project.PBXObjectArchiver;
import dev.nokee.xcode.project.PBXObjectReference;
import dev.nokee.xcode.project.PBXProjWriter;
import dev.nokee.xcode.scheme.XCScheme;
import dev.nokee.xcode.scheme.XCSchemeWriter;
import dev.nokee.xcode.workspace.WorkspaceSettings;
import dev.nokee.xcode.workspace.WorkspaceSettingsWriter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.internal.tasks.TaskDependencyContainer;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

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

	@Inject
	public GenerateXcodeIdeProjectTask(XcodeIdeProject xcodeProject) {
		this.xcodeProject = xcodeProject;
		// Workaround for https://github.com/gradle/gradle/issues/13405, this really sucks...
		this.dependsOn(new TaskDependencyContainer() {
			@Override
			public void visitDependencies(TaskDependencyResolveContext context) {
				xcodeProject.getTargets().stream().flatMap(it -> it.getBuildConfigurations().stream()).flatMap(it -> ((DefaultXcodeIdeBuildSettings)it.getBuildSettings()).getProviders().stream()).forEach(it -> {
					if (it instanceof TaskDependencyContainer) {
						((TaskDependencyContainer) it).visitDependencies(context);
					}
				});
			}
		});
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	// TODO: Ensure that half generated projects are deleted (avoid leaking files)
	@TaskAction
	private void generate() throws IOException {
		File projectDirectory = getProjectLocation().get().getAsFile();
		FileUtils.deleteDirectory(projectDirectory);
		projectDirectory.mkdirs();

		PBXProject.Builder projectBuilder = PBXProject.builder();

		// Convert all Gradle Xcode IDE targets to PBXTarget
		xcodeProject.getTargets().stream().map(this::toTarget).forEach(projectBuilder::target);

		// Create all build configuration at the project level, Xcode expect that.
		projectBuilder.buildConfigurations(builder -> {
			xcodeProject.getTargets().stream().flatMap(it -> it.getBuildConfigurations().stream()).map(XcodeIdeBuildConfiguration::getName).distinct().forEach(name -> {
				builder.buildConfiguration(it -> it.name(name));
			});
		});

		// Configure sources
		xcodeProject.getSources().forEach(file -> {
			projectBuilder.file(toAbsoluteFileReference(file));
		});
		xcodeProject.getGroups().forEach(group -> {
			projectBuilder.group(builder -> {
				builder.name(group.getName());
				group.getSources().forEach(file -> builder.child(toAbsoluteFileReference(file)));
			});
		});

		// Add all target product reference to Products source group
		projectBuilder.group(builder -> {
			builder.name(PRODUCTS_GROUP_NAME);
			xcodeProject.getTargets().stream().map(it -> pathToFileReferenceMapping.get(it.getProductReference().get())).forEach(builder::child);
		});

		// Lastly, create the indexing target
		xcodeProject.getTargets().stream().filter(this::isIndexableTarget).map(this::toIndexTarget).forEach(projectBuilder::target);

		PBXProject project = projectBuilder.build();

		// Convert to PBXProj model
		val pbxproj = new PBXObjectArchiver(getGidGenerator().get()).encode(project);


		// Do the schemes... using PBXProj model as it has GlobalIDs
		File schemesDirectory = new File(projectDirectory, "xcshareddata/xcschemes");
		schemesDirectory.mkdirs();
		pbxproj.getObjects().stream().filter(this::isPBXTarget).filter(this::notTestingOrIndexingTarget).forEach(targetRef -> {
			ImmutableList.Builder<XCScheme.BuildAction.BuildActionEntry> buildActionBuilder = ImmutableList.builder();
			buildActionBuilder.add(new XCScheme.BuildAction.BuildActionEntry(false, true, false, false, false, newBuildableReference(targetRef)));

			ImmutableList.Builder<XCScheme.TestAction.TestableReference> testActionBuilder = ImmutableList.builder();

			pbxproj.getObjects().stream().filter(this::isPBXTarget).filter(this::isTestingTarget).forEach(it -> {
				buildActionBuilder.add(new XCScheme.BuildAction.BuildActionEntry(true, false, false, false, false, newBuildableReference(it)));

				testActionBuilder.add(new XCScheme.TestAction.TestableReference(newBuildableReference(it)));
			});

			try (val writer = new XCSchemeWriter(new FileWriter(new File(schemesDirectory, targetRef.getFields().get("name") + ".xcscheme")))) {
				writer.write(new XCScheme(
					new XCScheme.BuildAction(buildActionBuilder.build()),
					new XCScheme.TestAction(testActionBuilder.build()),
					new XCScheme.LaunchAction(XcodeIdeProductType.of(targetRef.getFields().get("productType").toString()).equals(XcodeIdeProductTypes.DYNAMIC_LIBRARY) ? null : new XCScheme.LaunchAction.BuildableProductRunnable(newBuildableReference(targetRef)))
				));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});

		// Write the PBXProj file
		try (val writer = new PBXProjWriter(new FileWriter(new File(projectDirectory, "project.pbxproj")))) {
			writer.write(pbxproj);
		}

		// Write the WorkspaceSettings file
		File workspaceSettingsFile = new File(projectDirectory, "project.xcworkspace/xcshareddata/WorkspaceSettings.xcsettings");
		workspaceSettingsFile.getParentFile().mkdirs();
		try (val writer = new WorkspaceSettingsWriter(new FileWriter(workspaceSettingsFile))) {
			writer.write(WorkspaceSettings.builder().put(WorkspaceSettings.AutoCreateSchemes.Disabled).build());
		}
	}

	private boolean notTestingOrIndexingTarget(PBXObjectReference reference) {
		return !reference.getFields().get("productType").equals(XcodeIdeProductTypes.UNIT_TEST.toString())
			&& !reference.getFields().get("productType").equals(XcodeIdeProductTypes.UI_TEST.toString())
			&& !reference.getFields().get("productType").equals(INDEXER_PRODUCT_TYPE.toString());
	}

	private boolean isTestingTarget(PBXObjectReference reference) {
		return reference.getFields().get("productType").equals(XcodeIdeProductTypes.UNIT_TEST.toString())
			|| reference.getFields().get("productType").equals(XcodeIdeProductTypes.UI_TEST.toString());
	}

	public boolean isPBXTarget(PBXObjectReference reference) {
		return reference.isa().equals(PBXLegacyTarget.class.getSimpleName())
			|| reference.isa().equals(PBXNativeTarget.class.getSimpleName());
	}

	private boolean isTestingProductType(XcodeIdeProductType productType) {
		return productType.equals(XcodeIdeProductTypes.UNIT_TEST) || productType.equals(XcodeIdeProductTypes.UI_TEST);
	}

	private XCScheme.BuildableReference newBuildableReference(PBXObjectReference xcodeTarget) {
		return new XCScheme.BuildableReference(xcodeTarget.getGlobalID(), xcodeTarget.getFields().get("productName").toString(), xcodeTarget.getFields().get("name").toString(), "container:" + getProjectLocation().get().getAsFile().getName());
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
		PBXNativeTarget.Builder targetBuilder = PBXNativeTarget.builder();
		targetBuilder.name(xcodeTarget.getName());
		targetBuilder.productType(ProductTypes.valueOf(xcodeTarget.getProductType().get().toString()));

		// Configure build phases
		targetBuilder.buildPhase(newGradleBuildPhase());
		// Tulsi integration uses a script phase here to generate the dependency files, we use a Gradle task action instead.
		// See XcodeIdeObjectiveCIosApplicationPlugin
		targetBuilder.buildPhase(newSourcesBuildPhase(xcodeTarget.getSources()));

		targetBuilder.productName(xcodeTarget.getProductName().get());

		// Configures the product reference.
		// We only configure the .xctest, the -Runner.app and co. are an implementation detail.
		PBXFileReference productReference = pathToFileReferenceMapping.computeIfAbsent(xcodeTarget.getProductReference().get(), ignored -> new PBXFileReference(xcodeTarget.getProductReference().get(), xcodeTarget.getProductReference().get(), PBXSourceTree.BUILT_PRODUCTS_DIR));
		targetBuilder.productReference(productReference);

		targetBuilder.buildConfigurations(builder -> {
			xcodeTarget.getBuildConfigurations().forEach(buildConfiguration -> {
				// TODO: Set default PRODUCT_NAME if not set
				builder.buildConfiguration(buildConfigBuilder -> {
					buildConfigBuilder.name(buildConfiguration.getName()).buildSettings(settings -> {
						settings.put("__DO_NOT_CHANGE_ANY_VALUE_HERE__", "Instead, use the build.gradle[.kts] files.");

						for (Map.Entry<String, Object> entry : buildConfiguration.getBuildSettings().getElements().get().entrySet()) {
							settings.put(entry.getKey(), entry.getValue());
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
//						// Disable Xcode's signing as the applications are already signed by Nokee.
//						settings.put("CODE_SIGNING_REQUIRED", "NO");
//						settings.put("CODE_SIGN_IDENTITY", "");

						// TODO: This is most likely not required
						// Explicitly setting the FRAMEWORK_SEARCH_PATHS will allow Xcode to resolve references to the
						// XCTest framework when performing Live issues analysis.
//						settings.put("FRAMEWORK_SEARCH_PATHS", "$(PLATFORM_DIR)/Developer/Library/Frameworks");

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
				});
			});
		});

		return targetBuilder.build();
	}

	private PBXTarget toIndexTarget(XcodeIdeTarget xcodeTarget) {
		PBXFileReference productReference = new PBXFileReference(xcodeTarget.getProductReference().get(), xcodeTarget.getProductReference().get(), PBXSourceTree.BUILT_PRODUCTS_DIR);

		PBXNativeTarget.Builder targetBuilder = PBXNativeTarget.builder();
		targetBuilder.name("__indexer_" + xcodeTarget.getName());
		targetBuilder.productType(ProductType.of(INDEXER_PRODUCT_TYPE.toString(), null));
		targetBuilder.productName(xcodeTarget.getProductName().get());
		targetBuilder.buildPhase(newSourcesBuildPhase(xcodeTarget.getSources()));
		targetBuilder.productReference(productReference);

		targetBuilder.buildConfigurations(builder -> {
			xcodeTarget.getBuildConfigurations().forEach(buildConfiguration -> {
				builder.buildConfiguration(buildConfigBuilder -> {
					buildConfigBuilder.name(buildConfiguration.getName()).buildSettings(settings -> {
						settings.put("__DO_NOT_CHANGE_ANY_VALUE_HERE__", "Instead, use the build.gradle[.kts] files.");
						for (Map.Entry<String, Object> entry : buildConfiguration.getBuildSettings().getElements().get().entrySet()) {
							settings.put(entry.getKey(), entry.getValue());
						}

						// TODO: Set default PRODUCT_NAME if not set
					});
				});
			});
		});

		return targetBuilder.build();
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
		PBXSourcesBuildPhase.Builder builder = PBXSourcesBuildPhase.builder();
		for (File file : sourceFiles.filter(GenerateXcodeIdeProjectTask::keepingOnlyCompilationUnits)) {
			builder.file(PBXBuildFile.ofFile(toAbsoluteFileReference(file)));
		}
		return builder.build();
	}
	private static Set<String> COMPILATION_UNITS_EXTENSIONS = ImmutableSet.<String>builder()
		.add("m")
		.add("cp", "cpp", "c++", "cc", "cxx")
		.add("c")
		.add("mm")
		.add("swift")
		.build();
	private static boolean keepingOnlyCompilationUnits(File sourceFile) {
		return COMPILATION_UNITS_EXTENSIONS.contains(FilenameUtils.getExtension(sourceFile.getName()));
	}

	private PBXTarget toGradleTarget(XcodeIdeTarget xcodeTarget) {
		PBXFileReference productReference = toBuildProductFileReference(xcodeTarget.getProductReference().get());

		PBXLegacyTarget.Builder targetBuilder = PBXLegacyTarget.builder();
		targetBuilder.name(xcodeTarget.getName());
		targetBuilder.productType(toProductType(xcodeTarget.getProductType().get().toString()));
		targetBuilder.productName(xcodeTarget.getProductName().get());
		targetBuilder.buildToolPath(getGradleCommand().get());
		targetBuilder.buildArguments(getGradleBuildArgumentsString());
		targetBuilder.productReference(productReference);

		// For now, we want unidirectional configuration of the build logic, that is Gradle -> Xcode IDE.
		// It's not impossible to allow changes to build settings inside Xcode IDE to tickle down into Gradle for a directional configuration.
		// However, there are a lot of things to consider and it's not a priority at the moment.
		// If you are a user of the Nokee plugins reading this, feel free to open an feature request with your use cases.
		targetBuilder.passBuildSettingsInEnvironment(false);

		// For now, we want to pass the build settings in environment **only** so SDKROOT is picked up by clang.
		// Note that we keep the previous setting as this override is a simple workaround.
		//   See https://github.com/nokeedev/gradle-native/issues/334
		targetBuilder.passBuildSettingsInEnvironment(true);

		targetBuilder.buildConfigurations(builder -> {
			xcodeTarget.getBuildConfigurations().forEach(buildConfiguration -> {
				builder.buildConfiguration(buildConfigBuilder -> {
					buildConfigBuilder.name(buildConfiguration.getName()).buildSettings(settings -> {
						settings.put("__DO_NOT_CHANGE_ANY_VALUE_HERE__", "Instead, use the build.gradle[.kts] files.");
						for (Map.Entry<String, Object> entry : buildConfiguration.getBuildSettings().getElements().get().entrySet()) {
							settings.put(entry.getKey(), entry.getValue());
						}

						// We use the product reference here because Xcode uses the product type on PBXNativeTarget to infer an extension.
						// It is bolted onto the product name to form the path to the file under Products group.
						// With PBXLegacyTarget Xcode ignores the product reference on the target for the path.
						// Instead, it uses the PRODUCT_NAME settings to infer the path inside the BUILT_PRODUCT_DIR.
						settings.put("PRODUCT_NAME", xcodeTarget.getProductReference().get());

						// For now, lets always assume macosx as SDKROOT.
						// Later, Gradle should handle the sdk and sysroot properly.
						//   See https://github.com/nokeedev/gradle-native/issues/334
						settings.put("SDKROOT", "macosx");
					});
				});
			});
		});

		return targetBuilder.build();
	}

	private static ProductType toProductType(String identifier) {
		for (ProductType value : ProductTypes.values()) {
			if (value.getIdentifier().equals(identifier)) {
				return value;
			}
		}
		return ProductType.of(identifier, null);
	}

	private PBXShellScriptBuildPhase newGradleBuildPhase() {
		PBXShellScriptBuildPhase.Builder builder = PBXShellScriptBuildPhase.builder();

		// Gradle startup script is sh compatible.
		builder.shellPath("/bin/sh");

		// We nullify the stdin as Xcode console is non-interactive.
		builder.shellScript("exec \"" + getGradleCommand().get() + "\" " + getGradleBuildArgumentsString() + " < /dev/null");

		// When using a native target, Xcode process the Info.plist files to the same destination than Nokee.
		// To ensure we always use Nokee's artifact, we use the Info.plist as an input which force Xcode process the Info.plist before us.
		builder.inputPaths(ImmutableList.of("$(TARGET_BUILD_DIR)/$(INFOPLIST_PATH)"));

		return builder.build();
	}
	private String getGradleBuildArgumentsString() {
		return String.join(" ", Iterables.concat(XcodeIdePropertyAdapter.getAdapterCommandLine(), getAdditionalGradleArguments().get())) + " " + XcodeIdePropertyAdapter.adapt("GRADLE_IDE_PROJECT_NAME", xcodeProject.getName()) + " " + getBridgeTaskPath().get();
	}

	private PBXFileReference toAbsoluteFileReference(File file) {
		return computeFileReferenceIfAbsent(file.getAbsolutePath(), PBXFileReference::ofAbsolutePath);
	}

	private PBXFileReference toBuildProductFileReference(String name) {
		return computeFileReferenceIfAbsent(name,
			key -> new PBXFileReference(name, name, PBXSourceTree.BUILT_PRODUCTS_DIR));
	}

	// FIXME: Multiple group using the same code is only included in one place...
	private PBXFileReference computeFileReferenceIfAbsent(String key, Function<String, PBXFileReference> provider) {
		return pathToFileReferenceMapping.computeIfAbsent(key, provider);
	}

}
