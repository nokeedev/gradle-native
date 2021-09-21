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
			.get()
			.withArguments("--sdk", "iphonesimulator", "--show-sdk-path")
			.execute(ENGINE)
			.getResult()
			.assertNormalExitValue()
			.getStandardOutput()
			.getAsString()
			.trim();
	}
}
