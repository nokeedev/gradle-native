package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

@AllArgsConstructor(onConstructor_ = {@Inject})
public abstract class ObjectSourceSetInternal extends LanguageSourceSetInternal {
	@Getter private final TaskProvider<? extends NativeSourceCompileTask> compileTask;
	@Getter private final LanguageType languageType;

	public enum LanguageType {
		CPP, C, OBJECTIVE_C, OBJECTIVE_CPP
	}
}
