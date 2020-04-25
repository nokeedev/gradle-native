package dev.nokee.docs;

import dev.nokee.docs.types.UTType;

public interface LanguageTransform<IN extends UTType, OUT extends UTType> {
	SourceSet<OUT> transform(SourceSet<IN> sourceSet);
}
