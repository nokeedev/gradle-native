package dev.nokee.language.base.internal;

import lombok.Value;

@Value
public class UTTypeSourceCode implements UTType {
	public static final UTTypeSourceCode INSTANCE = new UTTypeSourceCode();

	String identifier = "public.source-code";

	@Override
	public String[] getFilenameExtensions() {
		return new String[0];
	}

	@Override
	public String getDisplayName() {
		return "Generic source code";
	}

	// Conform to: public.plain-text
	// Tags: none
}
