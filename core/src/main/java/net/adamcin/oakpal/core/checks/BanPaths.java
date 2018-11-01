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

package net.adamcin.oakpal.core.checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import net.adamcin.oakpal.core.PackageCheck;
import net.adamcin.oakpal.core.PackageCheckFactory;
import net.adamcin.oakpal.core.SimplePackageCheck;
import net.adamcin.oakpal.core.SimpleViolation;
import net.adamcin.oakpal.core.Violation;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.json.JSONObject;

/**
 * Ban paths by regular expression.
 */
public class BanPaths implements PackageCheckFactory {

    public class Check extends SimplePackageCheck {
        private final List<Pattern> banPatterns;

        public Check(final List<Pattern> banPatterns) {
            this.banPatterns = banPatterns;
        }

        @Override
        public String getCheckName() {
            return "BanPaths";
        }

        @Override
        public void importedPath(final PackageId packageId, final String path, final Node node)
                throws RepositoryException {
            banPatterns.forEach(pattern -> {
                if (pattern.matcher(path).find()) {
                    reportViolation(new SimpleViolation(Violation.Severity.MAJOR,
                            String.format("imported path %s matches banned pattern %s", path, pattern.pattern()), packageId));
                }
            });
        }

        @Override
        public void deletedPath(final PackageId packageId, final String path) {
            banPatterns.forEach(pattern -> {
                if (pattern.matcher(path).find()) {
                    reportViolation(new SimpleViolation(Violation.Severity.MAJOR,
                            String.format("deleted path %s matches banned pattern %s", path, pattern.pattern()), packageId));
                }
            });
        }
    }

    @Override
    public PackageCheck newInstance(final JSONObject config) throws Exception {
        List<String> patternStrings = new ArrayList<>();
        List<Pattern> patterns = new ArrayList<>();
        Optional.ofNullable(config.optJSONArray("patterns"))
                .map(array -> StreamSupport.stream(array.spliterator(), true)
                        .map(String::valueOf).collect(Collectors.toList())).ifPresent(patternStrings::addAll);
        for (String patternString : patternStrings) {
            patterns.add(Pattern.compile(patternString));
        }
        return new Check(patterns);
    }
}
