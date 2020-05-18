package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeCppSource implements UTType {
	String identifier = "public.c-plus-plus-source";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "cp", "cpp", "c++", "cc", "cxx" };
	}

	@Override
	public String getDisplayName() {
		return "C++ source code";
	}

	// conformsTo: public.source-code
}
