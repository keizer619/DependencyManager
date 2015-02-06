package org.wso2.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.wso2.sample.library.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.sample.util.Booter;
import org.wso2.sample.util.FileSearch;

/**
 * Created by tharik on 1/29/15.
 */
public class DependencyManager {

    public static final String XPATH_ARTIFACT_SOURCE = "/project/artifactId";
    public static final String POM_FILE_NAME = "pom.xml";
    private static ArrayList<File> pomFiles = new ArrayList<File>();

    private static ArrayList<Dependency> dependencies = new ArrayList<Dependency>();
    private static ArrayList<Dependency> uniqueDependencies = new ArrayList<Dependency>();

    public static void main(String [] args) throws Exception{
       processDependencies();
    }

    public static void processDependencies() throws Exception
    {
        String rootPath = "/Users/tharik/Desktop/git/rep";
        DependencyManager.loadPOMFiles(rootPath);
        String json;

        dependencies.addAll(GetDirectDependencies.loadDependenciesFromLocal("org.wso2.cep", "wso2cep", "4.0.0-SNAPSHOT", "product-cep"));
        dependencies.addAll(GetDirectDependencies.loadDependenciesFromLocal("org.wso2.bam", "wso2bam-parent", "3.0.0-SNAPSHOT", "product-bam"));
        dependencies.addAll(GetDirectDependencies.loadDependenciesFromLocal("org.wso2.mb", "mb-parent", "3.0.0-SNAPSHOT", "product-mb"));



        for (int i = 0; i < pomFiles.size(); i++) {
            DependencyManager.loadSourceRepositories(pomFiles.get(i), rootPath);
        }

        for (int i = 0; i < dependencies.size(); i++){
            System.out.println(i +" - " +dependencies.get(i).getGroupId() +" - " + dependencies.get(i).getArtifactId() + " - " + dependencies.get(i).getVersion() + " - "  + dependencies.get(i).getRepositorySource());
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

            // json += '"' +uniqueDependencies.get(i).getRepositoryDepends() +'"' + "->"
            // + '"' +uniqueDependencies.get(i).getRepositorySource()
            // + "(" +uniqueDependencies.get(i).getArtifactId()  + ")" + '"' + ";";
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

    public static ArrayList<Dependency> loadPOM(String rootPath) {
        FileSearch fileSearch = new FileSearch();
        String pathArray[]=fileSearch.getPath(rootPath);
        ArrayList<Dependency> snapshots = new ArrayList<Dependency>();

        for(int i =0;i<pathArray.length;i++){
            try {
                File file = new File(pathArray[i]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                System.out.println(pathArray[i]);

                String groupId=null;
                String artifactId=null;
                String version=null;

                Document doc;
                try {
                    doc = db.parse(file);
                    doc.getDocumentElement().normalize();
                    NodeList nodeLst = doc.getElementsByTagName("project");


                    RepositorySystem system = Booter.newRepositorySystem();
                    List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();

                    repositories.add((new RemoteRepository.Builder("wso2.snapshots", "default",
                            "http://maven.wso2.org/nexus/content/repositories/snapshots/")).build());
                    repositories.add((new RemoteRepository.Builder("wso2.releases", "default",
                            "http://maven.wso2.org/nexus/content/repositories/releases/")).build());
                    repositories.add((new RemoteRepository.Builder("wso2.wso2maven2", "default",
                            "http://maven.wso2.org/nexus/content/repositories/wso2maven2/")).build());

                    DefaultRepositorySystemSession session = Booter.newRepositorySystemSession(system);



                    for (int s = 0; s < nodeLst.getLength(); s++) {
                        Node fstNode = nodeLst.item(s);

                        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element newElement = (Element) fstNode;
                            NodeList groupIdElmntLst = newElement.getElementsByTagName("groupId");
                            Element groupIdElmnt = (Element) groupIdElmntLst.item(0);
                            NodeList groupIdNodeList = groupIdElmnt.getChildNodes();
                            groupId = ((Node) groupIdNodeList.item(0)).getNodeValue();

                            XPath xPath =  XPathFactory.newInstance().newXPath();
                            String expression = "/project/artifactId";
                            artifactId = xPath.compile(expression).evaluate(doc);

                            NodeList versionElmntLst = newElement.getElementsByTagName("version");
                            Element versionElmnt = (Element) versionElmntLst.item(0);
                            NodeList versionNodeList = versionElmnt.getChildNodes();
                            version = ((Node) versionNodeList.item(0)).getNodeValue();

                             if(artifactId.contains("parent")){
                                int index = artifactId.indexOf("parent");
                                artifactId = artifactId.substring(0,index-1);
                               }

                            snapshots.addAll(GetDirectDependencies.loadDependencies(groupId, artifactId, version,
                                    system, session, repositories,
                                    pathArray[i].split(File.separator)[rootPath.split(File.separator).length]));

                        }

                    }

                } catch (Exception e) {
                    System.out.println("Document parse error");
                }


            } catch (ParserConfigurationException e) {
                System.out.println("DocumentBuilder Error");
            }
        }

        return snapshots;
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
            String groupId = getXpathValue(doc, "/project/parent/groupId");

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
