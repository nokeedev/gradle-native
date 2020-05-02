package dev.nokee.ide.xcode.internal.tasks;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import lombok.NonNull;
import lombok.Value;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GenerateXcodeIdeWorkspaceTask extends DefaultTask {
	@InputFiles
	public abstract SetProperty<FileSystemLocation> getProjectLocations();

	@OutputDirectory
	public abstract Property<FileSystemLocation> getWorkspaceLocation();

	@TaskAction
	private void generate() throws IOException {
		File workspaceDirectory = getWorkspaceLocation().get().getAsFile();
		FileUtils.deleteDirectory(workspaceDirectory);
		workspaceDirectory.mkdirs();

		List<Workspace.FileRef> fileReferences = getProjectLocations().get().stream().map(it -> new Workspace.FileRef(it.getAsFile().getAbsolutePath())).collect(Collectors.toList());
		XmlMapper xmlMapper = new XmlMapper();
		xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
		xmlMapper.writeValue(new File(workspaceDirectory, "contents.xcworkspacedata"), new Workspace(fileReferences));

		File workspaceSettingsFile = new File(workspaceDirectory, "xcshareddata/WorkspaceSettings.xcsettings");
		workspaceSettingsFile.getParentFile().mkdirs();
		FileUtils.writeStringToFile(workspaceSettingsFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
			"<plist version=\"1.0\">\n" +
			"<dict>\n" +
			"\t<key>IDEWorkspaceSharedSettings_AutocreateContextsIfNeeded</key>\n" +
			"\t<false/>\n" +
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
