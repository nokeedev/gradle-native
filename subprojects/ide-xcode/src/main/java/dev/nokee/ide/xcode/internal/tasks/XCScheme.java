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
package dev.nokee.ide.xcode.internal.tasks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Value;

import java.util.List;

/**
 * <Scheme
 * LastUpgradeVersion = "0830"
 * version = "1.3">
 * ...
 * </Scheme>
 */
@Value
@JacksonXmlRootElement(localName = "Scheme")
class XCScheme {
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

	@JacksonXmlProperty(localName = "LastUpgradeVersion", isAttribute = true)
	public String getLastUpgradeVersion() {
		return "0830";
	}

	@JacksonXmlProperty(isAttribute = true)
	public String getVersion() {
		return "1.3";
	}

	/**
	 * <BuildAction
	 * parallelizeBuildables = "YES"
	 * buildImplicitDependencies = "YES">
	 * <BuildActionEntries>
	 * </BuildActionEntries>
	 * </BuildAction>
	 */
	@Value
	public static class BuildAction {
		@JacksonXmlElementWrapper(localName = "BuildActionEntries")
		@JacksonXmlProperty(localName = "BuildActionEntry")
		List<BuildAction.BuildActionEntry> buildActionEntries;

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
		List<TestAction.TestableReference> testables;

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
		@JsonInclude(JsonInclude.Include.NON_NULL)
		@JacksonXmlProperty(localName = "BuildableProductRunnable")
		LaunchAction.BuildableProductRunnable buildableProductRunnable;

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
