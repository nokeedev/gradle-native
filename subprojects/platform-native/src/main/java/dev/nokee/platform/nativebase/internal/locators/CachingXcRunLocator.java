package dev.nokee.platform.nativebase.internal.locators;

import java.io.File;

public class CachingXcRunLocator implements XcRunLocator {
	private final XcRunLocator delegate;
	private File cachedLocation;
	private String cachedVersion;

	public CachingXcRunLocator(XcRunLocator delegate) {
		this.delegate = delegate;
	}

	@Override
	public File findPath() {
		if (cachedLocation == null) {
			synchronized (this) {
				if (cachedLocation == null) {
					cachedLocation = delegate.findPath();
				}
			}
		}
		return cachedLocation;
	}

	@Override
	public String findVersion() {
		if (cachedVersion == null) {
			synchronized (this) {
				if (cachedVersion == null) {
					cachedVersion = delegate.findVersion();
				}
			}
		}
		return cachedVersion;
	}
}
