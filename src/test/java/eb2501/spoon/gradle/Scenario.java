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

import com.google.common.base.CaseFormat;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Scenario {

    public static void unfold(final String name, final File folder) {
        try {
            final File root = new File(Scenario.class.getResource("scenario/" + name).toURI());
            for (final File file : FileUtils.listFiles(root, null, true)) {
                final Path relative = root.toPath().relativize(file.toPath());
                final File target = Paths.get(folder.getAbsolutePath(), relative.toString()).toFile();
                FileUtils.copyFile(file, target);
            }
        }
        catch (final URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unfold(final Class<?> cls, final File folder) {
        final String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, cls.getSimpleName());
        unfold(name, folder);
    }

    public static void generateJars(final String name, final File folder) {
        try {
            final TemporaryFolder temp = new TemporaryFolder(folder);
            temp.create();

            // Unfold resources
            Scenario.unfold(name, temp.getRoot());

            // Create the JAR
            GradleRunner.create()
                    .withPluginClasspath()
                    .withProjectDir(temp.getRoot())
                    .withArguments("jar")
                    .build();

            // Finally extract the JAR
            final File[] files = new File(temp.getRoot(), "build/libs").listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (file.getName().endsWith(".jar")) {
                        FileUtils.copyFile(file, new File(folder, file.getName()));
                    }
                }
            }

            // Cleanup
            temp.delete();
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
