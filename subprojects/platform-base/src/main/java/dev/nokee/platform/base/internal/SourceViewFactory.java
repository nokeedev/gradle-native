package dev.nokee.platform.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.base.View;

public interface SourceViewFactory {
	<T extends LanguageSourceSet> SourceView<T> create(View<T> delegate);
}
