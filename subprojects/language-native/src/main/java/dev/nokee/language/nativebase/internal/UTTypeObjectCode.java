package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeObjectCode implements UTType {
	public static final UTTypeObjectCode INSTANCE = new UTTypeObjectCode();

	String identifier = "public.object-code";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "o", "obj"};
	}

	@Override
	public String getDisplayName() {
		return "Object code";
	}

	// conformsTo: public.data, public.executable
}
