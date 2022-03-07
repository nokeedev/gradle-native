/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nokeedocs;

import net.nokeedev.jbake.JBakeExtension;
import dev.gradleplugins.dockit.samples.Sample;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static net.nokeedev.jbake.JBakeExtension.jbake;

class NokeeSamplesPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.gradleplugins.documentation.samples");
		project.getPluginManager().apply(JBakeAsciidocLanguagePlugin.class);

		samples(project, samples -> {
			samples.configureEach(new UseLatestGlobalAvailableGradleWrapper(project));
		});
		samples(project, new LoadAllSamples(project.file("src/docs/samples").toPath()));

		project.getPluginManager().withPlugin("net.nokeedev.jbake-site", ignored -> {
			samples(project, new Action<NamedDomainObjectContainer<Sample>>() {
				@Override
				public void execute(NamedDomainObjectContainer<Sample> samples) {
					samples.configureEach(new RegisterJBakeExtension<>(project));

					samples.configureEach(new JBakeAssets<>(toPermalink(new AllSampleArchiveArtifacts())));
					samples.configureEach(new JBakeAssets<>(toPermalink(new AllGifs<>())));
					samples.configureEach(new JBakeAssets<>(toPermalink(new AllPngs<>())));
					samples.configureEach(new JBakeAssets<>(toPermalink(new AllCompiledDots<>(project))));

					samples.configureEach(new JBakeContent<>(toPermalink(new SampleReadMe())));
					samples.configureEach(new JBakeContent<>(new AllDslsContent()));
				}

				private BiConsumer<Sample, CopySpec> toPermalink(BiConsumer<? super Sample, ? super CopySpec> action) {
					return (sample, rootSpec) -> rootSpec.into(sample.getPermalink(), spec -> action.accept(sample, spec));
				}
			});

			jbake(project, it -> it.getConfigurations().put("archiveversion", project.getVersion().toString()));
			jbake(project, it -> it.getConfigurations().put("asciidoctor.attributes.export", "true"));
			jbake(project, new Action<JBakeExtension>() {
				@Override
				public void execute(JBakeExtension extension) {
					extension.getContent().from(extension.sync("samplesContent", toSamples(spec -> spec.from(samples(project).flatMap(collect(JBakeExtension::getContent))))));
					extension.getAssets().from(extension.sync("samplesAssets", toSamples(spec -> spec.from(samples(project).flatMap(collect(JBakeExtension::getAssets))))));
					extension.getTemplates().from(samples(project).flatMap(collect(JBakeExtension::getTemplates)));
					extension.getConfigurations().putAll(samples(project).flatMap(collectAllConfigurations()));
				}

				private Transformer<Provider<Set<FileSystemLocation>>, Set<Sample>> collect(Function<? super JBakeExtension, ? extends FileCollection> mapper) {
					return it -> {
						final ConfigurableFileCollection result = project.getObjects().fileCollection();
						for (Sample sample : it) {
							result.from(mapper.apply(jbake(sample)));
						}
						return result.getElements();
					};
				}

				private Transformer<Provider<Map<String, Object>>, Set<Sample>> collectAllConfigurations() {
					return it -> {
						final MapProperty<String, Object> result = project.getObjects().mapProperty(String.class, Object.class);
						for (Sample sample : it) {
							result.putAll(jbake(sample).getConfigurations());
						}
						return result;
					};
				}

				private Action<CopySpec> toSamples(Action<? super CopySpec> action) {
					return spec -> spec.into("samples", action);
				}
			});
		});
	}

	private static void samples(Project project, Action<? super NamedDomainObjectContainer<Sample>> action) {
		action.execute((NamedDomainObjectContainer<Sample>) project.getExtensions().getByName("samples"));
	}

	private static Provider<Set<Sample>> samples(Project project) {
		return project.provider(() -> (NamedDomainObjectContainer<Sample>) project.getExtensions().getByName("samples"));
	}
}
