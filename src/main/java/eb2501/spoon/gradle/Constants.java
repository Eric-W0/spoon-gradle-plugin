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

public class Constants {
    public static final String SPOON_SUFFIX = "_S_";
    public static final String TEMPLATE_SUFFIX = "_T_";
    public static final String RESOURCE_SUFFIX = "_R_";

    public final static String[] SUFFIXES = {
            Constants.SPOON_SUFFIX,
            Constants.TEMPLATE_SUFFIX,
            Constants.RESOURCE_SUFFIX
    };

    public static final String CONFIGURATION_NAME = "spoonCompile";
    public static final String TEST_CONFIGURATION_NAME = "spoonCompileTest";
    public static final String COMPILE_TASK_NAME = "spoonCompile%s";
    public static final String PROCESS_RESOURCES_TASK_NAME = "spoonProcessResources%s";
}
