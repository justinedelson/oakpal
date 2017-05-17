/*
 * Copyright 2017 Mark Adamcin
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base class implementing common or default methods of {@link ViolationReporter}.
 */
public abstract class AbstractViolationReporter implements ViolationReporter {
    private final List<Violation> violations = new ArrayList<>();

    protected final void reportViolation(Violation violation) {
        violations.add(violation);
    }

    @Override
    public URL getReporterUrl() {
        Class<?> clazz = getClass();
        return clazz.getResource(clazz.getSimpleName() + ".class");
    }

    @Override
    public final Collection<Violation> reportViolations() {
        List<Violation> toReturn = new ArrayList<>(this.violations);
        this.violations.clear();
        return Collections.unmodifiableList(toReturn);
    }
}
