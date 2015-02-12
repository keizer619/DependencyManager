/**
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.wso2.carbon.build.tools;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.wso2.carbon.build.data.PatternMatch;
import org.wso2.carbon.build.data.UrlValidate;

import java.io.*;
import java.lang.Exception;
import java.lang.String;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AetherManager
{
    private static final Log logger = LogFactory.getLog(AetherManager.class);

    /**
     * Provide dependencies on given artifact by checking against given remote repositories
     * @param groupId
     * @param artifactId
     * @param version
     * @param system
     * @param session
     * @param repositories
     * @param currentRepository
     * @return
     * @throws Exception
     */
    public static ArrayList<org.wso2.carbon.build.tools.dto.Dependency> loadDependenciesFromRemote(String groupId,
                                        String artifactId, String version, RepositorySystem system,
                                        DefaultRepositorySystemSession session, List<RemoteRepository> repositories,
                                        String currentRepository) throws Exception {

        ArrayList<org.wso2.carbon.build.tools.dto.Dependency> dependencies =
                new ArrayList<org.wso2.carbon.build.tools.dto.Dependency>();
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

    /**
     * Provide dependencies on given artifact by checking against local repository
     * @param groupId
     * @param artifactId
     * @param version
     * @param currentRepository
     * @return
     * @throws Exception
     */
    public static ArrayList<org.wso2.carbon.build.tools.dto.Dependency> loadDependenciesFromLocal(String groupId,
                                     String artifactId, String version , String currentRepository) throws Exception {

        ArrayList<org.wso2.carbon.build.tools.dto.Dependency> dependencies =
                new ArrayList<org.wso2.carbon.build.tools.dto.Dependency>();

        RepositorySystem system = AetherManager.newRepositorySystem();
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository(Constants.M2_PATH);
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        Artifact artifact = new DefaultArtifact(groupId + Constants.DEPENDENCY_SEPERATOR  + artifactId
                                                + Constants.DEPENDENCY_SEPERATOR  + version);
        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( artifact );
        descriptorRequest.setRepositories(new ArrayList<RemoteRepository>( Arrays.asList(
                new RemoteRepository.Builder( "central", "default", "http://central.maven.org/maven2/" ).build())));
        ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor( session, descriptorRequest );

        for (Dependency dependency : descriptorResult.getDependencies()) {
            dependencies.add(loadDependency(dependency, currentRepository));
        }

        for (Dependency dependency : descriptorResult.getManagedDependencies()) {
            dependencies.add(loadDependency(dependency, currentRepository));
        }

        return  dependencies;
    }

    private static org.wso2.carbon.build.tools.dto.Dependency loadDependency(Dependency dependency,
                                                                            String currentRepository) {
        org.wso2.carbon.build.tools.dto.Dependency dep = new org.wso2.carbon.build.tools.dto.Dependency();

        dep.setArtifactId(dependency.getArtifact().getArtifactId().toString());
        dep.setGroupId(dependency.getArtifact().getGroupId().toString());
        dep.setVersion(dependency.getArtifact().getVersion().toString());
        dep.setRepositoryDepends(currentRepository);
        dep.setRepositorySource(Constants.DEFAULT_SOURCE_NAME);
        dep.setLatestVersion(AetherManager.getLatestVersion(dep));

        return dep;
    }

    public static RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        locator.setErrorHandler( new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception ) {
                exception.printStackTrace();
            }
        } );

        return locator.getService(RepositorySystem.class);
    }

    public static String getLatestVersion(org.wso2.carbon.build.tools.dto.Dependency dependency) {
        String latestVersion = "";
        UrlValidate urlValidate = new UrlValidate();
        String url = urlValidate.checkUrl(dependency.getArtifactId(),
                dependency.getGroupId(), dependency.getVersion());
        String repository;
        URL newUrl;
        InputStream inStream = null;
        DataInputStream dataInStream;
        String line;
        BufferedWriter bufferedWriter;

        if (url != null) {

            try {

                if (url.contains("wso2")) {
                    repository = "wso2";
                } else {
                    repository = "maven";
                }
                newUrl = new URL(url);
                try {
                    inStream = newUrl.openStream();
                } catch (Exception ex) {
                    logger.error("Exception occurred : " + ex.getMessage());
                }
                dataInStream = new DataInputStream(new BufferedInputStream(inStream));
                bufferedWriter = new BufferedWriter(new FileWriter("out.txt"));

                while ((line = dataInStream.readLine()) != null) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }

                //Read out.txt file
                PatternMatch patternMatch = new PatternMatch();
                latestVersion = patternMatch.getMAtchDetails(dependency.getGroupId(),
                        dependency.getVersion(), url, repository);
            }
            catch (Exception ex)
            {

            }
        }

        return latestVersion;
    }
}
