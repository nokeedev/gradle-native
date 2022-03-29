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
package dev.nokee.platform.cpp.results;

import dev.nokee.platform.cpp.BuildExperimentResult;
import dev.nokee.platform.cpp.OutputDirSelector;
import org.gradle.profiler.BenchmarkResultCollector;
import org.gradle.profiler.InvocationSettings;
import org.gradle.profiler.ScenarioDefinition;
import org.gradle.profiler.report.AbstractGenerator;
import org.gradle.profiler.report.BenchmarkResult;
import org.gradle.profiler.report.CsvGenerator;
import org.gradle.profiler.report.HtmlGenerator;
import org.gradle.profiler.result.BuildInvocationResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public final class GradleProfilerReporter implements DataReporter<PerformanceTestResult> {
    private final OutputDirSelector outputDirSelector;
    private final CompositeReportGenerator compositeReportGenerator;
    private final BenchmarkResultCollector resultCollector;

    public GradleProfilerReporter(OutputDirSelector outputDirSelector) {
        this.outputDirSelector = outputDirSelector;
        this.compositeReportGenerator = new CompositeReportGenerator();
        this.resultCollector = new BenchmarkResultCollector(compositeReportGenerator);
    }

	public void report(PerformanceTestResult results) {
		for (BuildExperimentResult result : results) {
			@SuppressWarnings("unchecked")
			GradleProfilerResult<ScenarioDefinition, BuildInvocationResult> r = (GradleProfilerResult<ScenarioDefinition, BuildInvocationResult>) result.getExecutionResult();
			Consumer<? super BuildInvocationResult> scenarioReporter = resultCollector.scenario(
				r.getScenarioDefinition(),
				r.getScenarioInvoker().samplesFor(r.getInvocationSettings(), r.getScenarioDefinition())
			);
			r.getInvocationResults().forEach(scenarioReporter::accept);
		}

        PerformanceExperiment experiment = results.getPerformanceExperiment();
        File baseDir = outputDirSelector.outputDirFor(experiment.getScenario().getTestName());
        baseDir.mkdirs();
        compositeReportGenerator.setGenerators(Arrays.asList(
            new CsvGenerator(new File(baseDir, "benchmark.csv"), CsvGenerator.Format.LONG),
            new HtmlGenerator(new File(baseDir, "benchmark.html"))
        ));

        resultCollector.summarizeResults(line ->
            System.out.println("  " + line)
        );
        try {
            InvocationSettings settings = new InvocationSettings.InvocationSettingsBuilder()
                .setBenchmarkTitle(experiment.getDisplayName())
                .build();
            resultCollector.write(settings);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

//    @Override
//    public void close() {
//    }

    private static class CompositeReportGenerator extends AbstractGenerator {

        List<AbstractGenerator> generators;

        public CompositeReportGenerator() {
            super(null);
        }

        public void setGenerators(List<AbstractGenerator> generators) {
            this.generators = generators;
        }

        @Override
        public void write(InvocationSettings settings, BenchmarkResult result) throws IOException {
            for (AbstractGenerator generator : generators) {
                generator.write(settings, result);
            }
        }

        @Override
        public void summarizeResults(Consumer<String> consumer) {
            for (AbstractGenerator generator : generators) {
                generator.summarizeResults(consumer);
            }
        }

        @Override
        protected void write(InvocationSettings settings, BenchmarkResult result, BufferedWriter writer) {
            throw new UnsupportedOperationException();
        }
    }
}
