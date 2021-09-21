package dev.nokeebuild.licensing;

import com.diffplug.gradle.spotless.GroovyExtension;
import com.diffplug.gradle.spotless.JavaExtension;
import com.diffplug.gradle.spotless.SpotlessExtension;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.plugins.PluginManager;

import java.util.Arrays;
import java.util.stream.Collectors;

final class ConfigureSpotlessLicenseHeaderAction implements Action<AppliedPlugin> {
	private final GradleExtensions extensions;
	private final PluginManager pluginManager;

	public ConfigureSpotlessLicenseHeaderAction(GradleExtensions extensions, PluginManager pluginManager) {
		this.extensions = extensions;
		this.pluginManager = pluginManager;
	}

	@Override
	public void execute(AppliedPlugin ignored) {
		extensions.configure("spotless", this::spotless);
	}

	private void spotless(SpotlessExtension spotless) {
		pluginManager.withPlugin("java-base", ignored -> {
			spotless.java(this::java);
		});
		pluginManager.withPlugin("groovy-base", ignored -> {
			spotless.groovy(this::groovy);
		});
	}

	private void java(JavaExtension java) {
		extensions.ifPresent(LicenseExtension.class, extension -> {
			java.licenseHeader(extension.getCopyrightFileHeader().map(asMultilineJavaComment()).map(normalizeTodayYearPattern()).get());
		});
	}

	private void groovy(GroovyExtension groovy) {
		extensions.ifPresent(LicenseExtension.class, extensions -> {
			groovy.licenseHeader(extensions.getCopyrightFileHeader().map(asMultilineJavaComment()).map(normalizeTodayYearPattern()).get());
		});
	}

	private static Transformer<String, String> normalizeTodayYearPattern() {
		// because, spotless is very precise regarding this particular token
		return s -> s.replace("${today.year}", "$today.year");
	}

	private static Transformer<String, String> asMultilineJavaComment() {
		return new Transformer<String, String>() {
			@Override
			public String transform(String s) {
				return "/*\n" // opening multiline comment
					+ Arrays.stream(s.split("\n"))
						.map(this::prependMultilineCommentChars)
						.collect(Collectors.joining("\n"))
					+ "\n */"; // closing multiline comment
			}

			private String prependMultilineCommentChars(String line) {
				StringBuilder builder = new StringBuilder();
				builder.append(" *");
				if (!line.isEmpty()) {
					builder.append(" ").append(line);
				}
				return builder.toString();
			}
		};
	}
}
