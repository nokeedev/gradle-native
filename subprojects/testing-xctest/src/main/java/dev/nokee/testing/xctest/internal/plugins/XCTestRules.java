package dev.nokee.testing.xctest.internal.plugins;

import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectIdentifier;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.NativeExecutableSpec;
import org.gradle.nativeplatform.tasks.LinkExecutable;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.util.GUtil;

import java.util.stream.Collectors;

import static dev.nokee.platform.ios.internal.plugins.IosApplicationRules.getSdkPath;
import static dev.nokee.testing.xctest.internal.plugins.ObjectiveCXCTestTestSuitePlugin.getSdkPlatformPath;

public class XCTestRules extends RuleSource {
	@Mutate
	public void linkMachOBundles(NativeToolChainRegistry toolChains) {
		toolChains.withType(Clang.class, toolChain -> {
			toolChain.eachPlatform(platform -> {
				platform.getLinker().withArguments(args -> {
					// We need to be careful as to only change the XCTest compilation into bundles.
					// We can't use NativeLibrarySpec as binary names has the `lib` prefix and `.dylib` suffix.
					if (args.stream().anyMatch(it -> it.endsWith("UnitTest") || it.endsWith("UiTest"))) {
						args.add("-bundle");
					}
				});
			});
		});
	}

	 @Mutate
	public void registerXctestComponents(ComponentSpecContainer components, ProjectIdentifier projectIdentifier) {
		components.create("unitTest", NativeExecutableSpec.class, configureXcTest(projectIdentifier, "UnitTest"));

		components.create("uiTest", NativeExecutableSpec.class, configureXcTest(projectIdentifier, "UiTest"));
	}

	@Mutate
	public void configureLinkedBundle(TaskContainer tasks, ComponentSpecContainer components) {
		tasks.named("createUnitTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
			task.getSources().from(components.withType(NativeExecutableSpec.class).get("unitTest").getBinaries().withType(NativeExecutableBinarySpec.class).values().stream().map(it -> ((LinkExecutable)it.getTasks().getLink()).getLinkedFile()).collect(Collectors.toList()));
		});

		tasks.named("createUiTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
			task.getSources().from(components.withType(NativeExecutableSpec.class).get("uiTest").getBinaries().withType(NativeExecutableBinarySpec.class).values().stream().map(it -> ((LinkExecutable)it.getTasks().getLink()).getLinkedFile()).collect(Collectors.toList()));
		});
	}

	private Action<NativeExecutableSpec> configureXcTest(ProjectIdentifier projectIdentifier, String suffix) {
		return unitTest -> {
			// TODO: This is the module name
			unitTest.setBaseName(GUtil.toCamelCase(projectIdentifier.getName()) + suffix);

			unitTest.getBinaries().withType(NativeExecutableBinarySpec.class, binary -> {
				// Can't differ the argument to the task so we will just disable it.
				if (SystemUtils.IS_OS_MAC) {
					binary.getObjcCompiler().args("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath(), "-iframework", getSdkPlatformPath() + "/Developer/Library/Frameworks");
					binary.getLinker().args("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath(),
						"-Xlinker", "-rpath", "-Xlinker", "@executable_path/Frameworks",
						"-Xlinker", "-rpath", "-Xlinker", "@loader_path/Frameworks",
						"-Xlinker", "-export_dynamic",
						"-Xlinker", "-no_deduplicate",
						"-Xlinker", "-objc_abi_version", "-Xlinker", "2",
//						"-Xlinker", "-sectcreate", "-Xlinker", "__TEXT", "-Xlinker", "__entitlements", "-Xlinker", createEntitlementTask.get().outputFile.get().asFile.absolutePath
						"-fobjc-arc", "-fobjc-link-runtime",
						"-bundle_loader", ((Project) projectIdentifier).file("build/exe/main/" + GUtil.toCamelCase(projectIdentifier.getName())).getAbsolutePath(),
						"-lobjc", "-L" + getSdkPlatformPath() + "/Developer/usr/lib", "-F" + getSdkPlatformPath() + "/Developer/Library/Frameworks", "-framework", "XCTest"
					);
				}
			});
		};
	}
}
