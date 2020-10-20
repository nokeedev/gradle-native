package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.file.FileCollection;

public interface LanguageSourceSetInternal extends LanguageSourceSet {
	LanguageSourceSetIdentifier<?> getIdentifier();

	LanguageSourceSet convention(FileCollection files);
}
