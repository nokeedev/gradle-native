package dev.nokee.language.nativebase.internal;

import dev.nokee.language.nativebase.HeaderSearchPath;
import lombok.Value;

import java.io.File;

@Value
public class DefaultHeaderSearchPath implements HeaderSearchPath {
	File asFile;
}
