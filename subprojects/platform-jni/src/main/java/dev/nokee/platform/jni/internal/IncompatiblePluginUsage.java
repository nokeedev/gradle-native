package dev.nokee.platform.jni.internal;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
public class IncompatiblePluginUsage {
	private final Project project;

	public static IncompatiblePluginUsage forProject(Project project) {
		return new IncompatiblePluginUsage(project);
	}

	public IncompatiblePluginUsage assertPluginIds(Set<String> incompatiblePluginIds, AdviceRepository recommendationRepository) {
		Set<String> appliedIncompatiblePluginIds = incompatiblePluginIds.stream().filter(project.getPluginManager()::hasPlugin).collect(Collectors.toSet());
		if (!appliedIncompatiblePluginIds.isEmpty()) {
			throw new GradleException(new IncompatiblePluginUsageMessageBuilder().inProject(project).withIncompatiblePluginIds(appliedIncompatiblePluginIds).withRecommendation(recommendationRepository).build());
		}

		// Register actions to trigger when the plugin is applied
		incompatiblePluginIds.forEach(pluginId -> {
			project.getPluginManager().withPlugin(pluginId, appliedPlugin -> { throw new GradleException(new IncompatiblePluginUsageMessageBuilder().inProject(project).withIncompatiblePluginId(appliedPlugin.getId()).withRecommendation(recommendationRepository).build()); });
		});
		return this;
	}

	public IncompatiblePluginUsage assertPluginId(String pluginId, AdviceRepository recommendationRepository) {
		project.getPluginManager().withPlugin(pluginId, appliedPlugin -> { throw new GradleException(new IncompatiblePluginUsageMessageBuilder().inProject(project).withIncompatiblePluginId(pluginId).withRecommendation(recommendationRepository).build()); });
		return this;
	}

	public IncompatiblePluginUsage assertPluginClass(Class<? extends Plugin<?>> pluginClass, AdviceRepository recommendationRepository) {
		project.getPlugins().withType(pluginClass, appliedPlugin -> { throw new GradleException(new IncompatiblePluginUsageMessageBuilder().inProject(project).withIncompatiblePluginId(pluginClass.getCanonicalName()).withRecommendation(recommendationRepository).build()); });
		return this;
	}

	public interface AdviceRepository {
		void findAdvice(String incompatiblePluginId, Context context);
	}

	public static class IncompatiblePluginUsageMessageBuilder {
		private List<String> incompatiblePluginIds;
		private String projectPath;
		private AdviceRepository recommendation;

		public IncompatiblePluginUsageMessageBuilder withIncompatiblePluginIds(Iterable<String> incompatiblePluginIds) {
			this.incompatiblePluginIds = ImmutableList.copyOf(incompatiblePluginIds);
			return this;
		}

		public IncompatiblePluginUsageMessageBuilder withIncompatiblePluginId(String incompatiblePluginId) {
			this.incompatiblePluginIds = ImmutableList.of(incompatiblePluginId);
			return this;
		}

		public IncompatiblePluginUsageMessageBuilder withRecommendation(AdviceRepository recommendation) {
			this.recommendation = recommendation;
			return this;
		}

		public IncompatiblePluginUsageMessageBuilder inProject(Project project) {
			this.projectPath = project.getPath();
			return this;
		}

		public String build() {
			StringBuilder builder = new StringBuilder();
			builder.append("Nokee detected the usage of incompatible plugins in the project '").append(projectPath).append("'.\n");

			Set<Advice> advices = new LinkedHashSet<>();
			List<Footnote> footnotes = new ArrayList<>();
			Context context = new Context() {
				@Override
				public AdviceBuilder advice(String message) {
					Advice advice = new Advice(message);
					advices.add(advice);
					return new AdviceBuilder() {
						@Override
						public AdviceBuilder withFootnote(String note) {
							advice.linkToFootnote(footnotes.stream().filter(it -> it.footnote.equals(note)).findFirst().orElseGet(() -> {
								Footnote footnote = new Footnote(footnotes.size() + 1, note);
								footnotes.add(footnote);
								return footnote;
							}));
							return this;
						}
					};
				}
			};
			incompatiblePluginIds.forEach(it -> recommendation.findAdvice(it, context));


			builder.append("We recommend taking the following ");
			if (advices.size() == 1) {
				builder.append("action");
			} else {
				builder.append("actions");
			}
			builder.append(":\n");

			advices.stream().forEach(it -> builder.append(" * ").append(it.asAdvice()).append("\n"));

			builder.append("\nTo learn more, visit https://nokee.dev/docs/incompatible-plugins");
			if (!footnotes.isEmpty()) {
				builder.append("\n\n");
				for (Footnote it : footnotes) {
					builder.append(it.asFootnote()).append("\n");
				}
			}

			return builder.toString();
		}
	}

	@EqualsAndHashCode
	@RequiredArgsConstructor
	private static class Footnote {
		private final int number;
		private final String footnote;

		public String asReference() {
			return "[" + number + "]";
		}

		public String asFootnote() {
			return asReference() + " " + footnote;
		}
	}

	@EqualsAndHashCode(of = {"advice"})
	@RequiredArgsConstructor
	private static class Advice {
		private final String advice;
		private final List<Footnote> footnotes = new ArrayList<>();

		public Advice linkToFootnote(Footnote footnote) {
			footnotes.add(footnote);
			return this;
		}

		public String asAdvice() {
			if (footnotes.isEmpty()) {
				return advice;
			}
			return advice + " " + footnotes.stream().map(Footnote::asReference).collect(joining());
		}
	}

	public interface Context {
		AdviceBuilder advice(String advice);
	}

	public interface AdviceBuilder {
		AdviceBuilder withFootnote(String footnote);
	}
}
