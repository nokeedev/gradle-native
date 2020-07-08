package dev.nokee.language.c.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeCHeader implements UTType {
	public static final UTTypeCHeader INSTANCE = new UTTypeCHeader();

	String identifier = "public.c-header";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "h" };
	}

	@Override
	public String getDisplayName() {
		return "C header file";
	}

	// conformsTo: public.source-code
}
