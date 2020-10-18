package dev.nokee.platform.base;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;

public interface SourceAwareComponent {
	LanguageSourceSetView<LanguageSourceSet> getSources();
}
