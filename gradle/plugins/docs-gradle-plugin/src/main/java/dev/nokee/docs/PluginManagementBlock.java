package dev.nokee.docs;

public abstract class PluginManagementBlock {
	public static PluginManagementBlock asKotlinDsl() {
		return new PluginManagementBlockWithoutVersion() {
			@Override
			public PluginManagementBlock withVersion(String version) {
				return new KotlinDslPluginManagementBlock(version);
			}
		};
	}

	public static PluginManagementBlock asGroovyDsl() {
		return new PluginManagementBlockWithoutVersion() {
			@Override
			public PluginManagementBlock withVersion(String version) {
				return new GroovyDslPluginManagementBlock(version);
			}
		};
	}

	public abstract PluginManagementBlock withVersion(String version);

	private static abstract class PluginManagementBlockWithoutVersion extends PluginManagementBlock {
		@Override
		public String toString() {
			throw new IllegalStateException("A version is needed");
		}
	}

	private static class KotlinDslPluginManagementBlock extends PluginManagementBlock {
		private final String version;

		public KotlinDslPluginManagementBlock(String version) {
			this.version = version;
		}

		@Override
		public PluginManagementBlock withVersion(String version) {
			return new KotlinDslPluginManagementBlock(version);
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("pluginManagement {").append("\n");
			sb.append("	repositories {").append("\n");
			sb.append("		gradlePluginPortal()").append("\n");
			sb.append("		maven {").append("\n");
			sb.append("			url = uri(\"https://dl.bintray.com/nokeedev/distributions-snapshots\")").append("\n");
			sb.append("		}").append("\n");
			sb.append("	}").append("\n");
			sb.append("	resolutionStrategy {").append("\n");
			sb.append("		eachPlugin {").append("\n");
			sb.append("			if (requested.id.id.startsWith(\"dev.nokee.\")) {").append("\n");
			sb.append("				useModule(\"${requested.id.id}:${requested.id.id}.gradle.plugin:" + version + "\")").append("\n");
			sb.append("			}").append("\n");
			sb.append("		}").append("\n");
			sb.append("	}").append("\n");
			sb.append("}").append("\n");
			sb.append("\n");
			return sb.toString();
		}
	}

	private static class GroovyDslPluginManagementBlock extends PluginManagementBlock {
		private final String version;

		public GroovyDslPluginManagementBlock(String version) {
			this.version = version;
		}

		@Override
		public PluginManagementBlock withVersion(String version) {
			return new GroovyDslPluginManagementBlock(version);
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("pluginManagement {").append("\n");
			sb.append("	repositories {").append("\n");
			sb.append("		gradlePluginPortal()").append("\n");
			sb.append("		maven {").append("\n");
			sb.append("			url = uri('https://dl.bintray.com/nokeedev/distributions-snapshots')").append("\n");
			sb.append("		}").append("\n");
			sb.append("	}").append("\n");
			sb.append("	resolutionStrategy {").append("\n");
			sb.append("		eachPlugin {").append("\n");
			sb.append("			if (requested.id.id.startsWith('dev.nokee.')) {").append("\n");
			sb.append("				useModule(\"${requested.id.id}:${requested.id.id}.gradle.plugin:" + version + "\")").append("\n");
			sb.append("			}").append("\n");
			sb.append("		}").append("\n");
			sb.append("	}").append("\n");
			sb.append("}").append("\n");
			sb.append("\n");
			return sb.toString();
		}
	}
}
