package dev.nokee.docs;

import lombok.*;
import org.gradle.api.tasks.Input;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PluginManagementBlock {
	private static final PluginManagementBlock NONE = new NonePluginManagementBlock();

	public abstract KotlinDslPluginManagementBlock asKotlinDsl();

	public abstract GroovyDslPluginManagementBlock asGroovyDsl();

	public abstract PluginManagementBlock withRepository(String repository);

	public static PluginManagementBlockBuilder builder() {
		return new PluginManagementBlockBuilder();
	}

	public static PluginManagementBlock none() {
		return NONE;
	}

	public static PluginManagementBlock nokee(String version) {
		return builder()
			.withPluginNamespace("dev.nokee")
			.withVersion(version)
			.withRepository("https://repo.nokeedev.net/release")
			.withRepository("https://repo.nokeedev.net/snapshot")
			.withVersionVariableName("nokeeVersion")
			.build();
	}

	@AllArgsConstructor
	public static class PluginManagementBlockBuilder {
		@With private List<String> repositories;
		@With private final String pluginNamespace;
		@With private final String versionVariableName;
		@With private final String version;

		public PluginManagementBlockBuilder() {
			this(Collections.emptyList(), null, null, null);
		}

		public PluginManagementBlockBuilder withRepository(String repository) {
			List<String> result = new ArrayList<>();
			result.addAll(repositories);
			result.add(repository);
			return withRepositories(result);
		}

		public PluginManagementBlock build() {
			return new DefaultPluginManagementBlock(repositories, pluginNamespace, versionVariableName, version);
		}
	}

	@EqualsAndHashCode(callSuper = false)
	private static class NonePluginManagementBlock extends PluginManagementBlock {
		@Override
		public PluginManagementBlock withRepository(String repository) {
			return this;
		}

		@Override
		public KotlinDslPluginManagementBlock asKotlinDsl() {
			return new KotlinDslPluginManagementBlock() {
				@Override
				public KotlinDslPluginManagementBlock configureFromInitScript() {
					return this;
				}

				@Override
				public String toString() {
					return "";
				}
			};
		}

		@Override
		public GroovyDslPluginManagementBlock asGroovyDsl() {
			return new GroovyDslPluginManagementBlock() {
				@Override
				public GroovyDslPluginManagementBlock configureFromInitScript() {
					return this;
				}

				@Override
				public String toString() {
					return "";
				}
			};
		}
	}

	@ToString
	@AllArgsConstructor
	@EqualsAndHashCode(callSuper = false)
	private static class DefaultPluginManagementBlock extends PluginManagementBlock {
		@Getter(onMethod_={@Input}) @With private final List<String> repositories;
		@Getter(onMethod_={@Input}) private final String pluginNamespace;
		@Getter(onMethod_={@Input}) private final String versionVariableName;
		@Getter(onMethod_={@Input}) private final String version;

		public DefaultPluginManagementBlock withRepository(String repository) {
			List<String> result = new ArrayList<>();
			result.addAll(repositories);
			result.add(repository);
			return withRepositories(result);
		}

		public KotlinDslPluginManagementBlock asKotlinDsl() {
			return new DefaultKotlinDslPluginManagementBlock(this);
		}

		public GroovyDslPluginManagementBlock asGroovyDsl() {
			return new DefaultGroovyDslPluginManagementBlock(this);
		}
	}

	public interface KotlinDslPluginManagementBlock {
		KotlinDslPluginManagementBlock configureFromInitScript();
	}

	public interface GroovyDslPluginManagementBlock {
		GroovyDslPluginManagementBlock configureFromInitScript();
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private static class DefaultKotlinDslPluginManagementBlock implements KotlinDslPluginManagementBlock {
		private final DefaultPluginManagementBlock self;

		@Override
		public KotlinDslPluginManagementBlock configureFromInitScript() {
			return new KotlinDslPluginManagementBlock() {
				@Override
				public KotlinDslPluginManagementBlock configureFromInitScript() {
					return this;
				}

				@Override
				public String toString() {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintWriter out = new PrintWriter(outStream);
					out.println("settingsEvaluated { settings ->");
					out.println("	settings." + DefaultKotlinDslPluginManagementBlock.this.toString());
					out.println("}");
					out.println();
					out.flush();;
					return outStream.toString();
				}
			};
		}

		@Override
		public String toString() {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(outStream);
			out.println("pluginManagement {");
			if (!self.repositories.isEmpty()) {
				out.println("	repositories {");
				out.println("		gradlePluginPortal()");
				self.repositories.stream().map(DefaultKotlinDslPluginManagementBlock::maven).map(it -> "\t\t" + it).forEach(out::println);
				out.println("	}");
			}
			out.println("	val " + self.versionVariableName + " = \"" + self.version + "\"");
			out.println("	resolutionStrategy {");
			out.println("		eachPlugin {");
			out.println("			if (requested.id.id.startsWith(\"" + self.pluginNamespace + ".\")) {");
			out.println("				useModule(\"${requested.id.id}:${requested.id.id}.gradle.plugin:${" + self.versionVariableName + "}\")");
			out.println("			}");
			out.println("		}");
			out.println("	}");
			out.println("}");
			out.println();
			out.flush();
			return outStream.toString();
		}

		private static String maven(String repositoryUri) {
			return "maven { url = uri(\"" + repositoryUri + "\") }";
		}
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	private static class DefaultGroovyDslPluginManagementBlock implements GroovyDslPluginManagementBlock {
		private final DefaultPluginManagementBlock self;

		@Override
		public GroovyDslPluginManagementBlock configureFromInitScript() {
			return new GroovyDslPluginManagementBlock() {
				@Override
				public GroovyDslPluginManagementBlock configureFromInitScript() {
					return this;
				}

				@Override
				public String toString() {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintWriter out = new PrintWriter(outStream);
					out.println("settingsEvaluated { settings ->");
					out.println("	settings." + DefaultGroovyDslPluginManagementBlock.this.toString());
					out.println("}");
					out.println();
					out.flush();
					return outStream.toString();
				}
			};
		}

		@Override
		public String toString() {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintWriter out = new PrintWriter(outStream);
			out.println("pluginManagement {");
			if (!self.repositories.isEmpty()) {
				out.println("	repositories {");
				out.println("		gradlePluginPortal()");
				self.repositories.stream().map(DefaultGroovyDslPluginManagementBlock::maven).map(it -> "\t\t" + it).forEach(out::println);
				out.println("	}");
			}
			out.println("	def " + self.versionVariableName + " = \"" + self.version + "\"");
			out.println("	resolutionStrategy {");
			out.println("		eachPlugin {");
			out.println("			if (requested.id.id.startsWith('" + self.pluginNamespace + ".')) {");
			out.println("				useModule(\"${requested.id.id}:${requested.id.id}.gradle.plugin:${" + self.versionVariableName + "}\")");
			out.println("			}");
			out.println("		}");
			out.println("	}");
			out.println("}");
			out.println();
			out.flush();
			return outStream.toString();
		}

		private static String maven(String repositoryUri) {
			return "maven { url = '" + repositoryUri + "' }";
		}
	}
}
