/*
 * Copyright 2020 Mark Adamcin
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

package net.adamcin.oakpal.core.installable;

import org.apache.jackrabbit.vault.packaging.PackageId;
import org.jetbrains.annotations.NotNull;

public final class SubpackageInstallable implements PathInstallable {
    private final PackageId parentId;
    private final String jcrPath;
    private final PackageId subpackageId;

    public SubpackageInstallable(final PackageId parentId, final String jcrPath, final PackageId subpackageId) {
        this.parentId = parentId;
        this.jcrPath = jcrPath;
        this.subpackageId = subpackageId;
    }

    @NotNull
    @Override
    public PackageId getParentId() {
        return parentId;
    }

    @NotNull
    @Override
    public String getJcrPath() {
        return jcrPath;
    }

    public PackageId getSubpackageId() {
        return subpackageId;
    }
}