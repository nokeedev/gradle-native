package dev.nokee.language.nativebase.internal.rules;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;

public final class RegisterNativeLanguageSourceSetRule implements Action<DomainObjectIdentifier> {
	private final LanguageSourceSetName sourceSetName;
	private final Class<? extends LanguageSourceSetInternal> sourceSetType;
	private final LanguageSourceSetRegistry languageSourceSetRegistry;
	private final Action<? super LanguageSourceSetInternal> action;

	public RegisterNativeLanguageSourceSetRule(LanguageSourceSetName sourceSetName, Class<? extends LanguageSourceSetInternal> sourceSetType, LanguageSourceSetRegistry languageSourceSetRegistry) {
		this(sourceSetName, sourceSetType, languageSourceSetRegistry, ActionUtils.doNothing());
	}

	public RegisterNativeLanguageSourceSetRule(LanguageSourceSetName sourceSetName, Class<? extends LanguageSourceSetInternal> sourceSetType, LanguageSourceSetRegistry languageSourceSetRegistry, Action<LanguageSourceSetInternal> action) {
		this.sourceSetName = sourceSetName;
		this.sourceSetType = sourceSetType;
		this.languageSourceSetRegistry = languageSourceSetRegistry;
		this.action = action;
	}

	@Override
	public void execute(DomainObjectIdentifier identifier) {
		languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(sourceSetName, sourceSetType, identifier), action);
	}
}
