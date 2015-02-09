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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by tharik on 1/29/15.
 */
public class DependencyManager {

    public static final String XPATH_ARTIFACT_SOURCE = "/project/artifactId";
    public static final String XPATH_GROUP_ID = "/project/parent/groupId";
    public static boolean IS_ALL_POMS = false;
    public static  String POM_FILE_NAME = "pom.xml";


    private static ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
    private static ArrayList<Dependency> uniqueDependencies = new ArrayList<Dependency>();

    public static void main(String [] args) throws Exception{
       processDependencies();
    }

    public static void processDependencies() throws Exception
    {
        String rootPath = "/Users/tharik/Desktop/git/rep";
        ArrayList<File> pomFiles = DependencyManager.loadPOMFiles(rootPath);
        String json;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        for (int i= 0 ; i< pomFiles.size(); i++){
            if (DependencyManager.IS_ALL_POMS ||
                pomFiles.get(i).getPath().split(File.separator)[rootPath.split(File.separator).length + 1]
                .equals("pom.xml")) {

               try {

                 Document doc = dBuilder.parse(pomFiles.get(i));
                 doc.getDocumentElement().normalize();

                 String artifactId = getXpathValue(doc, DependencyManager.XPATH_ARTIFACT_SOURCE);
                 String groupId = getXpathValue(doc, "/project/groupId");
                 String version = getXpathValue(doc, "/project/version");

                 if (groupId.equals("")){
                     groupId = getXpathValue(doc, "/project/parent/groupId");
                 }

                if (version.equals("")){
                    version = getXpathValue(doc, "/project/parent/version");
                }

                dependencies.addAll(GetDirectDependencies.loadDependenciesFromLocal(groupId, artifactId, version,
                            pomFiles.get(i).getPath().split(File.separator)[rootPath.split(File.separator).length]));

               }
               catch (Exception ex)
               {
                 System.out.println(ex.getMessage());
               }

            }
        }

        for (int i = 0; i < pomFiles.size(); i++) {
            DependencyManager.loadSourceRepositories(pomFiles.get(i), rootPath);
        }

        for (int i = 0; i < dependencies.size(); i++){

            if (dependencies.get(i).getRepositorySource() != null) {

                if (!isDependencyExists(uniqueDependencies, dependencies.get(i).getRepositoryDepends(),
                        dependencies.get(i).getRepositorySource() )) {
                    uniqueDependencies.add(dependencies.get(i));
                }
            }
        }

        json = "digraph {";

        for (int i = 0; i < uniqueDependencies.size(); i++) {
            json += '"' +uniqueDependencies.get(i).getRepositoryDepends() +'"' + "->" + '"'
                    + uniqueDependencies.get(i).getRepositorySource() + '"' + ";";
        }

        json += "}";
        System.out.println(json);
        System.out.println("Total Dependencies : " + dependencies.size());
        System.out.println("Total unique Dependencies : " + uniqueDependencies.size());
        System.out.println("Pom Files :" +  pomFiles.size());
    }

    public static ArrayList<File> loadPOMFiles(String rootPath) {
        File folder = new File(rootPath);
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> pomFiles = new ArrayList<File>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().equals(DependencyManager.POM_FILE_NAME)) {
                    pomFiles.add(listOfFiles[i]);
                }
            } else if (listOfFiles[i].isDirectory()) {
                pomFiles.addAll(DependencyManager.loadPOMFiles(rootPath + File.separator + listOfFiles[i].getName()));
            }
        }

        return  pomFiles;
    }

    public static String getXpathValue(Document doc, String expression)
            throws Exception
    {
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

    public static void loadSourceRepositories(File fXmlFile, String rootPath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            doc.getDocumentElement().normalize();



            String artifactId = getXpathValue(doc, DependencyManager.XPATH_ARTIFACT_SOURCE);
            String groupId = getXpathValue(doc, DependencyManager.XPATH_GROUP_ID);

            for (int i = 0; i < dependencies.size(); i++) {
                if ( dependencies.get(i).getArtifactId().equals(artifactId)
                        && dependencies.get(i).getGroupId().equals(groupId)) {
                    dependencies.get(i).setRepositorySource(fXmlFile.getPath().split(File.separator)
                                                                    [rootPath.split(File.separator).length]);
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
          if (unique.get(i).getRepositorySource().equals(repoSource)
                  && unique.get(i).getRepositoryDepends().equals(repoDepends)) {
              return  true;
          }
        }
        return  false;
    }
}
