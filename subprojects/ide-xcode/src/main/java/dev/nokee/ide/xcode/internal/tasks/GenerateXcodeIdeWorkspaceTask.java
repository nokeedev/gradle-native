package dev.nokee.ide.xcode.internal.tasks;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import dev.nokee.ide.xcode.XcodeIdeProjectReference;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

		List<Workspace.FileRef> fileReferences = getProjectReferences().get().stream().map(it -> new Workspace.FileRef(it.getLocation().get().getAsFile().getAbsolutePath())).collect(Collectors.toList());
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		xmlMapper.writeValue(new File(workspaceDirectory, "contents.xcworkspacedata"), new Workspace(fileReferences));

		File sharedWorkspaceSettingsFile = new File(workspaceDirectory, "xcshareddata/WorkspaceSettings.xcsettings");
		sharedWorkspaceSettingsFile.getParentFile().mkdirs();
		FileUtils.writeStringToFile(sharedWorkspaceSettingsFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
			"<plist version=\"1.0\">\n" +
			"<dict>\n" +
			"\t<key>IDEWorkspaceSharedSettings_AutocreateContextsIfNeeded</key>\n" +
			"\t<false/>\n" +
			"</dict>\n" +
			"</plist>", Charset.defaultCharset());

		File userWorkspaceSettingsFile = new File(workspaceDirectory, "xcuserdata/" + System.getProperty("user.name") + ".xcuserdatad/WorkspaceSettings.xcsettings");
		userWorkspaceSettingsFile.getParentFile().mkdirs();
		FileUtils.writeStringToFile(userWorkspaceSettingsFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
			"<plist version=\"1.0\">\n" +
			"<dict>\n" +
			"\t<key>BuildLocationStyle</key>\n" +
			"\t<string>UseAppPreferences</string>\n" +
			"\t<key>CustomBuildLocationType</key>\n" +
			"\t<string>RelativeToDerivedData</string>\n" +
			"\t<key>DerivedDataCustomLocation</key>\n" +
			"\t<string>" + getDerivedDataLocation().get() + "</string>\n" +
			"\t<key>DerivedDataLocationStyle</key>\n" +
			"\t<string>WorkspaceRelativePath</string>\n" +
			"\t<key>IssueFilterStyle</key>\n" +
			"\t<string>ShowActiveSchemeOnly</string>\n" +
			"\t<key>LiveSourceIssuesEnabled</key>\n" +
			"\t<true/>\n" +
			"\t<key>ShowSharedSchemesAutomaticallyEnabled</key>\n" +
			"\t<true/>\n" +
			"</dict>\n" +
			"</plist>", Charset.defaultCharset());
	}

	@Value
	private static class Workspace {
		@JacksonXmlElementWrapper(useWrapping = false)
		@JacksonXmlProperty(localName = "FileRef")
		@NonNull Collection<FileRef> fileRef;

		@JacksonXmlProperty(isAttribute = true)
		public String getVersion() {
			return "1.0";
		}

		@Value
		static class FileRef {
			@JacksonXmlProperty(isAttribute = true)
			String location;

			public FileRef(String location) {
				this.location = "absolute:" + location;
			}
		}
	}
}
