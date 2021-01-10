package dev.gradleplugins.documentationkit;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

public interface DslContent {
	ConfigurableFileCollection getClassDocbookDirectories();
	ConfigurableFileCollection getClassMetaDataFiles();
	RegularFileProperty getTemplateFile();
	SetProperty<String> getClassNames();
	Property<String> getPermalink();

	DirectoryProperty getContentDirectory();
}
