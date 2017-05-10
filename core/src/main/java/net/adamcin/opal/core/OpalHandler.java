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

package net.adamcin.opal.core;

import java.io.File;
import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.fs.config.MetaInf;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.PackageProperties;

/**
 * Primary point of customization for the Opal framework.
 */
public interface OpalHandler extends OpalViolationReporter {

    /**
     * Called after the package is uploaded to the package manager at the beginning of the scan. Track subsequent
     * events using the package ID provided to this method. This method will only be called once for each package
     * provided to {@link OpalContext#scanPackages(File...)}.
     *
     * @param packageId the package ID of the newly opened package
     * @param file      the package file that will be opened
     */
    void onBeginPackage(PackageId packageId, File file);

    /**
     * Called after each subpackage is opened. Track subsequent events using the package ID provided to this method.
     *
     * @param packageId the package ID of the newly opened subpackage
     * @param parentId the package ID of the parent package.
     */
    void onBeginSubpackage(PackageId packageId, PackageId parentId);

    /**
     * Called for each package before it is extracted.
     *
     * @param packageId         the package ID of the newly opened package
     * @param packageProperties the package properties
     * @param metaInf           the package meta information
     * @param subpackages       extracted subpackages
     */
    void onOpen(PackageId packageId, PackageProperties packageProperties,
                MetaInf metaInf, List<PackageId> subpackages);

    /**
     * Notified when package importer adds, modifies, or leaves a node untouched.
     *
     * @param packageId the current package
     * @param node the imported node
     * @throws RepositoryException for obvious reasons
     */
    void onImportPath(PackageId packageId, String path, Node node) throws RepositoryException;

    /**
     * Notified when package importer deletes an existing node.
     *
     * @param packageId the current package
     * @param path deleted path
     */
    void onDeletePath(PackageId packageId, String path);

    /**
     * Provides an opportunity to inspect repository state between packages.
     *
     * @param packageId      the current package
     * @param inspectSession session providing access to repository state
     * @throws RepositoryException for obvious reasons
     */
    void onClose(PackageId packageId, Session inspectSession) throws RepositoryException;
}
