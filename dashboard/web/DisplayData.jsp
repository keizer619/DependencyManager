<%@page import="java.sql.*"%>
<%@page import="javax.sql.*"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.PreparedStatement"%>


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
		Class.forName("com.mysql.jdbc.Driver");
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/DependencyManager", "nishali",
				"thilanka");
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
			Statement st = con.createStatement();
			String query="";
			if (repository.equalsIgnoreCase("All")) {
				query="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID";
				if(thirdParty!=null){
					query="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID WHERE D.SourceRepoId='2'";
				}
			} else {
				query="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID WHERE R.RepoName = '"+repository+"'";
				if(thirdParty!=null){
					query="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID WHERE R.RepoName = '"+repository+"' and D.SourceRepoId='2'";
				}
			}
			
			if(snapshot!=null){
				query=query.concat(" AND D.Version LIKE '%snapshot%'");
			}
			
			ResultSet rs = st.executeQuery(query);
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
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>" + rs.getString(1) + "</td>");
				out.println("<td>" + rs.getString(2) + "</td>");
				out.println("<td>" + rs.getString(3) + "</td>");
				out.println("<td>" + rs.getString(4) + "</td>");
				out.println("<td>" + rs.getString(5) + "</td>");
				out.println("<td>" + rs.getString(6) + "</td>");
				out.println("</tr>");
			}

			out.println("</table>");
			
			//if Show Artifact button is clicked
		}else if(showArtifact!=null && showArtifact.equals("Show Artifacts")){

			String repository = request.getParameter("cBoxRepository");
			Statement st = con.createStatement();
			String query="";
			if (repository.equalsIgnoreCase("All")) {
				query = "select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName,'' from DependencyTable D, RepositoryTable R where D.SourceRepoId=R.RepoId ";
			} else {
				query = "select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName,'' from DependencyTable D, RepositoryTable R where R.RepoName='"
						+ repository
						+ "' and R.RepoId=D.SourceRepoId";
			}
			
			if(snapshot!=null){
				query=query.concat(" AND D.Version LIKE '%snapshot%'");
			}
			ResultSet rs = st.executeQuery(query);
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
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>" + rs.getString(1) + "</td>");
				out.println("<td>" + rs.getString(2) + "</td>");
				out.println("<td>" + rs.getString(3) + "</td>");
				out.println("<td>" + rs.getString(4) + "</td>");
				out.println("<td>" + rs.getString(5) + "</td>");
				out.println("<td>" + rs.getString(6) + "</td>");
				out.println("</tr>");
			}

			out.println("</table>");
			choice=null;
		
		}else if(showUsage!=null && showUsage.equals("Show Usage")){
			String groupId=request.getParameter("cBoxGroup");
			String artifactId=request.getParameter("cBoxArtifact");
			String version=request.getParameter("cBoxVersion");
			Statement st = con.createStatement();
			//String query="select RD.GroupId,RD.ArtifactId,RD.Version,D.LatestVersion,'',R.RepoName from DependencyTable D,RepositoryTable R,RepositoryDependencyTable RD where D.GroupId='"+groupId+"' and D.ArtifactId='"+artifactId+"' and D.Version='"+version+"' and RD.DependRepoId=R.RepoId and RD.GroupId=D.GroupId and RD.ArtifactId=D.ArtifactID and RD.Version=D.Version";
			String query="SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion, RR.RepoName, R.RepoName FROM (DependencyManager.RepositoryTable R join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID where RD.GroupId='"+groupId+"' and RD.ArtifactId='"+artifactId+"' and RD.Version='"+version+"' and RD.DependRepoId=R.RepoId and RD.GroupId=D.GroupId and RD.ArtifactId=D.ArtifactID and RD.Version=D.Version";
			ResultSet rs = st.executeQuery(query);
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
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>" + rs.getString(1) + "</td>");
				out.println("<td>" + rs.getString(2) + "</td>");
				out.println("<td>" + rs.getString(3) + "</td>");
				out.println("<td>" + rs.getString(4) + "</td>");
				out.println("<td>" + rs.getString(5) + "</td>");
				out.println("<td>" + rs.getString(6) + "</td>");
				out.println("</tr>");
			}

			out.println("</table>");
		}
	%>
</body>
</html>