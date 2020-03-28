package dev.nokee.platform.nativebase.internal.locators;

public abstract class MacOSSdkPathLocator extends AbstractXcRunLocator {
	@Override
	protected String getXcRunFlagPrefix() {
		return "--show-sdk";
	}
}
