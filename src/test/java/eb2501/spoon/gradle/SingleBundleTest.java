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

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.StringWriter;

public class SingleBundleTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void before() {

        // Generate the bundle1 JAR
        Scenario.generateJars("bundle1", tempFolder.getRoot());

        // Unfold the resources folder
        Scenario.unfold(getClass(), tempFolder.getRoot());
    }

    @Test
    public void testSingleBundle() {
        final StringWriter output = new StringWriter();
        GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(tempFolder.getRoot())
                .withArguments("run", "-q")
                .forwardStdOutput(output)
                .build();
        Assert.assertEquals(output.toString(), "2" + System.lineSeparator());
    }
}
