package org.wso2.sample;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepository;
import org.wso2.sample.util.Booter;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;

import java.io.FileWriter;
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

        Artifact artifact = new DefaultArtifact(groupId + Constants.DEPENDENCY_SEPERATOR + artifactId
                                                + Constants.DEPENDENCY_SEPERATOR  + version);
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact(artifact);
        descriptorRequest.setRepositories(system.newResolutionRepositories(session, repositories));

        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);

        for (Dependency dependency :  descriptorResult.getDependencies()) {
            dependencies.add(loadDependency(dependency, currentRepository));
        }

        for (Dependency dependency : descriptorResult.getManagedDependencies()) {
            dependencies.add(loadDependency(dependency, currentRepository));
        }
        return  dependencies;
    }

    public static ArrayList<org.wso2.sample.library.Dependency> loadDependenciesFromLocal(String groupId,
                                     String artifactId, String version , String currentRepository) throws Exception {

        ArrayList<org.wso2.sample.library.Dependency> dependencies = new ArrayList<org.wso2.sample.library.Dependency>();

        RepositorySystem system = Booter.newRepositorySystem();
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(Constants.M2_PATH);
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        Artifact artifact = new DefaultArtifact(groupId + Constants.DEPENDENCY_SEPERATOR  + artifactId
                                                + Constants.DEPENDENCY_SEPERATOR  + version);
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( artifact );
        descriptorRequest.setRepositories( Booter.newRepositories( system, session ) );
        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor( session, descriptorRequest );

        for (Dependency dependency : descriptorResult.getDependencies()) {
            dependencies.add(loadDependency(dependency, currentRepository));
        }

        for (Dependency dependency : descriptorResult.getManagedDependencies()) {
            dependencies.add(loadDependency(dependency, currentRepository));
        }

        return  dependencies;
    }

    private static org.wso2.sample.library.Dependency loadDependency(Dependency dependency, String currentRepository)
    {
        org.wso2.sample.library.Dependency dep = new org.wso2.sample.library.Dependency();

        dep.setArtifactId(dependency.getArtifact().getArtifactId().toString());
        dep.setGroupId(dependency.getArtifact().getGroupId().toString());
        dep.setVersion(dependency.getArtifact().getVersion().toString());
        dep.setRepositoryDepends(currentRepository);
        dep.setRepositorySource(Constants.DEFAULT_SOURCE_NAME);

        return dep;
    }

}
