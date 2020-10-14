package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeObjectiveCSource implements UTType {
	public static final UTTypeObjectiveCSource INSTANCE = new UTTypeObjectiveCSource();

	String identifier = "public.objective-c-source";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "m" };
	}

	@Override
	public String getDisplayName() {
		return "Objective-C source code";
	}

	// conformsTo: public.source-code
	// tags: .m
	// displayName: Objective-C source code
}
