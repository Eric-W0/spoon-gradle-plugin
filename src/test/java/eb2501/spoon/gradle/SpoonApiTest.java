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
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class SpoonApiTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void before() {
        Scenario.unfold(getClass(), tempFolder.getRoot());
    }

    @Test
    public void testSpoonApi() {
        GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(tempFolder.getRoot())
                .withArguments(
                        "test",
                        "-q",
                        String.format("-Ptest_classpath=%s", System.getProperty("test_classpath"))
                )
                .build();
        final File file = new File(
                tempFolder.getRoot(),
                "build/generated/source/spoon/test/eb2501/ephemeral/First.java"
        );
        Assert.assertFalse(file.exists());
    }
}
