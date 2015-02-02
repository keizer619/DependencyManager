package org.wso2.sample;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.sample.library.Dependency;
import org.wso2.sample.util.Booter;
import org.wso2.sample.util.FileSearch;

public class Main {

	public static void main(String args[]){

		FileSearch fileSearch = new FileSearch();
		String parentPath="/Users/tharik/Desktop/git/rep";
		String pathArray[]=fileSearch.getPath(parentPath);
        ArrayList<Dependency> dependencies = new ArrayList<Dependency>();

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

                    DefaultRepositorySystemSession session = Booter.newRepositorySystemSession(system);



					for (int s = 0; s < nodeLst.getLength(); s++) {
						Node fstNode = nodeLst.item(s);

						if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
							Element newElement = (Element) fstNode;
							NodeList groupIdElmntLst = newElement.getElementsByTagName("groupId");
							Element groupIdElmnt = (Element) groupIdElmntLst.item(0);
							NodeList groupIdNodeList = groupIdElmnt.getChildNodes();
							groupId = ((Node) groupIdNodeList.item(0)).getNodeValue();

							NodeList artifactIdElmntLst = newElement.getElementsByTagName("artifactId");
							Element artfIdElmnt = (Element) artifactIdElmntLst.item(0);
							NodeList artifIdNodeList = artfIdElmnt.getChildNodes();
							artifactId = ((Node) artifIdNodeList.item(0)).getNodeValue();
							
							if(artifactId.contains("parent")){
								int index = artifactId.indexOf("parent");
								artifactId = artifactId.substring(0,index-1);
							}

							NodeList versionElmntLst = newElement.getElementsByTagName("version");
							Element versionElmnt = (Element) versionElmntLst.item(0);
							NodeList versionNodeList = versionElmnt.getChildNodes();
							version = ((Node) versionNodeList.item(0)).getNodeValue();

                            dependencies.addAll(GetDirectDependencies.loadDependencies(groupId, artifactId, version, system, session, repositories, ""));

						}

					}

				} catch (Exception e) {
					System.out.println("Document parse error");
				}


			} catch (ParserConfigurationException e) {
				System.out.println("DocumentBuilder Error");
			}
		}

        System.out.println(dependencies.size());

	}

}
