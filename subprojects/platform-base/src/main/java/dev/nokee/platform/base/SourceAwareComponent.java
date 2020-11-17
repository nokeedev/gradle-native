package dev.nokee.platform.base;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.LanguageSourceSetView;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface SourceAwareComponent {
	LanguageSourceSetView<LanguageSourceSet> getSources();

	default void sources(Action<? super LanguageSourceSetView<LanguageSourceSet>> action) {
		action.execute(getSources());
	}

	default void sources(@DelegatesTo(value = LanguageSourceSetView.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		ConfigureUtil.configureUsing(closure).execute(getSources());
	}
}
