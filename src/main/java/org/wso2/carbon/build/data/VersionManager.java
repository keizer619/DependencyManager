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


package org.wso2.carbon.build.data;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.wso2.carbon.build.tools.dto.Dependency;
import org.wso2.carbon.build.data.UrlValidate;
import org.wso2.carbon.build.tools.Constants;


public class VersionManager {

	private static Connection connect = null;

	static URL newUrl=null;
	static InputStream inStream = null;
	static DataInputStream dataInStream = null;
	static String line=null;
	static BufferedWriter bufferedWriter = null;
	static boolean valid = false;

	static int maxId=0;

	public void VersionManage(ArrayList<Dependency> uniqueDependencies){
		UrlValidate urlValidate = new UrlValidate();

		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection(Constants.DATABASE_CONNECTION, "nishali", "thilanka");
			Statement stmt = connect.createStatement();

			for (int i = 0; i < uniqueDependencies.size(); i++){
					
					//insert data into RepositoryTable
					ResultSet compareRepository = stmt.executeQuery("SELECT RepoId FROM RepositoryTable WHERE RepoName='"+uniqueDependencies.get(i).getRepositoryDepends()+"'");												
					if(!compareRepository.next()){
						String insertRepositoryQuery="INSERT INTO RepositoryTable VALUES(?,?,?)";
						PreparedStatement insertRepositorySt = connect.prepareStatement(insertRepositoryQuery);
						insertRepositorySt.setString(1,null);
						insertRepositorySt.setString(2,  uniqueDependencies.get(i).getRepositoryDepends());
						insertRepositorySt.setString(3,null);
						insertRepositorySt.execute();
					}
					
					//insert into DependencyTable
					ResultSet compareDependency = stmt.executeQuery("SELECT GroupId FROM DependencyTable WHERE GroupId='"+uniqueDependencies.get(i).getGroupId()+"' AND ArtifactId='"+uniqueDependencies.get(i).getArtifactId()+"' AND Version='"+uniqueDependencies.get(i).getVersion()+"'");
					if(!compareDependency.next()){
						int sourceRepoId=0;
						ResultSet repoId=stmt.executeQuery("SELECT RepoId FROM RepositoryTable WHERE RepoName='"+uniqueDependencies.get(i).getRepositoryDepends()+"'");
						while (repoId.next()) {
							sourceRepoId=repoId.getInt(1);
						}
						
						String insertDepndencyQuery="INSERT INTO DependencyTable values(?,?,?,?,?)";
						PreparedStatement insertDependencySt = connect.prepareStatement(insertDepndencyQuery);
						insertDependencySt.setString(1, uniqueDependencies.get(i).getGroupId());
						insertDependencySt.setString(2, uniqueDependencies.get(i).getArtifactId());
						insertDependencySt.setString(3, uniqueDependencies.get(i).getVersion());
						insertDependencySt.setString(4, null);
						insertDependencySt.setInt(5, sourceRepoId);
						insertDependencySt.execute();
					}
					

					String url = urlValidate.checkUrl(uniqueDependencies.get(i).getArtifactId(), uniqueDependencies.get(i).getGroupId(), uniqueDependencies.get(i).getVersion());
					String repository=null;
					if(url!=null){
						if(url.contains("wso2")){
							repository="wso2";
						}else{
							repository="maven";
						}
						newUrl = new URL(url);
						try{
							inStream = newUrl.openStream();
						}catch (Exception e) {
							System.out.println("Error-open stream");
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
						String latestVersion=patternMatch.getMAtchDetails(uniqueDependencies.get(i).getGroupId(), uniqueDependencies.get(i).getVersion(), url,repository);
					
						//Update the latestVersion in DependencyTable
						String insertVersionQuery = "update DependencyTable set latestVersion = ? where GroupId='"+uniqueDependencies.get(i).getGroupId()+"' AND ArtifactId='"+uniqueDependencies.get(i).getArtifactId()+"' AND Version='"+uniqueDependencies.get(i).getVersion()+"'";
						PreparedStatement update = connect.prepareStatement(insertVersionQuery);
						update.setString(1, latestVersion);
						update.execute();
						 
						//insert into RepositoryDependencyTable
						int sourceRepoId2=0;
						ResultSet repoId=stmt.executeQuery("SELECT RepoId FROM RepositoryTable WHERE RepoName='"+uniqueDependencies.get(i).getRepositoryDepends()+"'");
						while (repoId.next()) {
							sourceRepoId2=repoId.getInt(1);
						}
						ResultSet compareRepoDependency = stmt.executeQuery ("SELECT * FROM RepositoryDependencyTable WHERE GroupId='"+uniqueDependencies.get(i).getGroupId()+"' AND ArtifactId='"+uniqueDependencies.get(i).getArtifactId()+"' AND Version='"+uniqueDependencies.get(i).getVersion()+"' AND SourceRepoId='"+sourceRepoId2+"'");
						if(!compareRepoDependency.next()){
							String insertRepoDepndencyQuery="INSERT INTO RepositoryDependencyTable values(?,?,?,?)";
							PreparedStatement insertRepoDependencySt = connect.prepareStatement(insertRepoDepndencyQuery);
							insertRepoDependencySt.setString(1, uniqueDependencies.get(i).getGroupId());
							insertRepoDependencySt.setString(2, uniqueDependencies.get(i).getArtifactId());
							insertRepoDependencySt.setString(3, uniqueDependencies.get(i).getVersion());
							insertRepoDependencySt.setInt(4, sourceRepoId2);
							insertRepoDependencySt.execute();
						}
						
						
					}
				
			}
			stmt.close();
			connect.close();
		}catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
		} catch (MalformedURLException e) {
			System.out.println("MalformedURL");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setValid(){
		valid=true;
	}
	

}
