package dev.gradleplugins.documentationkit;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;

public interface DslMetaData {
	LanguageSourceSet getSources();

	ConfigurableFileCollection getClassDocbookFiles();

	DirectoryProperty getExtractedMetaDataFile();
}
