package dev.nokee.language.objectivecpp.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeObjectiveCppSource implements UTType {
	public static final UTTypeObjectiveCppSource INSTANCE = new UTTypeObjectiveCppSource();
	String identifier = "public.objective-c-plus-plus-source";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "mm" };
	}

	@Override
	public String getDisplayName() {
		return "Objective-C++ source code";
	}

	// conformsTo: public.source-code
}
