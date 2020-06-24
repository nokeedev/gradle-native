package dev.nokee.testing.xctest.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.internal.PathAwareCommandLineTool;
import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.VariantProvider;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.ios.tasks.internal.CreateIosApplicationBundleTask;
import dev.nokee.platform.ios.tasks.internal.ProcessPropertyListTask;
import dev.nokee.platform.ios.tasks.internal.SignIosApplicationBundleTask;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.internal.BundleBinaryInternal;
import dev.nokee.testing.xctest.tasks.internal.CreateIosXCTestBundleTask;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public abstract class DefaultUnitTestXCTestTestSuiteComponent extends BaseXCTestTestSuiteComponent implements Component {

	@Inject
	public DefaultUnitTestXCTestTestSuiteComponent(NamingScheme names) {
		super(names);
	}

	@Override
	protected void onEachVariant(BuildVariant buildVariant, VariantProvider<DefaultXCTestTestSuiteVariant> variant, NamingScheme names) {
		super.onEachVariant(buildVariant, variant, names);

		variant.configure(testSuite -> {
			testSuite.getBinaries().configureEach(BundleBinary.class, binary -> {
				((BundleBinaryInternal)binary).getBaseName().set(names.getBaseName().getAsCamelCase());
			});
			String moduleName = testSuite.getNames().getBaseName().getAsCamelCase();

			// XCTest Unit Testing
			TaskProvider<ProcessPropertyListTask> processUnitTestPropertyListTask = getTasks().register("processUnitTestPropertyList", ProcessPropertyListTask.class, task -> {
				task.getIdentifier().set(getProviders().provider(() -> getGroupId().get().get().get() + "." + moduleName));
				task.getModule().set(moduleName);
				task.getSources().from("src/unitTest/resources/Info.plist");
				task.getOutputFile().set(getLayout().getBuildDirectory().file("ios/unitTest/Info.plist"));
			});

			TaskProvider<CreateIosXCTestBundleTask> createUnitTestXCTestBundle = getTasks().register("createUnitTestXCTestBundle", CreateIosXCTestBundleTask.class, task -> {
				task.getXCTestBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + moduleName + "-unsigned.xctest"));
				task.getSources().from(processUnitTestPropertyListTask.flatMap(it -> it.getOutputFile()));
				task.getSources().from(testSuite.getBinaries().withType(BundleBinary.class).getElements().map(binaries -> binaries.stream().map(binary -> binary.getLinkTask().get().getLinkedFile()).collect(Collectors.toList())));
			});
			Provider<CommandLineTool> codeSignatureTool = getProviders().provider(() -> new PathAwareCommandLineTool(new File("/usr/bin/codesign")));
			TaskProvider<SignIosApplicationBundleTask> signUnitTestXCTestBundle = getTasks().register("signUnitTestXCTestBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createUnitTestXCTestBundle.flatMap(CreateIosXCTestBundleTask::getXCTestBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + moduleName + ".xctest"));
				task.getCodeSignatureTool().set(codeSignatureTool);
				task.getCodeSignatureTool().disallowChanges();
			});

			TaskProvider<CreateIosApplicationBundleTask> createUnitTestApplicationBundleTask = getTasks().register("createUnitTestLauncherApplicationBundle", CreateIosApplicationBundleTask.class, task -> {
				task.getApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getNames().getBaseName().getAsCamelCase() + "-unsigned.app"));
				task.getSources().from(getTestedComponent().flatMap(c -> c.getVariants().getElements().map(it -> it.iterator().next().getBinaries().withType(IosApplicationBundleInternal.class).get().iterator().next().getBundleTask().map(t -> t.getSources()))));
				task.getPlugIns().from(signUnitTestXCTestBundle.flatMap(SignIosApplicationBundleTask::getSignedApplicationBundle));
				task.getFrameworks().from(getXCTestBundleInjectDynamicLibrary());
				task.getFrameworks().from(getXCTestFrameworks());
				task.getSwiftSupportRequired().set(false);
			});

			val signTask = getTasks().register("signUnitTestLauncherApplicationBundle", SignIosApplicationBundleTask.class, task -> {
				task.getUnsignedApplicationBundle().set(createUnitTestApplicationBundleTask.flatMap(CreateIosApplicationBundleTask::getApplicationBundle));
				task.getSignedApplicationBundle().set(getLayout().getBuildDirectory().file("ios/products/unitTest/" + getTestedComponent().get().getNames().getBaseName().getAsCamelCase() + ".app"));
				task.getCodeSignatureTool().set(codeSignatureTool);
				task.getCodeSignatureTool().disallowChanges();
			});

			testSuite.getBinaryCollection().add(getObjects().newInstance(SignedIosApplicationBundleInternal.class, signTask));
		});

		TaskProvider<Task> bundle = getTasks().register(names.getTaskName("bundle"), task -> {
			task.dependsOn(variant.map(it -> it.getBinaries().withType(SignedIosApplicationBundleInternal.class).get()));
		});
	}

	private Provider<File> getXCTestBundleInjectDynamicLibrary() {
		return getProviders().provider(() -> new File(getSdkPlatformPath(), "Developer/usr/lib/libXCTestBundleInject.dylib"));
	}

	private Provider<List<File>> getXCTestFrameworks() {
		return getProviders().provider(() -> {
			return ImmutableList.<File>builder()
				.add(new File(getSdkPlatformPath(), "Developer/Library/PrivateFrameworks/XCTAutomationSupport.framework"))
				.add(new File(getSdkPlatformPath(), "Developer/usr/lib/libXCTestSwiftSupport.dylib"))
				.add(new File(getSdkPlatformPath(), "Developer/Library/Frameworks/XCTest.framework"))
				.build();
		});
	}

	public static String getSdkPlatformPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--show-sdk-platform-path").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
