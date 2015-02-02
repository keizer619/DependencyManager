package org.wso2.sample;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.wso2.sample.library.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Created by tharik on 1/29/15.
 */
public class DependencyManager {

    public static final String XPATH_ARTIFACT_SOURCE = "/project/artifactId";
    public static final String POM_FILE_NAME = "pom.xml";
    private static ArrayList<File> pomFiles = new ArrayList<File>();

    private static ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
    private static ArrayList<Dependency> uniqueDependencies = new ArrayList<Dependency>();

    public static void main(String [] args) {
        String rootPath = "/Users/tharik/Desktop/git/rep";
        DependencyManager.loadPOMFiles(rootPath);
        String json;

        for (int i = 0; i < pomFiles.size(); i++) {
            DependencyManager.loadPOM(pomFiles.get(i), rootPath);
        }

        for (int i = 0; i < pomFiles.size(); i++) {
            DependencyManager.loadSourceRepositories(pomFiles.get(i), rootPath);
        }

        for (int i = 0; i < dependencies.size(); i++){

            if (dependencies.get(i).getRepositorySource() != null) {

                if (!isDependencyExists(uniqueDependencies, dependencies.get(i).getRepositoryDepends(), dependencies.get(i).getRepositorySource() )) {
                  uniqueDependencies.add(dependencies.get(i));
                }
            }
        }

        json = "digraph {";

        for (int i = 0; i < uniqueDependencies.size(); i++) {
            //json += '"' +uniqueDependencies.get(i).getRepositoryDepends() +'"' + "->" + '"' +uniqueDependencies.get(i).getRepositorySource() + '"' + ";";
            json += '"' +uniqueDependencies.get(i).getRepositoryDepends() +'"' + "->" + '"' +uniqueDependencies.get(i).getRepositorySource() + "(" +uniqueDependencies.get(i).getArtifactId()  + ")" + '"' + ";";
        }

        json += "}";

        System.out.println(json);

        System.out.println("Total Dependencies : " + dependencies.size());
        System.out.println("Total unique Dependencies : " + uniqueDependencies.size());
        System.out.println("Pom Files :" +  pomFiles.size());
    }

    public static void loadPOMFiles(String rootPath) {
        File folder = new File(rootPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(DependencyManager.POM_FILE_NAME)) {
                    pomFiles.add(listOfFiles[i]);
                }
            } else if (listOfFiles[i].isDirectory()) {
                DependencyManager.loadPOMFiles(rootPath + File.separator + listOfFiles[i].getName());
            }
        }

    }

    public static void loadPOM(File fXmlFile, String rootPath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("dependency");

                for (int temp = 0; temp < nList.getLength(); temp++) {

                    Node nNode = nList.item(temp);


                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        if (eElement.getElementsByTagName("version").item(0) != null &&
                                eElement.getElementsByTagName("version").item(0).getTextContent().toUpperCase().contains("SNAPSHOT")) {

                            Dependency snapshot = new Dependency();
                            snapshot.setGroupId(eElement.getElementsByTagName("groupId").item(0).getTextContent());
                            snapshot.setArtifactId(eElement.getElementsByTagName("artifactId").item(0).getTextContent());
                            snapshot.setVersion(eElement.getElementsByTagName("version").item(0).getTextContent());
                            snapshot.setRepositoryDepends(fXmlFile.getPath().split(File.separator)[rootPath.split(File.separator).length]);
                            dependencies.add(snapshot);
                        }
                    }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + fXmlFile.getPath());
        }
    }

    public static void loadSourceRepositories(File fXmlFile, String rootPath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();

            XPath xPath =  XPathFactory.newInstance().newXPath();
            String expression = DependencyManager.XPATH_ARTIFACT_SOURCE;
            NodeList nList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    for (int i = 0; i < dependencies.size(); i++) {
                        if ( dependencies.get(i).getArtifactId().equals(nNode.getTextContent())) {
                            dependencies.get(i).setRepositorySource(fXmlFile.getPath().split(File.separator)[rootPath.split(File.separator).length]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + fXmlFile.getPath());
        }
    }

    public static boolean isDependencyExists(ArrayList<Dependency> unique, String repoDepends, String repoSource) {

        if (repoDepends.trim().equals(repoSource.trim())) {
            return true;
        }

        for (int i = 0; i < unique.size(); i++) {
          if (unique.get(i).getRepositorySource().equals(repoSource) && unique.get(i).getRepositoryDepends().equals(repoDepends)) {
              return  true;
          }
        }
        return  false;
    }

}
