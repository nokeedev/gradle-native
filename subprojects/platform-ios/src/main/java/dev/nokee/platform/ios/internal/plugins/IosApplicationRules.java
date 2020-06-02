package dev.nokee.platform.ios.internal.plugins;

import dev.nokee.core.exec.CachingProcessBuilderEngine;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.ProcessBuilderEngine;

public class IosApplicationRules {
	private static final CachingProcessBuilderEngine ENGINE = new CachingProcessBuilderEngine(new ProcessBuilderEngine());

	// Api used by :testingXctest
	public static String getSdkPath() {
		return CommandLineTool
			.fromPath("xcrun")
			.withArguments("--sdk", "iphonesimulator", "--show-sdk-path")
			.execute(ENGINE)
			.getResult()
			.assertNormalExitValue()
			.getStandardOutput()
			.getAsString()
			.trim();
	}
}
