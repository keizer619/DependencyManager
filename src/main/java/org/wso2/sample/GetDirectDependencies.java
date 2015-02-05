/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.wso2.sample;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.wso2.sample.util.Booter;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import java.io.PrintWriter;
import java.lang.Exception;import java.lang.String;import java.lang.System;import java.util.ArrayList;
import java.util.List;

/**
 * Determines the direct dependencies of an artifact as declared in its artifact descriptor (POM).
 */
public class GetDirectDependencies
{

    public static ArrayList<org.wso2.sample.library.Dependency> loadDependencies(String groupId, String artifactId, String version, RepositorySystem system,
                                        DefaultRepositorySystemSession session, List<RemoteRepository> repositories, String currentRepository)
            throws Exception {

        ArrayList<org.wso2.sample.library.Dependency> dependencies = new ArrayList<org.wso2.sample.library.Dependency>();


        PrintWriter out;

        Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        descriptorRequest.setRepositories(system.newResolutionRepositories(session, repositories));

        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);

        for (Dependency dependency : descriptorResult.getDependencies()) {
            String dep = dependency.toString();

            if (dep.toUpperCase().contains("SNAPSHOT")) {

                try {

                    org.wso2.sample.library.Dependency snapshot = new org.wso2.sample.library.Dependency();

                    snapshot.setArtifactId(dependency.getArtifact().getArtifactId().toString());
                    snapshot.setGroupId(dependency.getArtifact().getGroupId().toString());
                    snapshot.setVersion(dependency.getArtifact().getVersion().toString());
                    snapshot.setRepositoryDepends(currentRepository);


                    dependencies.add(snapshot);


                } catch (Exception e) {
                }


            }

        }
        return  dependencies;
    }

}
