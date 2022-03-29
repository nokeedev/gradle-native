/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.cpp.results;

import dev.nokee.platform.cpp.OutputDirSelector;

import java.io.File;

public final class DefaultOutputDirSelector implements OutputDirSelector {
    private static final String DEBUG_ARTIFACTS_DIRECTORY_PROPERTY_NAME = "org.gradle.performance.debugArtifactsDirectory";

    private final File baseOutputDir;

    public DefaultOutputDirSelector(File fallbackDirectory) {
        String debugArtifactsDirectoryPath = System.getProperty(DEBUG_ARTIFACTS_DIRECTORY_PROPERTY_NAME);
		if (debugArtifactsDirectoryPath == null || debugArtifactsDirectoryPath.isEmpty()) {
			this.baseOutputDir = fallbackDirectory;
		} else {
            this.baseOutputDir = new File(debugArtifactsDirectoryPath);
		}
    }

    @Override
    public File outputDirFor(String testId) {
        String fileSafeName = OutputDirSelector.fileSafeNameFor(testId);
        return new File(baseOutputDir, shortenPath(fileSafeName, 40));
    }

    private static String shortenPath(String longName, int expectedMaxLength) {
        if (longName.length() <= expectedMaxLength) {
            return longName;
        } else {
            return longName.substring(0, expectedMaxLength - 10) + "." + longName.substring(longName.length() - 9);
        }
    }
}
