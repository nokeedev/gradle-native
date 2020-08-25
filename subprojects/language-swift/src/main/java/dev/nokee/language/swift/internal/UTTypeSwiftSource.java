package dev.nokee.language.swift.internal;

import dev.nokee.language.base.internal.UTType;
import lombok.Value;

@Value
public class UTTypeSwiftSource implements UTType {
	public static final UTTypeSwiftSource INSTANCE = new UTTypeSwiftSource();
	String identifier = "public.swift-source";

	@Override
	public String[] getFilenameExtensions() {
		return new String[] { "swift" };
	}

	@Override
	public String getDisplayName() {
		return "Swift source code";
	}
}
