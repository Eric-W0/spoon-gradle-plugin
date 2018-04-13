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

import org.gradle.api.GradleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessorGraph {
    private static final String PROPERTIES_PATH = "META-INF/spoon-bundle.properties";
    private static final Pattern PROCESSOR_PATH_KEY = Pattern.compile("processor\\.([^.]+)\\.path");
    private static final Pattern PROCESSOR_CONSUME_KEY = Pattern.compile("processor\\.([^.]+)\\.consumes");
    private static final Pattern PROCESSOR_PRODUCE_KEY = Pattern.compile("processor\\.([^.]+)\\.produces");

    // Annotation to Processor
    final Map<String, String> processor;

    // Processor to Annotation
    final Map<String, String> annotation;

    // Annotation to List<Annotation>
    final Map<String, List<String>> produces;

    public ProcessorGraph() {
        processor = new HashMap<>();
        annotation = new HashMap<>();
        produces = new HashMap<>();
    }

    public void readClasspath(final File... files) throws IOException {
        for (final File file : files) {
            if (!file.exists()) {
                continue;
            }
            if (file.getName().toLowerCase().endsWith(".jar")) {
                readJarClasspath(file);
            } else if (file.isDirectory()) {
                readFolderClasspath(file);
            }
        }
    }

    private void readJarClasspath(final File file) throws IOException {
        final JarFile jar = new JarFile(file);
        final JarEntry entry = jar.getJarEntry(PROPERTIES_PATH);
        if (entry != null) {
            try (final InputStream istream = jar.getInputStream(entry)) {
                readBundleProperties(file, istream);
            }
        }
    }

    private void readFolderClasspath(final File folder) throws IOException {
        final File file = new File(folder, PROPERTIES_PATH);
        if (file.exists()) {
            try (final InputStream istream = new FileInputStream(file)) {
                readBundleProperties(file, istream);
            }
        }
    }

    private void readBundleProperties(final File file, final InputStream istream) throws IOException {
        final Properties props = new Properties();
        props.load(istream);

        // Name to Annotation
        final Map<String, String> path = new HashMap<>();

        // Name to Annotation
        final Map<String, String> consumes = new HashMap<>();

        // Name to List<Annotation>
        final Map<String, List<String>> produces = new HashMap<>();

        Matcher matcher;
        for (final String key : props.stringPropertyNames()) {

            matcher = PROCESSOR_PATH_KEY.matcher(key);
            if (matcher.matches()) {
                final String name = matcher.group(1);
                final String value = props.getProperty(key);
                if (path.containsKey(name)) {
                    throw new GradleException(String.format(
                            "Processor '%s' in file '%s' gives duplicate path '%s' and '%s'",
                            name,
                            file,
                            value,
                            path.get(name)
                    ));
                }
                path.put(name, value);
                continue;
            }

            matcher = PROCESSOR_CONSUME_KEY.matcher(key);
            if (matcher.matches()) {
                final String name = matcher.group(1);
                final String value = props.getProperty(key);
                if (consumes.containsKey(name)) {
                    throw new GradleException(String.format(
                            "Processor '%s' in file '%s' gives duplicate consume '%s' and '%s'",
                            name,
                            file,
                            value,
                            consumes.get(name)
                    ));
                }
                consumes.put(name, value);
                continue;
            }

            matcher = PROCESSOR_PRODUCE_KEY.matcher(key);
            if (matcher.matches()) {
                final String name = matcher.group(1);
                final String value = props.getProperty(key);
                List<String> list = produces.get(name);
                if (list == null) {
                    list = new ArrayList<>();
                    produces.put(name, list);
                }
                for (final String item : value.split(",")) {
                    final String trimmed = item.trim();
                    if (list.contains(trimmed)) {
                        throw new GradleException(String.format(
                                "Processor '%s' in file '%s' gives two times the same produce '%s'",
                                name,
                                file,
                                value
                        ));
                    }
                }
                continue;
            }

            throw new GradleException(String.format("Property '%s' in file '%s' is not recognized", key, file));
        }

        // Make some sanity checks
        for (final String name : path.keySet()) {
            if (!consumes.containsKey(name)) {
                throw new GradleException(String.format(
                        "Processor '%s' in file '%s' has a path but no consumes",
                        name,
                        file
                ));
            }
        }
        for (final String name : consumes.keySet()) {
            if (!path.containsKey(name)) {
                throw new GradleException(String.format(
                        "Processor '%s' in file '%s' has a consumes but no path",
                        name,
                        file
                ));
            }
        }
        for (final String name : produces.keySet()) {
            if (!path.containsKey(name)) {
                throw new GradleException(String.format(
                        "Processor '%s' in file '%s' has a produces but no path",
                        name,
                        file
                ));
            }
        }

        // Populate mappings
        for (final Map.Entry<String, String> entry : path.entrySet()) {
            if (this.annotation.containsKey(entry.getValue())) {
                throw new GradleException(String.format(
                        "Processor '%s' in file '%s' has a path '%s' that's already been defined elsewhere",
                        entry.getKey(),
                        file,
                        entry.getValue()
                ));
            }
            final String annotation = consumes.get(entry.getKey());
            if (processor.containsKey(annotation)) {
                throw new GradleException(String.format(
                        "Processor '%s' in file '%s' has a consumes '%s' that's already been defined elsewhere",
                        entry.getKey(),
                        file,
                        annotation
                ));
            }
            this.processor.put(annotation, entry.getValue());
            this.annotation.put(entry.getValue(), annotation);
            this.produces.put(annotation, produces.get(entry.getKey()));
        }
    }

    private void populate(final LinkedList<String> processors, final Set<String> done, final String annotation) {
        if (!done.contains(annotation)) {
            final List<String> list = produces.get(annotation);
            if (list != null) {
                for (final String item : list) {
                    populate(processors, done, item);
                }
            }
            processors.addFirst(processor.get(annotation));
            done.add(annotation);
        }
    }

    public List<String> getProcessors() {
        final LinkedList<String> result = new LinkedList<>();
        final Set<String> done = new HashSet<>();
        for (final String annotation : processor.keySet()) {
            populate(result, done, annotation);
        }
        return result;
    }
}
