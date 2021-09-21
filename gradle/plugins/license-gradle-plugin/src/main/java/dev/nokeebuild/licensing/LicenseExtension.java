package dev.nokeebuild.licensing;

import org.gradle.api.provider.Provider;

import java.net.URL;

public interface LicenseExtension {
	Provider<String> getDisplayName();
	Provider<URL> getLicenseUrl();
	Provider<String> getName();
	Provider<String> getShortName();
	Provider<String> getCopyrightFileHeader();
}
