package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeCppHeader implements UTType {
	public static final UTTypeCppHeader INSTANCE = new UTTypeCppHeader();

	String identifier = "public.c-plus-plus-header";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "hpp", "h++", "hxx" };
	}

	@Override
	public String getDisplayName() {
		return "C++ header file";
	}

	// conformsTo: public.source-code
}
