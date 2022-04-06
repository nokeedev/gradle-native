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
package dev.nokee.xcode.scheme;

import lombok.Value;

import java.util.List;

@Value
public class XCScheme {
	BuildAction buildAction;
	TestAction testAction;
	LaunchAction launchAction;
	ProfileAction profileAction = new ProfileAction();
	AnalyzeAction analyzeAction = new AnalyzeAction();
	ArchiveAction archiveAction = new ArchiveAction();

	public String getLastUpgradeVersion() {
		return "0830";
	}

	public String getVersion() {
		return "1.3";
	}

	@Value
	public static class BuildAction {
		List<BuildAction.BuildActionEntry> buildActionEntries;

		public boolean getParallelizeBuildables() {
			// Gradle takes care of executing the build in parallel.
			return false;
		}

		public boolean getBuildImplicitDependencies() {
			// Gradle takes care of the project dependencies.
			return false;
		}

		@Value
		public static class BuildActionEntry {
			boolean buildForTesting;
			boolean buildForRunning;
			boolean buildForProfiling;
			boolean buildForArchiving;
			boolean buildForAnalyzing;
			BuildableReference buildableReference;
		}
	}

	@Value
	public static class TestAction {
		List<TestAction.TestableReference> testables;

		public String getBuildConfiguration() {
			return "Default";
		}

		public String getSelectedDebuggerIdentifier() {
			return "Xcode.DebuggerFoundation.Debugger.LLDB";
		}

		public String getSelectedLauncherIdentifier() {
			return "Xcode.DebuggerFoundation.Launcher.LLDB";
		}

		public boolean getShouldUseLaunchSchemeArgsEnv() {
			return true;
		}
		// TODO: AdditionalOptions

		@Value
		public static class TestableReference {
			BuildableReference buildableReference;

			public boolean getSkipped() {
				return false;
			}
		}
	}

	@Value
	public static class LaunchAction {
		LaunchAction.BuildableProductRunnable buildableProductRunnable;

		public String getBuildConfiguration() {
			return "Default";
		}

		public String getSelectedDebuggerIdentifier() {
			return "Xcode.DebuggerFoundation.Debugger.LLDB";
		}

		public String getSelectedLauncherIdentifier() {
			return "Xcode.DebuggerFoundation.Launcher.LLDB";
		}

		public String getLaunchStyle() {
			return "0";
		}

		public boolean getUseCustomWorkingDirectory() {
			return false;
		}

		public boolean getIgnoresPersistentStateOnLaunch() {
			return false;
		}

		public boolean getDebugDocumentVersioning() {
			return true;
		}

		public String getDebugServiceExtension() {
			return "internal";
		}

		public boolean getAllowLocationSimulation() {
			return true;
		}

		// TODO: AdditionalOptions

		@Value
		public static class BuildableProductRunnable {
			BuildableReference buildableReference;

			public String getRunnableDebuggingMode() {
				return "0";
			}

		}
	}

	@Value
	public static class ProfileAction {
		public String getBuildConfiguration() {
			return "Default";
		}

		public boolean getShouldUseLaunchSchemeArgsEnv() {
			return true;
		}

		public String getSavedToolIdentifier() {
			return "";
		}

		public boolean getUseCustomWorkingDirectory() {
			return false;
		}

		public boolean getDebugDocumentVersioning() {
			return true;
		}
	}

	@Value
	public static class AnalyzeAction {
		public String getBuildConfiguration() {
			return "Default";
		}
	}

	@Value
	public static class ArchiveAction {
		public String getBuildConfiguration() {
			return "Default";
		}

		public boolean getRevealArchiveInOrganizer() {
			return true;
		}
	}

	@Value
	public static class BuildableReference {
		String blueprintIdentifier;
		String buildableName;
		String blueprintName;
		String referencedContainer;

		public String getBuildableIdentifier() {
			return "primary";
		}
	}
}
