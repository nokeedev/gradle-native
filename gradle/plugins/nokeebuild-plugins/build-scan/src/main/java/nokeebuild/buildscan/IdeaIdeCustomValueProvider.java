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
package nokeebuild.buildscan;

import com.gradle.scan.plugin.BuildScanExtension;
import org.gradle.api.Action;

import java.util.Optional;

final class IdeaIdeCustomValueProvider implements Action<BuildScanExtension> {
	public static final String[] IDEA_RUNTIME_SYSTEM_PROPERTY_NAMES = new String[]{"idea.registered", "idea.active", "idea.paths.selector"};
	public static final String IDEA_VERSION_SYSTEM_PROPERTY_NAME = "idea.paths.selector";
	private static final String IDEA_VERSION = "ideaVersion";
	private final Parameters ideaRuntime;

	IdeaIdeCustomValueProvider(Parameters ideaRuntime) {
		this.ideaRuntime = ideaRuntime;
	}

	@Override
	public void execute(BuildScanExtension buildScan) {
		if (ideaRuntime.wasLaunchedFromIdea()) {
			buildScan.tag("IDEA");
			ideaRuntime.ideaVersion().ifPresent(it -> buildScan.value(IDEA_VERSION, it));
		}
	}

	interface Parameters {
		boolean wasLaunchedFromIdea();

		Optional<String> ideaVersion();
	}
}
