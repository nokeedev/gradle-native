package dev.nokee.language.base.internal;

import com.google.common.collect.Maps;
import dev.nokee.language.base.LanguageSourceSet;
import org.gradle.api.Namer;

import java.util.Map;

/**
 * An implementation of the namer interface for objects implementing the {@link LanguageSourceSet} interface.
 */
public class LanguageSourceSetNamer implements Namer<LanguageSourceSet> {
	public static final Namer<LanguageSourceSet> INSTANCE = new LanguageSourceSetNamer();
	private final static Map<String, String> LANGUAGES = Maps.newHashMap();

	@Override
	public String determineName(LanguageSourceSet object) {
		return guessLanguageName(object.getPublicType().getConcreteClass().getSimpleName());
	}

	private static synchronized String guessLanguageName(String typeName) {
		return LANGUAGES.computeIfAbsent(typeName, LanguageSourceSetNamer::computeLanguageName);
	}

	private static String computeLanguageName(String typeName) {
		return typeName.replaceAll("LanguageSourceSet$", "").replaceAll("SourceSet$", "").replaceAll("Source$", "").replaceAll("Set$", "");
	}
}
