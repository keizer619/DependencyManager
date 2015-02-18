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


package org.wso2.carbon.build.tools.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.wso2.carbon.build.tools.Constants;
import org.wso2.carbon.build.tools.dto.Dependency;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class VersionManager {

    private static final Log logger = LogFactory.getLog(VersionManager.class);
	private static Connection connect = null;


	static boolean valid = false;

	public void VersionManage(ArrayList<Dependency> uniqueDependencies){


		try {
			Class.forName("com.mysql.jdbc.Driver");
			connect = DriverManager.getConnection(Constants.DATABASE_CONNECTION, Constants.MYSQL_USERNAME,
                    Constants.MYSQL_PASSWORD);
			Statement stmt = connect.createStatement();
			
			if(Constants.DELETE_DATABASE==true){
				String deleteRepositoryDependency="DELETE FROM RepositoryDependencyTable";
				PreparedStatement deleteRepoDepPS = connect.prepareStatement(deleteRepositoryDependency);
				deleteRepoDepPS.execute();
				String deleteDependency="DELETE FROM DependencyTable";
				PreparedStatement deleteDependencyPS = connect.prepareStatement(deleteDependency);
				deleteDependencyPS.execute();
				String deleteRepository="DELETE FROM RepositoryTable";
				PreparedStatement deleteRepositoryPS = connect.prepareStatement(deleteRepository);
				deleteRepositoryPS.execute();
				String alterRepository="ALTER TABLE RepositoryTable AUTO_INCREMENT = 1";
				PreparedStatement alterRepositoryPS = connect.prepareStatement(alterRepository);
				alterRepositoryPS.execute();
			}

			for (int i = 0; i < uniqueDependencies.size(); i++){

                  try{
                    //insert data into RepositoryTable
					ResultSet compareRepository = stmt.executeQuery(
                            "SELECT RepoId FROM RepositoryTable WHERE RepoName='"
                            +uniqueDependencies.get(i).getRepositoryDepends()+"'");
					if(!compareRepository.next()){
						String insertRepositoryQuery="INSERT INTO RepositoryTable VALUES(?,?,?)";
						PreparedStatement insertRepositorySt = connect.prepareStatement(insertRepositoryQuery);
						insertRepositorySt.setString(1,null);
						insertRepositorySt.setString(2,  uniqueDependencies.get(i).getRepositoryDepends());
						insertRepositorySt.setString(3,null);
						insertRepositorySt.execute();
					}
					ResultSet compareSource=stmt.executeQuery(
							"SELECT RepoId FROM RepositoryTable WHERE RepoName='"
									+uniqueDependencies.get(i).getRepositorySource()+"'");
					if(!compareSource.next()){
						String insertRepositoryQuery="INSERT INTO RepositoryTable VALUES(?,?,?)";
						PreparedStatement insertRepositorySt = connect.prepareStatement(insertRepositoryQuery);
						insertRepositorySt.setString(1,null);
						insertRepositorySt.setString(2,  uniqueDependencies.get(i).getRepositorySource());
						insertRepositorySt.setString(3,null);
						insertRepositorySt.execute();
					}
					
					//insert into DependencyTable
					ResultSet compareDependency = stmt.executeQuery(
                            "SELECT GroupId FROM DependencyTable WHERE GroupId='"
                                    +uniqueDependencies.get(i).getGroupId()
                                    +"' AND ArtifactId='"+uniqueDependencies.get(i).getArtifactId()
                                    +"' AND Version='"+uniqueDependencies.get(i).getVersion()+"'");
					if(!compareDependency.next()){
						int sourceRepoId=0;
						ResultSet repoId=stmt.executeQuery("SELECT RepoId FROM RepositoryTable WHERE RepoName='"
                                +uniqueDependencies.get(i).getRepositorySource()+"'");
						while (repoId.next()) {
							sourceRepoId=repoId.getInt(1);
						}
						
						String insertDepndencyQuery="INSERT INTO DependencyTable values(?,?,?,?,?)";
						PreparedStatement insertDependencySt = connect.prepareStatement(insertDepndencyQuery);
						insertDependencySt.setString(1, uniqueDependencies.get(i).getGroupId());
						insertDependencySt.setString(2, uniqueDependencies.get(i).getArtifactId());
						insertDependencySt.setString(3, uniqueDependencies.get(i).getVersion());
						if(uniqueDependencies.get(i).getLatestVersion()!=null){
							insertDependencySt.setString(4, uniqueDependencies.get(i).getLatestVersion());
						}else{
							insertDependencySt.setString(4,"NA");
						}
						insertDependencySt.setInt(5, sourceRepoId);
						insertDependencySt.execute();
					}
                    else {

                        String updateDependencyQuery="UPDATE DependencyTable SET LatestVersion = ? WHERE GroupId = ? AND ArtifactId = ? AND Version = ?";
                        PreparedStatement updateDependencySt = connect.prepareStatement(updateDependencyQuery);
                        if(uniqueDependencies.get(i).getLatestVersion()!=null){
                        	updateDependencySt.setString(1, uniqueDependencies.get(i).getLatestVersion());
						}else{
							updateDependencySt.setString(1,"NA");
						}
                        updateDependencySt.setString(2, uniqueDependencies.get(i).getGroupId());
                        updateDependencySt.setString(3, uniqueDependencies.get(i).getArtifactId());
                        updateDependencySt.setString(4, uniqueDependencies.get(i).getVersion());
                        updateDependencySt.execute();
                    }



						}
                  catch (Exception ex)
                  {
                      logger.error("Exception occurred : " + ex.getMessage());
                  }

                //insert into RepositoryDependencyTable
                        int sourceRepoId2=0;
                        ResultSet repoId=stmt.executeQuery("SELECT RepoId FROM RepositoryTable WHERE RepoName='"
                                +uniqueDependencies.get(i).getRepositoryDepends()+"'");
                        while (repoId.next()) {
                            sourceRepoId2=repoId.getInt(1);
                        }
                        ResultSet compareRepoDependency = stmt.executeQuery (
                                "SELECT * FROM RepositoryDependencyTable WHERE GroupId='"
                                        +uniqueDependencies.get(i).getGroupId()
                                        +"' AND ArtifactId='"+uniqueDependencies.get(i).getArtifactId()
                                        +"' AND Version='"+uniqueDependencies.get(i).getVersion()
                                        +"' AND DependRepoId='"+sourceRepoId2+"'");

                        if(!compareRepoDependency.next()){
                            String insertRepoDependencyQuery="INSERT INTO RepositoryDependencyTable values(?,?,?,?)";
                            PreparedStatement insertRepoDependencySt =
                                    connect.prepareStatement(insertRepoDependencyQuery);
                            insertRepoDependencySt.setString(1, uniqueDependencies.get(i).getGroupId());
                            insertRepoDependencySt.setString(2, uniqueDependencies.get(i).getArtifactId());
                            insertRepoDependencySt.setString(3, uniqueDependencies.get(i).getVersion());
                            insertRepoDependencySt.setInt(4, sourceRepoId2);
                            insertRepoDependencySt.execute();
						
					}
				
			}
			stmt.close();
			connect.close();
		}catch (SQLException ex) {
            logger.error("Exception occurred : " + ex.getMessage());
		}
        catch (ClassNotFoundException ex) {
            logger.error("Exception occurred : " + ex.getMessage());
		}
        catch (Exception ex) {
            logger.error("Exception occurred : " + ex.getMessage());
        }

	}

	public void setValid(){
		valid=true;
	}
	

}