package dev.nokee.platform.ios.internal.plugins;

import org.apache.commons.io.IOUtils;
import org.gradle.api.internal.project.ProjectIdentifier;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.NativeExecutableSpec;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.util.GUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class IosApplicationRules extends RuleSource {
	@Mutate
	public void configureToolchain(NativeToolChainRegistry toolchains) {
		toolchains.withType(Clang.class, toolchain -> {
			toolchain.eachPlatform(platform -> {
				// Although this should be correct, clearing the args to remove the -m64 (which is not technically, exactly, required in this instance) and adding the target with the correct sysroot...
				// Gradle forcefully append the macOS SDK sysroot to the configured args.
				// The sysroot used is the macOS not the iPhoneSimulator.
				// To solve this, we can reprobe the compiler right before the task executes.
				((DefaultGccPlatformToolChain)platform).getCompilerProbeArgs().clear();
				((DefaultGccPlatformToolChain)platform).getCompilerProbeArgs().addAll(Arrays.asList("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath()));
			});
		});
	}

	@Mutate
	public void createExecutable(ComponentSpecContainer components, ProjectIdentifier projectIdentifier) {
		components.create("main", NativeExecutableSpec.class, application -> {
			// TODO: This is the module name
			application.setBaseName(GUtil.toCamelCase(projectIdentifier.getName()));

			application.getBinaries().withType(NativeExecutableBinarySpec.class, binary -> {
				binary.getObjcCompiler().args("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath(), "-fobjc-arc");
				binary.getLinker().args("-target", "x86_64-apple-ios13.2-simulator", "-isysroot", getSdkPath(),
					"-Xlinker", "-rpath", "-Xlinker", "@executable_path/Frameworks",
					"-Xlinker", "-export_dynamic",
					"-Xlinker", "-no_deduplicate",
					"-Xlinker", "-objc_abi_version", "-Xlinker", "2",
//					"-Xlinker", "-sectcreate", "-Xlinker", "__TEXT", "-Xlinker", "__entitlements", "-Xlinker", createEntitlementTask.get().outputFile.get().asFile.absolutePath
					"-lobjc", "-framework", "UIKit", "-framework", "Foundation"
				);
			});
		});
	}

	private static String getSdkPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--show-sdk-path").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
