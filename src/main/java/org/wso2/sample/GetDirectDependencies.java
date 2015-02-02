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
import java.lang.Exception;import java.lang.String;import java.lang.System;import java.util.ArrayList;
import java.util.List;

/**
 * Determines the direct dependencies of an artifact as declared in its artifact descriptor (POM).
 */
public class GetDirectDependencies
{

    public static void main( String[] args )
        throws Exception {
        processAether();
    }

    public static void processAether()
        throws Exception {

        RepositorySystem system = Booter.newRepositorySystem();
        List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();

        repositories.add((new RemoteRepository.Builder("wso2.snapshots", "default",
                "http://maven.wso2.org/nexus/content/repositories/snapshots/")).build());
        repositories.add((new RemoteRepository.Builder("wso2.releases", "default",
                "http://maven.wso2.org/nexus/content/repositories/releases/")).build());

        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession(system);

       loadDependencies("org.wso2.andes", "andes", "3.0.0-SNAPSHOT", system, session, repositories);

        //loadDependencies("org.wso2.balana", "balana", "1.0.0.wso2v8-SNAPSHOT", system, session, repositories);

     //  loadDependencies("org.wso2.governance", "governance", "5.0.0", system, session, repositories);
        //loadDependencies("org.wso2.carbon", "carbon-mediation", "4.3.0-SNAPSHOT");
    }

    public static void loadDependencies(String groupId, String artifactId, String version, RepositorySystem system,
                                        DefaultRepositorySystemSession session, List<RemoteRepository> repositories)
            throws Exception {

        int counter = 0;
        Artifact artifact = new DefaultArtifact( groupId + ":" + artifactId + ":" + version  );
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( artifact );
        descriptorRequest.setRepositories( system.newResolutionRepositories(session, repositories ) );

        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor( session, descriptorRequest );

        for ( Dependency dependency : descriptorResult.getDependencies() )
        {
            System.out.println( counter++ + " - " + dependency );

        }

    }

}
