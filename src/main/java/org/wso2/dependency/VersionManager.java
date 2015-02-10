package org.wso2.dependency;

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

import org.wso2.sample.library.Dependency;
import org.wso2.dependency.UrlValidate;

/**
 * @author 		W.A.T. Nishali Wijesinghe	(nishaliw@wso2.com)				
 * @since 		2015-02-09 				
 */

public class VersionManager {


	private static Connection connect = null;
	private static String username="nishali";
	private static String password="thilanka";

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
			connect = DriverManager.getConnection("jdbc:mysql://localhost/DependencyManager", username, password);
			Statement stmt = connect.createStatement();

			for (int i = 0; i < uniqueDependencies.size(); i++){

				String insertQuery="INSERT INTO UniqueDependencyTable values(?,?,?,?,?,?)";
				PreparedStatement ps = connect.prepareStatement(insertQuery);
				ps.setString(1, null);
				ps.setString(2, uniqueDependencies.get(i).getRepositoryDepends());
				ps.setString(3, uniqueDependencies.get(i).getGroupId());
				ps.setString(4, uniqueDependencies.get(i).getArtifactId());
				ps.setString(5, uniqueDependencies.get(i).getVersion());
				ps.setString(6, null);
				ps.execute();


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


					ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS id FROM UniqueDependencyTable");

					while (rs.next()) {
						maxId = rs.getInt(1);
					}

					String insertVersionQuery = "update UniqueDependencyTable set latestVersion = ? where id = ?";
					PreparedStatement update = connect.prepareStatement(insertVersionQuery);
					update.setString(1, latestVersion);
					update.setInt(2, maxId);
					update.execute();

				}
			}
			stmt.close();
			connect.close();
		}catch (SQLException e) {
			System.out.println("Error-Insert pom file details");
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
