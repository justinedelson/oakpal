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

import java.util.List;

import net.adamcin.oakpal.core.ProgressCheck;
import net.adamcin.oakpal.core.ProgressCheckFactory;
import net.adamcin.oakpal.core.SimpleProgressCheck;
import net.adamcin.oakpal.core.SimpleViolation;
import net.adamcin.oakpal.core.Violation;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.json.JSONObject;

/**
 * Check for subpackage inclusion.
 * <p>
 * Example config denying all subpackages:
 * <pre>
 *     "config": {
 *         "denyAll": true
 *     }
 * </pre>
 * <p>
 * Example config requiring an explicit group for all subpackages:
 * <pre>
 *     "config": {
 *         "rules": [{
 *             "type": "deny",
 *             "pattern": "my_packages/.*"
 *         }]
 *     }
 * </pre>
 * <p>
 * {@code config} options:
 * <dl>
 * <dt>{@code rules}</dt>
 * <dd>A list of {@link Rule} elements. Rules are evaluated top-to-bottom. The type of the last rule to match is
 * the effective action taken for the element. Any affected packageId matching a DENY rule will be reported as a
 * violation.</dd>
 * <dt>{@code denyAll}</dt>
 * <dd>Set to true to report a violation for any subpackage installation.</dd>
 * </dl>
 */
public class Subpackages implements ProgressCheckFactory {
    public static final String CONFIG_RULES = "rules";
    public static final String CONFIG_DENY_ALL = "denyAll";

    class Check extends SimpleProgressCheck {
        private final List<Rule> rules;
        private final boolean denyAll;

        public Check(final List<Rule> rules, final boolean denyAll) {
            this.rules = rules;
            this.denyAll = denyAll;
        }

        @Override
        public String getCheckName() {
            return Subpackages.this.getClass().getSimpleName();
        }

        @Override
        public void identifySubpackage(final PackageId packageId, final PackageId parentId) {
            if (denyAll) {
                reportViolation(new SimpleViolation(Violation.Severity.MAJOR,
                        String.format("subpackage %s included by %s. no subpackages are allowed.",
                                packageId, parentId), packageId));
            } else {
                Rule lastMatch = Rule.fuzzyDefaultAllow(rules);
                for (Rule rule : rules) {
                    System.out.printf("packageId: %s, pattern: %s, matches: %s\n", packageId.toString(),
                            rule.getPattern().pattern(), rule.matches(packageId.toString()));

                    if (rule.matches(packageId.toString())) {
                        lastMatch = rule;
                    }
                }

                if (lastMatch.isDeny()) {
                    reportViolation(new SimpleViolation(Violation.Severity.MAJOR,
                            String.format("subpackage %s included by %s matches deny pattern %s",
                                    packageId.toString(), parentId.toString(),
                                    lastMatch.getPattern().pattern()), packageId));
                }
            }
        }
    }

    @Override
    public ProgressCheck newInstance(final JSONObject config) throws Exception {
        List<Rule> rules = Rule.fromJSON(config.optJSONArray(CONFIG_RULES));

        final boolean denyAll = config.has(CONFIG_DENY_ALL) && config.optBoolean(CONFIG_DENY_ALL);

        return new Check(rules, denyAll);
    }
}
