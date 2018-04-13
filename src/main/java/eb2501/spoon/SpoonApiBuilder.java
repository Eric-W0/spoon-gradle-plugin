/*
 * Copyright 2018 eb2501@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eb2501.spoon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.OutputType;
import spoon.SpoonAPI;
import spoon.compiler.Environment;
import spoon.processing.Processor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpoonApiBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpoonApiFactory.class);

    private Launcher launcher;
    private final List<File> inputSources;
    private final List<File> templateSources;
    private final List<String> processorNames;
    private final List<Processor<?>> processorInstances;

    public SpoonApiBuilder() {
        launcher = new Launcher();
        launcher.getEnvironment().setOutputType(OutputType.NO_OUTPUT);
        inputSources = new ArrayList<>();
        templateSources = new ArrayList<>();
        processorNames = new ArrayList<>();
        processorInstances = new ArrayList<>();
    }

    public SpoonAPI build() {

        // Doing the lazy initialization
        inputSources.forEach(i -> {
            if (i.exists()) {
                launcher.getModelBuilder().addInputSource(i);
            }
        });
        templateSources.forEach(t -> {
            if (t.exists()) {
                launcher.getModelBuilder().addTemplateSource(t);
            }
        });
        processorNames.forEach(p -> launcher.addProcessor(p));
        processorInstances.forEach(p -> launcher.addProcessor(p));

        // Log parameters
        final Environment env = launcher.getEnvironment();
        LOGGER.debug("SpoonApi parameters:");
        LOGGER.debug("===");
        LOGGER.debug("  loggingLevel        = {}", env.getLevel());
        LOGGER.debug("  complianceLevel     = {}", env.getComplianceLevel());
        LOGGER.debug("  autoImports         = {}", env.isAutoImports());
        LOGGER.debug("  preserveLineNumbers = {}", env.isPreserveLineNumbers());
        LOGGER.debug("  tabulationSize      = {}", env.isUsingTabulations() ? env.getTabulationSize() : -1);
        LOGGER.debug("  preserveComments    = {}", env.isCommentsEnabled());
        LOGGER.debug("  skipSelfChecks      = {}", env.checksAreSkipped());
        LOGGER.debug("  encoding            = {}", env.getEncoding());
        LOGGER.debug("  inputSources:");
        for (final File file : inputSources) {
            LOGGER.debug("   - {}", file);
        }
        LOGGER.debug(
                "  outputDirectory     = {}",
                (env.getOutputType() == OutputType.NO_OUTPUT) ? "NULL" : env.getSourceOutputDirectory()
        );
        LOGGER.debug("  templateSources:");
        for (final File file : templateSources) {
            LOGGER.debug("   - {}", file);
        }
        LOGGER.debug("  processorNames:");
        for (final String processor : processorNames) {
            LOGGER.debug("   - {}", processor);
        }
        LOGGER.debug("  classpath:");
        for (final String classpath : launcher.getModelBuilder().getSourceClasspath()) {
            LOGGER.debug("   - {}", classpath);
        }
        LOGGER.debug("===");

        // Doing the rest
        final SpoonAPI result = launcher;
        launcher = null;
        return result;
    }

    private void check() {
        if (launcher == null) {
            throw new IllegalStateException("Has already been built");
        }
    }

    public SpoonApiBuilder withLoggingLevel(final String level) {
        check();
        if (!level.isEmpty()) {
            launcher.getEnvironment().setLevel(level);
        }
        return this;
    }

    public SpoonApiBuilder withComplianceLevel(final int level) {
        check();
        launcher.getEnvironment().setComplianceLevel(level);
        return this;
    }

    public SpoonApiBuilder withAutoImports(final boolean autoImports) {
        check();
        launcher.getEnvironment().setAutoImports(autoImports);
        return this;
    }

    public SpoonApiBuilder withPreserveLineNumbers(final boolean preserveLineNumbers) {
        check();
        launcher.getEnvironment().setPreserveLineNumbers(preserveLineNumbers);
        return this;
    }

    public SpoonApiBuilder withTabulationSize(final int tabulationSize) {
        check();
        if (tabulationSize == -1) {
            launcher.getEnvironment().useTabulations(false);
        } else {
            launcher.getEnvironment().useTabulations(true);
            launcher.getEnvironment().setTabulationSize(tabulationSize);
        }
        return this;
    }

    public SpoonApiBuilder withPreserveComments(final boolean preserveComments) {
        check();
        launcher.getEnvironment().setCommentEnabled(preserveComments);
        return this;
    }

    public SpoonApiBuilder withSkipSelfChecks(final boolean skipSelfChecks) {
        check();
        launcher.getEnvironment().setSelfChecks(skipSelfChecks);
        return this;
    }

    public SpoonApiBuilder withEncoding(final Charset encoding) {
        check();
        launcher.getEnvironment().setEncoding(encoding);
        return this;
    }

    public SpoonApiBuilder withInputSource(final File inputSource) {
        check();
        inputSources.clear();
        inputSources.add(inputSource);
        return this;
    }

    public SpoonApiBuilder withInputSources(final List<File> inputSources) {
        check();
        this.inputSources.clear();
        this.inputSources.addAll(inputSources);
        return this;
    }

    public SpoonApiBuilder withInputSources(final File... inputSources) {
        return withInputSources(Arrays.asList(inputSources));
    }

    public SpoonApiBuilder withOutputDirectory(final File outputDir) {
        check();
        if (outputDir == null) {
            launcher.getEnvironment().setOutputType(OutputType.NO_OUTPUT);
        } else {
            launcher.getEnvironment().setOutputType(OutputType.CLASSES);
            launcher.getEnvironment().setSourceOutputDirectory(outputDir);
        }
        return this;
    }

    public SpoonApiBuilder withTemplateSource(final File templateSource) {
        check();
        templateSources.clear();
        templateSources.add(templateSource);
        return this;
    }

    public SpoonApiBuilder withTemplateSources(final List<File> templateSources) {
        check();
        this.templateSources.clear();
        this.templateSources.addAll(templateSources);
        return this;
    }

    public SpoonApiBuilder withTemplateSources(final File... templateSources) {
        return withTemplateSources(Arrays.asList(templateSources));
    }

    public SpoonApiBuilder withProcessorName(final String processor) {
        check();
        processorNames.clear();
        processorNames.add(processor);
        return this;
    }

    public SpoonApiBuilder withProcessorNames(final List<String> processors) {
        check();
        this.processorNames.clear();
        this.processorNames.addAll(processors);
        return this;
    }

    public SpoonApiBuilder withProcessorNames(final String... processors) {
        return withProcessorNames(Arrays.asList(processors));
    }

    public SpoonApiBuilder withProcessorInstance(final Processor<?> processor) {
        check();
        processorInstances.clear();
        processorInstances.add(processor);
        return this;
    }

    public SpoonApiBuilder withProcessorInstances(final List<Processor<?>> processors) {
        check();
        this.processorInstances.clear();
        this.processorInstances.addAll(processors);
        return this;
    }

    public SpoonApiBuilder withProcessorInstances(final Processor<?>... processors) {
        return withProcessorInstances(Arrays.asList(processors));
    }

    public SpoonApiBuilder withSourceClasspath(final List<File> classpath) {
        check();
        launcher.getModelBuilder().setSourceClasspath(
                classpath
                        .stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList())
                        .toArray(new String[0])
        );
        return this;
    }
}
