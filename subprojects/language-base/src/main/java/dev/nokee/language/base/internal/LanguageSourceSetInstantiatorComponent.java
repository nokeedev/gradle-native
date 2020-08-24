package dev.nokee.language.base.internal;

import dagger.BindsInstance;
import dagger.Component;
import dev.nokee.gradle.internal.GradleModule;
import org.gradle.api.Project;

@Component(modules = {GradleModule.class, LanguageModule.class})
public interface LanguageSourceSetInstantiatorComponent {
	LanguageSourceSetInstantiator get();

	@Component.Factory
	interface Factory {
		LanguageSourceSetInstantiatorComponent create(@BindsInstance Project project);
	}
}
