package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeCSource implements UTType {
	public static final UTTypeCSource INSTANCE = new UTTypeCSource();

	String identifier = "public.c-source";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "c" };
	}

	@Override
	public String getDisplayName() {
		return "C source code";
	}

	// conformsTo: public.source-code
}
