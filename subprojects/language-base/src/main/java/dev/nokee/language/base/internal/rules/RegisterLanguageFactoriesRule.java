package dev.nokee.language.base.internal.rules;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetFactory;
import dev.nokee.language.base.internal.LanguageSourceSetFactoryRegistry;

import javax.inject.Inject;
import java.util.Map;

public final class RegisterLanguageFactoriesRule implements Runnable {
	private final Map<Class<?>, LanguageSourceSetFactory<?>> factories;
	private final LanguageSourceSetFactoryRegistry registry;

	@Inject
	public RegisterLanguageFactoriesRule(Map<Class<?>, LanguageSourceSetFactory<?>> factories, LanguageSourceSetFactoryRegistry registry) {
		this.factories = factories;
		this.registry = registry;
	}

	@Override
	public void run() {
		factories.forEach((key, value) -> {
			registry.registerFactory((Class<LanguageSourceSet>)key, value);
		});
	}
}
