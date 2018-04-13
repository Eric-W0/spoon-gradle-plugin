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

import eb2501.spoon.SpoonApiFactory;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;
import org.gradle.plugins.ide.idea.IdeaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SpoonPlugin implements Plugin<Project> {
    private static final String TASK_GROUP = "Spoon";
    private static final String TASK_DESCRIPTION =
            "Precompile Java files on the '%s' source set using the Spoon framework";

    @Override
    public void apply(final Project project) {

        // Rejecting if JavaBasePlugin hasn't been applied
        if (!project.getPlugins().hasPlugin(JavaBasePlugin.class)) {
            throw new GradleException("Must apply a java plugin");
        }

        final JavaPluginConvention java = project.getConvention().getPlugin(JavaPluginConvention.class);
        final IdeaPlugin idea = project.getConvention().findPlugin(IdeaPlugin.class);

        // Adding spoon & spoonTest configuration
        final Configuration config = project.getConfigurations().create(
                Constants.CONFIGURATION_NAME,
                c -> c.setTransitive(false)
        );
        config.extendsFrom(project.getConfigurations().getByName("compile"));
        final Configuration configTest = project.getConfigurations().create(
                Constants.TEST_CONFIGURATION_NAME,
                c -> c.setTransitive(false)
        );
        configTest.extendsFrom(project.getConfigurations().getByName("testCompile"));

        // Create the spoonCompile/spoonProcessResource tasks for each SourceSet
        final Map<SourceSet, SpoonTask> mapping = new HashMap<>();
        java.getSourceSets().all(ss -> {
            final boolean isMain = ss.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME);
            final JavaCompile compileTask = (JavaCompile)project.getTasks().getByName(ss.getCompileJavaTaskName());
            final Copy processResourcesTask = (Copy)project.getTasks().getByName(ss.getProcessResourcesTaskName());

            final SpoonTask spoonCompileTask = project.getTasks().create(
                    String.format(Constants.COMPILE_TASK_NAME, isMain ? "" : StringUtils.capitalize(ss.getName())),
                    SpoonTask.class,
                    t -> {
                        t.sourceSet = ss;
                        t.setDescription(String.format(TASK_DESCRIPTION, ss.getName()));
                        t.setGroup(TASK_GROUP);
                    }
            );
            compileTask.dependsOn(spoonCompileTask);

            final Copy spoonProcessResourcesTask = project.getTasks().create(
                    String.format(Constants.PROCESS_RESOURCES_TASK_NAME, isMain ? "" : StringUtils.capitalize(ss.getName())),
                    Copy.class,
                    t -> {
                        for (final File srcDir : ss.getJava().getSrcDirs()) {
                            t.from(srcDir);
                        }
                        t.into(processResourcesTask.getDestinationDir());
                        t.include(String.format("**/*%s.java", Constants.RESOURCE_SUFFIX));
                    }
            );
            processResourcesTask.dependsOn(spoonProcessResourcesTask);

            mapping.put(ss, spoonCompileTask);
        });

        // Doing post-evaluation work
        project.afterEvaluate(p -> java.getSourceSets().all(ss -> {
            final SpoonTask spoonCompileTask = mapping.get(ss);

            // Add generated files to the list of sources to be compiled
            ss.getJava().srcDir(spoonCompileTask.getGenerated());

            // Exclude the suffixed files from the compilation process
            final JavaCompile compileTask = (JavaCompile)project.getTasks().getByName(ss.getCompileJavaTaskName());
            for (final String suffix : Constants.SUFFIXES) {
                compileTask.exclude(String.format("**/*%s.java", suffix));
            }

            // Add Idea hints
            if (idea != null) {
                idea.getModel().getModule().getGeneratedSourceDirs().add(spoonCompileTask.getGenerated());
            }

            // For 'test', we want to set the system properties for SpoonApiBuilder
            if (ss.getName().equals(SourceSet.TEST_SOURCE_SET_NAME)) {
                final Test testTask = (Test)project.getTasks().getByName("test");

                testTask.doFirst(t -> {
                    testTask.systemProperty(
                            SpoonApiFactory.INPUT_SOURCES_KEY,
                            SpoonApiFactory.renderFiles(spoonCompileTask.getSource().getFiles())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.TEMPLATE_SOURCES_KEY,
                            SpoonApiFactory.renderFiles(spoonCompileTask.getTemplate().getFiles())
                    );

                    final File generated = spoonCompileTask.getRealGenerated();
                    if (generated != null) {
                        testTask.systemProperty(SpoonApiFactory.OUTPUT_DIRECTORY_KEY, generated.getAbsolutePath());
                    }

                    testTask.systemProperty(
                            SpoonApiFactory.LOGGING_LEVEL_KEY,
                            spoonCompileTask.getLoggingLevel()
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.COMPLIANCE_LEVEL_KEY,
                            Integer.toString(spoonCompileTask.getComplianceLevel())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.AUTO_IMPORTS_KEY,
                            Boolean.toString(spoonCompileTask.getAutoImports())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.PRESERVE_LINE_NUMBERS_KEY,
                            Boolean.toString(spoonCompileTask.getPreserveLineNumbers())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.TABULATION_SIZE_KEY,
                            Integer.toString(spoonCompileTask.getTabulationSize())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.PRESERVE_COMMENTS_KEY,
                            Boolean.toString(spoonCompileTask.getPreserveComments())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.SKIP_SELF_CHECKS_KEY,
                            Boolean.toString(spoonCompileTask.getSkipSelfChecks())
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.ENCODING_KEY,
                            spoonCompileTask.getEncoding()
                    );

                    testTask.systemProperty(
                            SpoonApiFactory.SOURCE_CLASSPATH_KEY,
                            SpoonApiFactory.renderFiles(spoonCompileTask.getRealClasspath())
                    );
                });
            }
        }));
    }
}
