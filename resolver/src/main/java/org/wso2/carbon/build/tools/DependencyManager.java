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

import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.wso2.carbon.build.tools.dto.Dependency;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.wso2.carbon.build.tools.data.VersionManager;

public class DependencyManager {

    private static final Log logger = LogFactory.getLog(DependencyManager.class);

    public static void main(String [] args) throws Exception {
        ArrayList<Dependency> dependencies =  getAllDependencies();
        ArrayList<Dependency> uniqueDependencies = new ArrayList<Dependency>();
        VersionManager versionManager = new VersionManager();

        //Eliminates duplicated repository relationships
        for (int i = 0; i < dependencies.size(); i++){

            if (dependencies.get(i).getRepositorySource() != null) {

                if (!isRepositoryDependencyExists(uniqueDependencies, dependencies.get(i).getRepositoryDepends(),
                        dependencies.get(i).getRepositorySource())) {
                    uniqueDependencies.add(dependencies.get(i));
                }
            }
        }

        System.out.println(DependencyManager.generateJsonGraph(uniqueDependencies));
        System.out.println("Total unique Repository Dependencies : " + uniqueDependencies.size());

        //Eliminates duplicated artifact repositories
        uniqueDependencies.clear();
        for (int i = 0; i < dependencies.size(); i++){

            if (dependencies.get(i).getRepositorySource() != null) {

                if (!isDependencyExists(uniqueDependencies, dependencies.get(i).getGroupId(),
                        dependencies.get(i).getArtifactId(), dependencies.get(i).getVersion(),
                        dependencies.get(i).getRepositoryDepends() )) {
                    uniqueDependencies.add(dependencies.get(i));
                }
            }
        }

        System.out.println("Total Dependencies : " + dependencies.size());
        System.out.println("Total unique Dependencies : " + uniqueDependencies.size());
        
        versionManager.VersionManage(uniqueDependencies);
    }

    public static ArrayList<Dependency> getDependencies(String groupId, String artifactId, String version
                                                         , String currentRepository) throws  Exception{
        return AetherManager.loadDependenciesFromLocal(groupId, artifactId, version, currentRepository);
    }

    public static ArrayList<Dependency> getAllDependencies() throws Exception {

        ArrayList<Dependency> dependencies = new ArrayList<Dependency>();


        ArrayList<File> pomFiles = DependencyManager.loadPOMFiles(Constants.ROOT_PATH);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        for (int i= 0 ; i < pomFiles.size(); i++) {
            if (Constants.IS_ALL_POMS ||
                pomFiles.get(i).getPath().split(File.separator)[Constants.ROOT_PATH.split(File.separator).length + 1]
                .equals(Constants.POM_FILE_NAME)) {

               try {

                 Document doc = dBuilder.parse(pomFiles.get(i));
                 doc.getDocumentElement().normalize();

                 String artifactId = getXpathValue(doc, Constants.XPATH_ARTIFACT_SOURCE);
                 String groupId = getXpathValue(doc, Constants.XPATH_GROUP_ID);
                 String version = getXpathValue(doc, Constants.XPATH_VERSION);

                 if (groupId.equals("")){
                     groupId = getXpathValue(doc, Constants.XPATH_PARENT_GROUP_ID);
                 }

                if (version.equals("")){
                    version = getXpathValue(doc, Constants.XPATH_PARENT_VERSION);
                }

                dependencies.addAll(AetherManager.loadDependenciesFromLocal(groupId, artifactId, version,
                        pomFiles.get(i).getPath().split(File.separator)
                                [Constants.ROOT_PATH.split(File.separator).length]));

               }
               catch (Exception ex) {
                   logger.error("Exception occurred when loading pom dependency " + ex.getMessage());
               }

            }
        }
        //Load sources by checking all loaded pom.xml files
        for (int i = 0; i < pomFiles.size(); i++) {
           dependencies = DependencyManager.loadSourceRepositories(pomFiles.get(i), Constants.ROOT_PATH, dependencies);
        }


        return dependencies;
    }

    /**
     * Generate Json string for dependency graph
     * @param dependencies
     * @return
     */
    public static String generateJsonGraph(ArrayList<Dependency> dependencies) {
        String json = "digraph {";

        for (int i = 0; i < dependencies.size(); i++) {
            json += '"' +dependencies.get(i).getRepositoryDepends() +'"' + "->" + '"'
                    + dependencies.get(i).getRepositorySource() + '"' + ";";
        }
        json += "}";
        return json;
    }

    /**
     * Load all pom.xml files recursively in given path
     * @param rootPath
     * @return
     */
    public static ArrayList<File> loadPOMFiles(String rootPath) {
        File folder = new File(rootPath);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> pomFiles = new ArrayList<File>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(Constants.POM_FILE_NAME)) {
                    pomFiles.add(listOfFiles[i]);
                }
            } else if (listOfFiles[i].isDirectory()) {
                pomFiles.addAll(DependencyManager.loadPOMFiles(rootPath + File.separator + listOfFiles[i].getName()));
            }
        }

        return  pomFiles;
    }

    /**
     * Provide value of given xpath expression and xml document
     * @param doc
     * @param expression
     * @return
     * @throws Exception
     */
    public static String getXpathValue(Document doc, String expression)
            throws Exception {
        XPath xPath =  XPathFactory.newInstance().newXPath();

        NodeList nList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

        for (int temp = 0; temp < nList.getLength(); temp++) {

            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                return nNode.getTextContent();
            }
        }

        return "";
    }

    /**
     * Search and load source repository of given dependencies
     * @param fXmlFile
     * @param rootPath
     * @param dependencies
     * @return
     */
    public static ArrayList<Dependency>  loadSourceRepositories(File fXmlFile, String rootPath,
                                                                ArrayList<Dependency> dependencies) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();



            String artifactId = getXpathValue(doc, Constants.XPATH_ARTIFACT_SOURCE);
            String groupId = getXpathValue(doc, Constants.XPATH_PARENT_GROUP_ID);

            for (int i = 0; i < dependencies.size(); i++) {
                if ( dependencies.get(i).getArtifactId().equals(artifactId)
                        && dependencies.get(i).getGroupId().equals(groupId)) {
                    dependencies.get(i).setRepositorySource(fXmlFile.getPath().split(File.separator)
                                                                    [rootPath.split(File.separator).length]);
                }
            }
        } catch (Exception ex) {
            logger.error("Exception occurred when loading repository " + ex.getMessage());
        }
        return dependencies;
    }

    /**
     * Check weather dependency relationship between repositories exists
     * @param dependencies
     * @param repoDepends
     * @param repoSource
     * @return
     */
    public static boolean isRepositoryDependencyExists(ArrayList<Dependency> dependencies, String repoDepends,
                                                       String repoSource) {
        //Eliminates recursive repository dependencies
        if (repoDepends.trim().equals(repoSource.trim())) {
            return true;
        }

        for (int i = 0; i < dependencies.size(); i++) {
          if (dependencies.get(i).getRepositorySource().equals(repoSource)
                  && dependencies.get(i).getRepositoryDepends().equals(repoDepends)) {
              return  true;
          }
        }
        return  false;
    }

    /**
     * Check weather dependency artifact exists
     * @param dependencies
     * @param groupId
     * @param artifactId
     * @param version
     * @param productDepends
     * @return
     */
    public static boolean isDependencyExists(ArrayList<Dependency> dependencies, String groupId, String artifactId,
                                             String version, String productDepends) {
        for (int i = 0; i < dependencies.size(); i++) {
            if (dependencies.get(i).getGroupId().equals(groupId)
                    && dependencies.get(i).getArtifactId().equals(artifactId)
                    && dependencies.get(i).getVersion().equals(version)
                    && dependencies.get(i).getRepositoryDepends().equals(productDepends)) {
                return  true;
            }
        }
        return  false;
    }


}
