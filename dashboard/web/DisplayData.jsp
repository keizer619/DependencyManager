<%@page import="java.sql.*"%>

<html>
<head>
<title>Display Data</title>
    <script type="text/javascript" src="js/jquery.js"></script>
    <link type="text/css" rel="stylesheet" href="css/jquery.dataTables.css" />
    <script type="text/javascript" src="js/jquery.dataTables.js"></script>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#tbMain').dataTable( {
                "paging":   true,
                "ordering": true,
                "info":     true
            } );
        } );
    </script>
</head>
<body>

	<%
		String userName="nishali";
		String password="thilanka";
		Class.forName("com.mysql.jdbc.Driver");
		Connection con1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/DependencyManager", userName,password);
	%>

	<%
		String choice = request.getParameter("cBoxChoice");
		String showDependency=request.getParameter("btnShowDependencies");
		String showArtifact=request.getParameter("btnShowArtifacts");
		String showUsage=request.getParameter("btnShowUsage");
		String snapshot=request.getParameter("snapshotVersions");
		String thirdParty=request.getParameter("thirdParty");
		
		//if Show Dependency button is clicked
		if (showDependency!=null && showDependency.equals("Show Dependencies")) {
			String repository = request.getParameter("cBoxRepository");
			Statement st1 = con1.createStatement();
			String query1="";
			if (repository.equalsIgnoreCase("All")) {
				query1="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID";
				if(thirdParty!=null){
					query1="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID WHERE D.SourceRepoId='2'";
				}
			} else {
				query1="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID WHERE R.RepoName = '"+repository+"'";
				if(thirdParty!=null){
					query1="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID WHERE R.RepoName = '"+repository+"' and D.SourceRepoId='2'";
				}
			}
			
			if(snapshot!=null){
				query1=query1.concat(" AND D.Version LIKE '%snapshot%'");
			}
			
			ResultSet rs1 = st1.executeQuery(query1);
			out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Group ID</th>");
			out.println("<th>Artifact ID</th>");
			out.println("<th>Version</th>");
			out.println("<th>Latest Version</th>");
			out.println("<th>Source Repository</th>");
			out.println("<th>Depend Repository</th>");
			out.println("</tr>");
			out.println("</thead>");
			while (rs1.next()) {
				out.println("<tr>");
				out.println("<td>" + rs1.getString(1) + "</td>");
				out.println("<td>" + rs1.getString(2) + "</td>");
				out.println("<td>" + rs1.getString(3) + "</td>");
				out.println("<td>" + rs1.getString(4) + "</td>");
				out.println("<td>" + rs1.getString(5) + "</td>");
				out.println("<td>" + rs1.getString(6) + "</td>");
				out.println("</tr>");
			}

			out.println("</table>");
			
			//if Show Artifact button is clicked
		}else if(showArtifact!=null && showArtifact.equals("Show Artifacts")){

			String repository = request.getParameter("cBoxRepository");
			Statement st1 = con1.createStatement();
			String query1="";
			if (repository.equalsIgnoreCase("All")) {
				query1 = "select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName,'' from DependencyTable D, RepositoryTable R where D.SourceRepoId=R.RepoId ";
			} else {
				query1 = "select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName,'' from DependencyTable D, RepositoryTable R where R.RepoName='"
						+ repository
						+ "' and R.RepoId=D.SourceRepoId";
			}
			
			if(snapshot!=null){
				query1=query1.concat(" AND D.Version LIKE '%snapshot%'");
			}
			ResultSet rs1 = st1.executeQuery(query1);
			out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Group ID</th>");
			out.println("<th>Artifact ID</th>");
			out.println("<th>Version</th>");
			out.println("<th>Latest Version</th>");
			out.println("<th>Source Repository</th>");
			out.println("<th>Depend Repository</th>");
			out.println("</tr>");
			out.println("</thead>");
			while (rs1.next()) {
				out.println("<tr>");
				out.println("<td>" + rs1.getString(1) + "</td>");
				out.println("<td>" + rs1.getString(2) + "</td>");
				out.println("<td>" + rs1.getString(3) + "</td>");
				out.println("<td>" + rs1.getString(4) + "</td>");
				out.println("<td>" + rs1.getString(5) + "</td>");
				out.println("<td>" + rs1.getString(6) + "</td>");
				out.println("</tr>");
			}

			out.println("</table>");
			choice=null;
		
		}else if(showUsage!=null && showUsage.equals("Show Usage")){
			String groupId1=request.getParameter("cBoxGroup");
			String artifactId1=request.getParameter("cBoxArtifact");
			String version1=request.getParameter("cBoxVersion");
			Statement st1 = con1.createStatement();
			//String query1="select RD.GroupId,RD.ArtifactId,RD.Version,D.LatestVersion,'',R.RepoName from DependencyTable D,RepositoryTable R,RepositoryDependencyTable RD where D.GroupId='"+groupId1+"' and D.ArtifactId='"+artifactId1+"' and D.Version='"+version1+"' and RD.DependRepoId=R.RepoId and RD.GroupId=D.GroupId and RD.ArtifactId=D.ArtifactID and RD.Version=D.Version";
			String query1="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion, RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID where RD.GroupId='"+groupId1+"' and RD.ArtifactId='"+artifactId1+"' and RD.Version='"+version1+"' and RD.DependRepoId=R.RepoId and RD.GroupId=D.GroupId and RD.ArtifactId=D.ArtifactID and RD.Version=D.Version";
			ResultSet rs1 = st1.executeQuery(query1);
			out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Group ID</th>");
			out.println("<th>Artifact ID</th>");
			out.println("<th>Version</th>");
			out.println("<th>Latest Version</th>");
			out.println("<th>Source Repository</th>");
			out.println("<th>Depend Repository</th>");
			out.println("</tr>");
			out.println("</thead>");
			while (rs1.next()) {
				out.println("<tr>");
				out.println("<td>" + rs1.getString(1) + "</td>");
				out.println("<td>" + rs1.getString(2) + "</td>");
				out.println("<td>" + rs1.getString(3) + "</td>");
				out.println("<td>" + rs1.getString(4) + "</td>");
				out.println("<td>" + rs1.getString(5) + "</td>");
				out.println("<td>" + rs1.getString(6) + "</td>");
				out.println("</tr>");
			}

			out.println("</table>");
		}
	%>
</body>
</html>