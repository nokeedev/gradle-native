package dev.nokee.docs;

import com.google.common.collect.ImmutableSet;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;

public abstract class PluginManagementBlock {
	protected final Set<String> repositoryUris = new HashSet<>();

	private PluginManagementBlock(Set<String> repositoryUris) {
		this.repositoryUris.addAll(repositoryUris);
	}

	public static PluginManagementBlock asKotlinDsl() {
		return new PluginManagementBlockWithoutVersion(emptySet()) {
			@Override
			public PluginManagementBlock withVersion(String version) {
				return new KotlinDslPluginManagementBlock(version, emptySet());
			}
		};
	}

	public static PluginManagementBlock asGroovyDsl() {
		return new PluginManagementBlockWithoutVersion(emptySet()) {
			@Override
			public PluginManagementBlock withVersion(String version) {
				return new GroovyDslPluginManagementBlock(version, emptySet());
			}
		};
	}

	public abstract PluginManagementBlock withVersion(String version);

	public abstract PluginManagementBlock withRepository(String uri);

	public String configureFromInitScript() {
		return "settingsEvaluated { settings ->\nsettings." + this.toString() + "\n}\n";
	}

	private static abstract class PluginManagementBlockWithoutVersion extends PluginManagementBlock {
		PluginManagementBlockWithoutVersion(Set<String> repositoryUris) {
			super(repositoryUris);
		}

		@Override
		public PluginManagementBlock withRepository(String uri) {
			PluginManagementBlock delegate = this;
			return new PluginManagementBlockWithoutVersion(ImmutableSet.<String>builder().addAll(repositoryUris).add(uri).build()) {
				@Override
				public PluginManagementBlock withVersion(String version) {
					// TODO: Support multiple repository
					assert repositoryUris.size() == 1;
					return delegate.withVersion(version).withRepository(uri);
				}
			};
		}

		@Override
		public String toString() {
			throw new IllegalStateException("A version is needed");
		}
	}

	private static class KotlinDslPluginManagementBlock extends PluginManagementBlock {
		private final String version;

		public KotlinDslPluginManagementBlock(String version, Set<String> repositoryUris) {
			super(repositoryUris);
			this.version = version;
		}

		@Override
		public PluginManagementBlock withRepository(String uri) {
			return new KotlinDslPluginManagementBlock(version, ImmutableSet.<String>builder().addAll(repositoryUris).add(uri).build());
		}

		@Override
		public PluginManagementBlock withVersion(String version) {
			return new KotlinDslPluginManagementBlock(version, repositoryUris);
		}

		@Override
		public String toString() {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(outStream);
			out.println("pluginManagement {");
			out.println("	repositories {");
			out.println("		gradlePluginPortal()");
			out.println(maven("https://dl.bintray.com/nokeedev/distributions-snapshots"));
			repositoryUris.stream().map(KotlinDslPluginManagementBlock::maven).forEach(out::println);
			out.println("	}");
			out.println("	resolutionStrategy {");
			out.println("		eachPlugin {");
			out.println("			if (requested.id.id.startsWith(\"dev.nokee.\")) {");
			out.println("				useModule(\"${requested.id.id}:${requested.id.id}.gradle.plugin:" + version + "\")");
			out.println("			}");
			out.println("		}");
			out.println("	}");
			out.println("}");
			out.println();
			out.flush();
			return outStream.toString();
		}

		private static String maven(String repositoryUri) {
			return "		maven {\n" +
			"			url = uri(\"" + repositoryUri + "\")\n" +
			"		}";
		}
	}

	private static class GroovyDslPluginManagementBlock extends PluginManagementBlock {
		private final String version;

		public GroovyDslPluginManagementBlock(String version, Set<String> repositoryUris) {
			super(repositoryUris);
			this.version = version;
		}

		@Override
		public PluginManagementBlock withRepository(String uri) {
			return new GroovyDslPluginManagementBlock(version, ImmutableSet.<String>builder().addAll(repositoryUris).add(uri).build());
		}

		@Override
		public PluginManagementBlock withVersion(String version) {
			return new GroovyDslPluginManagementBlock(version, repositoryUris);
		}

		@Override
		public String toString() {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(outStream);
			out.println("pluginManagement {");
			out.println("	repositories {");
			out.println("		gradlePluginPortal()");
			out.println(maven("https://dl.bintray.com/nokeedev/distributions-snapshots"));
			repositoryUris.stream().map(GroovyDslPluginManagementBlock::maven).forEach(out::println);
			out.println("	}");
			out.println("	resolutionStrategy {");
			out.println("		eachPlugin {");
			out.println("			if (requested.id.id.startsWith('dev.nokee.')) {");
			out.println("				useModule(\"${requested.id.id}:${requested.id.id}.gradle.plugin:" + version + "\")");
			out.println("			}");
			out.println("		}");
			out.println("	}");
			out.println("}");
			out.println();
			out.flush();
			return outStream.toString();
		}

		private static String maven(String repositoryUri) {
			return "		maven {\n" +
				"			url = '" + repositoryUri + "'\n" +
				"		}";
		}
	}
}
