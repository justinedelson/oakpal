/*
 * Copyright 2018 Mark Adamcin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.adamcin.oakpal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import aQute.bnd.header.Parameters;
import aQute.bnd.osgi.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    private Util() {
        // do nothing
    }

    public static List<String> getManifestHeaderValues(final Manifest manifest, final String headerName) {
        Domain domain = Domain.domain(manifest);
        Parameters params = domain.getParameters(headerName);
        return new ArrayList<>(params.keySet());
    }

    public static List<URL> resolveManifestResources(final URL manifestUrl, final List<String> resources) {
        return resources.stream()
                .map(name -> {
                    final String relPath = "../" + name;
                    try {
                        return Optional.of(new URL(manifestUrl, relPath));
                    } catch (final MalformedURLException e) {
                        LOGGER.debug("[resolveManifestResources] malformed url: manifestUrl={}, relPath={}, error={}",
                                manifestUrl, relPath, e.getMessage());
                        return Optional.<URL>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }

    public static Map<URL, List<URL>> mapManifestHeaderResources(final String headerName, final ClassLoader classLoader) throws IOException {
        Map<URL, List<URL>> map = new LinkedHashMap<>();
        Enumeration<URL> resEnum = classLoader.getResources(JarFile.MANIFEST_NAME);
        while (resEnum.hasMoreElements()) {
            URL url = resEnum.nextElement();
            try (InputStream is = url.openStream()) {
                Manifest manifest = new Manifest(is);
                List<URL> headerResources = resolveManifestResources(url, Util.getManifestHeaderValues(manifest, headerName));
                map.put(url, headerResources);
            }
        }

        return map;
    }

    public static Map<URL, List<URL>> mapManifestHeaderResources(final String headerName, final List<File> files) throws IOException {
        Map<URL, List<URL>> map = new LinkedHashMap<>();
        for (File zipFile : files) {
            if (!zipFile.exists() || zipFile.isDirectory()) {
                File manifestFile = new File(zipFile, JarFile.MANIFEST_NAME);
                if (manifestFile.exists()) {
                    try (InputStream fis = new FileInputStream(manifestFile)) {
                        Manifest manifest = new Manifest(fis);
                        final URL manifestUrl = manifestFile.toURI().toURL();
                        map.put(manifestUrl, resolveManifestResources(manifestUrl,
                                Util.getManifestHeaderValues(manifest, headerName)));
                    }
                }
            } else {
                try (JarFile jar = new JarFile(zipFile)) {
                    Manifest manifest = jar.getManifest();
                    final URL manifestUrl = new URL(String.format("jar:%s!/%s",
                            zipFile.toURI().toURL().toExternalForm(), JarFile.MANIFEST_NAME));
                    map.put(manifestUrl, resolveManifestResources(manifestUrl,
                            Util.getManifestHeaderValues(manifest, headerName)));
                }
            }
        }
        return map;
    }

    static ClassLoader getDefaultClassLoader() {
        return Thread.currentThread().getContextClassLoader() != null
                ? Thread.currentThread().getContextClassLoader()
                : Util.class.getClassLoader();
    }

    public static <T> Predicate<T> debugFilter(final Logger logger, final String format) {
        if (logger.isDebugEnabled()) {
            return item -> {
                logger.debug(format, item);
                return true;
            };
        } else {
            return item -> true;
        }
    }

    public static <T> Predicate<T> traceFilter(final Logger logger, final String format) {
        if (logger.isTraceEnabled()) {
            return item -> {
                logger.trace(format, item);
                return true;
            };
        } else {
            return item -> true;
        }
    }

}
