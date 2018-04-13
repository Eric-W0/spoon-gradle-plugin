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

import org.apache.commons.lang3.StringUtils;
import spoon.SpoonAPI;
import spoon.processing.Processor;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SpoonApiFactory {
    private static final String PREFIX = "eb2501.spoon.";

    public static final String INPUT_SOURCES_KEY         = PREFIX + "inputSources";
    public static final String TEMPLATE_SOURCES_KEY      = PREFIX + "templateSources";
    public static final String OUTPUT_DIRECTORY_KEY      = PREFIX + "outputDirectory";
    public static final String LOGGING_LEVEL_KEY         = PREFIX + "loggingLevel";
    public static final String COMPLIANCE_LEVEL_KEY      = PREFIX + "complianceLevel";
    public static final String AUTO_IMPORTS_KEY          = PREFIX + "autoImports";
    public static final String PRESERVE_LINE_NUMBERS_KEY = PREFIX + "preserveLineNumbers";
    public static final String TABULATION_SIZE_KEY       = PREFIX + "tabulationSize";
    public static final String PRESERVE_COMMENTS_KEY     = PREFIX + "preserveComments";
    public static final String SKIP_SELF_CHECKS_KEY      = PREFIX + "skipSelfChecks";
    public static final String ENCODING_KEY              = PREFIX + "encoding";
    public static final String SOURCE_CLASSPATH_KEY      = PREFIX + "sourceClasspath";

    public static List<File> parseFiles(final String text) {
        return Arrays.stream(text.split("[" + File.pathSeparator + "]"))
                .map(File::new)
                .collect(Collectors.toList());
    }

    public static String renderFiles(final Collection<File> files) {
        return StringUtils.join(
                files.stream().map(File::getAbsolutePath).collect(Collectors.toList()),
                File.pathSeparator
        );
    }

    public static SpoonApiBuilder createDefaultBuilder() {
        final SpoonApiBuilder builder = new SpoonApiBuilder();

        final String inputSources = System.getProperty(INPUT_SOURCES_KEY);
        if (inputSources != null) {
            builder.withInputSources(parseFiles(inputSources));
        }

        final String templateSources = System.getProperty(TEMPLATE_SOURCES_KEY);
        if (templateSources != null) {
            builder.withTemplateSources(parseFiles(templateSources));
        }

        final String genDir = System.getProperty(OUTPUT_DIRECTORY_KEY);
        if (genDir != null) {
            builder.withOutputDirectory(new File(genDir));
        }

        final String loggingLevel = System.getProperty(LOGGING_LEVEL_KEY);
        if (loggingLevel != null) {
            builder.withLoggingLevel(loggingLevel);
        }

        final String complianceLevel = System.getProperty(COMPLIANCE_LEVEL_KEY);
        if (complianceLevel != null) {
            builder.withComplianceLevel(Integer.parseInt(complianceLevel));
        }

        final String autoImports = System.getProperty(AUTO_IMPORTS_KEY);
        if (autoImports != null) {
            builder.withAutoImports(Boolean.parseBoolean(autoImports));
        }

        final String preserveLineNumbers = System.getProperty(PRESERVE_LINE_NUMBERS_KEY);
        if (preserveLineNumbers != null) {
            builder.withPreserveLineNumbers(Boolean.parseBoolean(preserveLineNumbers));
        }

        final String tabulationSize = System.getProperty(TABULATION_SIZE_KEY);
        if (tabulationSize != null) {
            builder.withTabulationSize(Integer.parseInt(tabulationSize));
        }

        final String preserveComments = System.getProperty(PRESERVE_COMMENTS_KEY);
        if (preserveComments != null) {
            builder.withPreserveComments(Boolean.parseBoolean(preserveComments));
        }

        final String skipSelfChecks = System.getProperty(SKIP_SELF_CHECKS_KEY);
        if (skipSelfChecks != null) {
            builder.withSkipSelfChecks(Boolean.parseBoolean(skipSelfChecks));
        }

        final String encoding = System.getProperty(ENCODING_KEY);
        if (encoding != null) {
            builder.withEncoding(Charset.forName(encoding));
        }

        final String sourceClasspath = System.getProperty(SOURCE_CLASSPATH_KEY);
        if (sourceClasspath != null) {
            System.err.println("DEBUG! " + sourceClasspath);
            builder.withSourceClasspath(parseFiles(sourceClasspath));
        }

        return builder;
    }

    public static SpoonAPI createTest(final URL input, final String processor) {
        try {
            return createDefaultBuilder()
                    .withInputSource(new File(input.toURI()))
                    .withProcessorName(processor)
                    .withOutputDirectory(null)
                    .build();
        }
        catch (final URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Processor<?>> SpoonAPI createTest(final URL input, final Class<T> processor) {
        return createTest(input, processor.getName());
    }
}
