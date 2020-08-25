package dev.nokee.platform.base;

import dev.nokee.language.base.LanguageSourceSet;

public interface SourceAwareComponent {
	SourceView<LanguageSourceSet> getSources();
}
