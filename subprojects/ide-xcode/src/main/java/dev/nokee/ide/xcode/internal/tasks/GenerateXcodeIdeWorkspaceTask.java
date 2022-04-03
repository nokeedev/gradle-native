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

import dev.nokee.ide.xcode.XcodeIdeProjectReference;
import dev.nokee.xcode.XmlPropertyListWriter;
import dev.nokee.xcode.workspace.WorkspaceSettings;
import dev.nokee.xcode.workspace.WorkspaceSettingsWriter;
import dev.nokee.xcode.workspace.XCFileReference;
import dev.nokee.xcode.workspace.XCWorkspaceData;
import dev.nokee.xcode.workspace.XCWorkspaceDataWriter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static dev.nokee.xcode.workspace.WorkspaceSettings.BuildLocationStyle.UseAppPreferences;
import static dev.nokee.xcode.workspace.WorkspaceSettings.CustomBuildLocationType.RelativeToDerivedData;
import static dev.nokee.xcode.workspace.WorkspaceSettings.DerivedDataLocationStyle.WorkspaceRelativePath;
import static dev.nokee.xcode.workspace.WorkspaceSettings.IssueFilterStyle.ShowActiveSchemeOnly;

public abstract class GenerateXcodeIdeWorkspaceTask extends DefaultTask {
	@Internal
	public abstract SetProperty<XcodeIdeProjectReference> getProjectReferences();

	@InputFiles
	protected Provider<List<FileSystemLocation>> getInputFiles() {
		return getProjectReferences().map(it -> it.stream().map(XcodeIdeProjectReference::getLocation).map(Provider::get).collect(Collectors.toList()));
	}

	@Input
	public abstract Property<String> getDerivedDataLocation();

	@OutputDirectory
	public abstract Property<FileSystemLocation> getWorkspaceLocation();

	@Inject
	public GenerateXcodeIdeWorkspaceTask() {
		dependsOn(getProjectReferences());
	}

	@TaskAction
	private void generate() throws IOException {
		File workspaceDirectory = getWorkspaceLocation().get().getAsFile();
		FileUtils.deleteDirectory(workspaceDirectory);
		workspaceDirectory.mkdirs();

		try (val writer = new XCWorkspaceDataWriter(new FileWriter(new File(workspaceDirectory, "contents.xcworkspacedata")))) {
			val builder = XCWorkspaceData.builder();
			getProjectReferences().get().stream().map(it -> XCFileReference.of("absolute:" + it.getLocation().get().getAsFile().getAbsolutePath())).forEach(builder::fileRef);
			writer.write(builder.build());
		}

		File sharedWorkspaceSettingsFile = new File(workspaceDirectory, "xcshareddata/WorkspaceSettings.xcsettings");
		sharedWorkspaceSettingsFile.getParentFile().mkdirs();
		try (val writer = new WorkspaceSettingsWriter(new XmlPropertyListWriter(new FileWriter(sharedWorkspaceSettingsFile)))) {
			writer.write(WorkspaceSettings.builder().put(WorkspaceSettings.AutoCreateSchemes.Disabled).build());
		}

		File userWorkspaceSettingsFile = new File(workspaceDirectory, "xcuserdata/" + System.getProperty("user.name") + ".xcuserdatad/WorkspaceSettings.xcsettings");
		userWorkspaceSettingsFile.getParentFile().mkdirs();
		try (val writer = new WorkspaceSettingsWriter(new XmlPropertyListWriter(new FileWriter(userWorkspaceSettingsFile)))) {
			writer.write(WorkspaceSettings.builder()
				.put(UseAppPreferences)
				.put(RelativeToDerivedData)
				.put(new WorkspaceSettings.DerivedDataCustomLocation(getDerivedDataLocation().get()))
				.put(WorkspaceRelativePath)
				.put(ShowActiveSchemeOnly)
				.put(WorkspaceSettings.LiveSourceIssues.Enabled)
				.put(WorkspaceSettings.ShowSharedSchemesAutomatically.Enabled)
				.build());
		}
	}
}
