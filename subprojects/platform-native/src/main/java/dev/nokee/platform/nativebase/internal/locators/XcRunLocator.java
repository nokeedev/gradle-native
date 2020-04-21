package dev.nokee.platform.nativebase.internal.locators;

import java.io.File;

public interface XcRunLocator {
	File findPath();

	String findVersion();
}
