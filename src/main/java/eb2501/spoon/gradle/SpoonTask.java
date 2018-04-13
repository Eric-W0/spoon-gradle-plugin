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

package eb2501.spoon.gradle;

import eb2501.spoon.SpoonApiBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.JavaCompile;
import spoon.SpoonAPI;
import spoon.compiler.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class SpoonTask extends DefaultTask {
    SourceSet sourceSet;
    private Cache<FileCollection> source;
    private Cache<FileCollection> template;
    private Cache<File> generated;
    private Cache<Integer> complianceLevel;
    private SpoonTaskMode mode = SpoonTaskMode.NO_GUI;
    private boolean autoImports = true;
    private boolean preserveLineNumbers = true;
    private int tabulationSize = -1;
    private Boolean preserveComments = true;
    private Boolean skipSelfChecks = false;
    private Cache<String> encoding;
    private Cache<String> loggingLevel;
    private Cache<List<String>> processors;
    private Cache<FileCollection> classpath;

    private FileCollection getDefaultSource() {
        return getProject()
                .files(sourceSet.getJava())
                .filter(f -> f.getName().endsWith(Constants.SPOON_SUFFIX + ".java"));
    }

    private FileCollection getDefaultTemplate() {
        return getProject()
                .files(sourceSet.getJava())
                .filter(f -> f.getName().endsWith(Constants.TEMPLATE_SUFFIX + ".java"));
    }

    private File getDefaultGenerated() {
        return new File(getProject().getBuildDir(), "generated/source/spoon/" + sourceSet.getName());
    }

    private int getDefaultComplianceLevel() {
        final JavaPluginConvention java = getProject().getConvention().getPlugin(JavaPluginConvention.class);
        final JavaVersion version = java.getSourceCompatibility();
        switch (version) {
            case VERSION_1_1:
                return 1;

            case VERSION_1_2:
                return 2;

            case VERSION_1_3:
                return 3;

            case VERSION_1_4:
                return 4;

            case VERSION_1_5:
                return 5;

            case VERSION_1_6:
                return 6;

            case VERSION_1_7:
                return 7;

            case VERSION_1_8:
                return 8;

            case VERSION_1_9:
                return 9;

            default:
                throw new GradleException(String.format("Doesn't support java source version '%s'", version));
        }
    }

    private String getDefaultEncoding() {
        final JavaCompile compileTask = (JavaCompile)getProject().getTasks().getByName(sourceSet.getCompileJavaTaskName());
        final String encoding = compileTask.getOptions().getEncoding();
        if (encoding == null) {
            return StandardCharsets.UTF_8.name();
        } else {
            return encoding;
        }
    }

    private String getDefaultLoggingLevel() {
        final LogLevel level = getLogging().getLevel();
        if (level == null) {
            return "";
        } else {
            return level.toString();
        }
    }

    private List<String> getDefaultProcessors() {
        try {
            final ProcessorGraph graph = new ProcessorGraph();
            graph.readClasspath(getClasspath().getFiles().toArray(new File[0]));
            return graph.getProcessors();
        }
        catch (final IOException e) {
            throw new GradleException("IOException thrown", e);
        }
    }

    private FileCollection getDefaultClasspath() {
        if (sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME)) {
            final JavaPluginConvention java = getProject().getConvention().getPlugin(JavaPluginConvention.class);
            return getProject().getConfigurations().getByName(Constants.TEST_CONFIGURATION_NAME)
                    .plus(java.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getRuntimeClasspath());
        } else {
            return getProject().getConfigurations().getByName(Constants.CONFIGURATION_NAME);
        }
    }

    public SpoonTask() {
        source = new Cache<>(this::getDefaultSource);
        template = new Cache<>(this::getDefaultTemplate);
        generated = new Cache<>(this::getDefaultGenerated);
        complianceLevel = new Cache<>(this::getDefaultComplianceLevel);
        encoding = new Cache<>(this::getDefaultEncoding);
        loggingLevel = new Cache<>(this::getDefaultLoggingLevel);
        processors = new Cache<>(this::getDefaultProcessors);
        classpath = new Cache<>(this::getDefaultClasspath);
    }

    @SkipWhenEmpty
    @InputFiles
    public FileCollection getSource() {
        return source.get();
    }

    @InputFiles
    public FileCollection getTemplate() {
        return template.get();
    }

    @OutputDirectory
    public File getGenerated() {
        return generated.get();
    }

    public void setGenerated(final File generated) {
        this.generated.set(generated);
    }

    @Input
    public Integer getComplianceLevel() {
        return complianceLevel.get();
    }

    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel.set(complianceLevel);
    }

    @Console
    public SpoonTaskMode getMode() {
        return mode;
    }

    public void setMode(final SpoonTaskMode mode) {
        this.mode = mode;
    }

    @Input
    public boolean getAutoImports() {
        return autoImports;
    }

    public void setAutoImports(final boolean autoImports) {
        this.autoImports = autoImports;
    }

    @Input
    public boolean getPreserveLineNumbers() {
        return preserveLineNumbers;
    }

    public void setPreserveLineNumbers(final boolean preserveLineNumbers) {
        this.preserveLineNumbers = preserveLineNumbers;
    }

    @Input
    public int getTabulationSize() {
        return tabulationSize;
    }

    public void setTabulationSize(final int tabulationSize) {
        this.tabulationSize = tabulationSize;
    }

    @Input
    public boolean getPreserveComments() {
        return preserveComments;
    }

    public void setPreserveComments(final boolean preserveComments) {
        this.preserveComments = preserveComments;
    }

    @Input
    public boolean getSkipSelfChecks() {
        return skipSelfChecks;
    }

    public void setSkipSelfChecks(final boolean skipSelfChecks) {
        this.skipSelfChecks = skipSelfChecks;
    }

    @Input
    public String getEncoding() {
        return encoding.get();
    }

    public void setEncoding(final String encoding) {
        this.encoding.set(encoding);
    }

    @Console
    public String getLoggingLevel() {
        return loggingLevel.get();
    }

    public void setLoggingLevel(final String loggingLevel) {
        this.loggingLevel.set(loggingLevel);
    }

    @Input
    public List<String> getProcessors() {
        return processors.get();
    }

    List<String> getRealProcessors() {
        if (mode == SpoonTaskMode.GUI_BEFORE) {
            return Collections.emptyList();
        } else {
            return getProcessors();
        }
    }

    File getRealGenerated() {
        if (mode == SpoonTaskMode.NO_GUI) {
            return getGenerated();
        } else {
            return null;
        }
    }

    List<File> getRealClasspath() {
        return getClasspath().getFiles().stream()
                .filter(File::exists)
                .collect(Collectors.toList());
    }

    @CompileClasspath
    public FileCollection getClasspath() {
        return classpath.get();
    }

    public void setClasspath(final FileCollection classpath) {
        this.classpath.set(classpath);
    }

    @TaskAction
    public void run() {

        // Build the SpoonApi
        final SpoonApiBuilder builder = new SpoonApiBuilder()
                .withLoggingLevel(getLoggingLevel())
                .withComplianceLevel(getComplianceLevel())
                .withAutoImports(getAutoImports())
                .withPreserveLineNumbers(getPreserveLineNumbers())
                .withTabulationSize(getTabulationSize())
                .withPreserveComments(getPreserveComments())
                .withSkipSelfChecks(getSkipSelfChecks())
                .withEncoding(Charset.forName(getEncoding()))
                .withInputSources(new ArrayList<>(getSource().getFiles()))
                .withOutputDirectory(getRealGenerated())
                .withProcessorNames(getRealProcessors())
                .withSourceClasspath(getRealClasspath());

        final Set<File> templateFiles = this.template.get().getFiles();
        if (!templateFiles.isEmpty()) {
            builder.withTemplateSources(new ArrayList<>(templateFiles));
        }

        if (mode != SpoonTaskMode.GUI_BEFORE) {
            builder.withProcessorInstances(
                    new TypeProcessor(),
                    new TypeRefProcessor()
            );
        }

        final SpoonAPI spoon = builder.build();

        // Run it!
        spoon.run();
        final Environment environment = spoon.getEnvironment();
        if (environment.getErrorCount() > 0) {
            throw new GradleException(String.format(
                    "Spoon processing generated %d errors!",
                    environment.getErrorCount()
            ));
        } else {
            if (getMode() != SpoonTaskMode.NO_GUI) {
                SpoonGui.show(spoon.getFactory());
            }
        }
    }
}
