<%@page import="java.sql.*"%>
<%@page import="java.sql.Connection"%>

<html>
<head>
    <title>Results</title>

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
		"jdbc:mysql://localhost:3306/DependencyManager", "root",
		"Root@wso2");
Statement st = con.createStatement();
%>

	<% String condition=request.getParameter("cBoxDisplay");
		String repository=request.getParameter("txtRepository");
		String groupId=request.getParameter("txtGroupId");
		String artifactId=request.getParameter("txtArtifactId");
		String version=request.getParameter("txtVersion");

		//String snapshot=request.getParameter("checkSnapshots");
	if(condition.equals("AllDep")){
		String query="Select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName from DependencyTable D,RepositoryTable R where D.SourceRepoId=R.RepoId";
		ResultSet rs = st.executeQuery(query);
		out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>Group ID</th>");
		out.println("<th>Artifact ID</th>");
		out.println("<th>Version</th>");
		out.println("<th>Latest Version</th>");
		out.println("<th>Repository</th>");
		out.println("</tr>");
		out.println("</thead>");
		while (rs.next()) {
			out.println("<tr>");
			out.println("<td>"+rs.getString(1)+"</td>");
			out.println("<td>"+rs.getString(2)+"</td>");
			out.println("<td>"+rs.getString(3)+"</td>");
			out.println("<td>"+rs.getString(4)+"</td>");
			out.println("<td>"+rs.getString(5)+"</td>");
			out.println("</tr>");
				}
		
		out.println("</table>");
	}else if(condition.equals("AllRepo")){
		String query="Select RepoId,RepoName from RepositoryTable";
			ResultSet rs = st.executeQuery(query);
			out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th>Repository Id</th>");
			out.println("<th>Repository Name</th>");
			out.println("</tr>");
			out.println("</thead>");
			while (rs.next()) {
				out.println("<tr>");
				out.println("<td>"+rs.getString(1)+"</td>");
				out.println("<td>"+rs.getString(2)+"</td>");
				out.println("</tr>");
					}
	}else if(condition.equals("Dep_Repo")){
		String query="select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName from DependencyTable D,RepositoryTable R where D.SourceRepoId=R.RepoId and R.RepoName='"+repository+"' ";
		ResultSet rs = st.executeQuery(query);
		out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>Group ID</th>");
		out.println("<th>Artifact ID</th>");
		out.println("<th>Version</th>");
		out.println("<th>Latest Version</th>");
		out.println("<th>Repository</th>");
		out.println("</tr>");
		out.println("</thead>");
		while (rs.next()) {
			out.println("<tr>");
			out.println("<td>"+rs.getString(1)+"</td>");
			out.println("<td>"+rs.getString(2)+"</td>");
			out.println("<td>"+rs.getString(3)+"</td>");
			out.println("<td>"+rs.getString(4)+"</td>");
			out.println("<td>"+rs.getString(5)+"</td>");
			out.println("</tr>");
				}
		
		out.println("</table>");
	}else if(condition.equals("Repo_Dep")){
		String query="Select * from RepositoryTable";
		if(!groupId.isEmpty() && !artifactId.isEmpty() && !version.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.GroupId='"+groupId+"' and D.ArtifactId='"+artifactId+"' and D.Version='"+version+"' and D.SourceRepoId=R.RepoId";
		}else if(!groupId.isEmpty() && !artifactId.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.GroupId='"+groupId+"' and D.ArtifactId='"+artifactId+"' and D.SourceRepoId=R.RepoId";
		}else if(!groupId.isEmpty() && !version.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.GroupId='"+groupId+"' and D.Version='"+version+"' and D.SourceRepoId=R.RepoId";
		}else if(!artifactId.isEmpty() && !version.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.ArtifactId='"+artifactId+"' and D.Version='"+version+"' and D.SourceRepoId=R.RepoId";
		}else if(!groupId.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.GroupId='"+groupId+"' and D.SourceRepoId=R.RepoId";
		}else if(!artifactId.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.ArtifactId='"+artifactId+"' and D.SourceRepoId=R.RepoId";
		}else if(!version.isEmpty()){
			query="select R.RepoId,R.RepoName from DependencyTable D,RepositoryTable R where D.Version='"+version+"' and D.SourceRepoId=R.RepoId";
		}
		
		ResultSet rs = st.executeQuery(query);
		out.println("<table id='tbMain' class='display' cellspacing='0' width='100%'>");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th>Repo Id</th>");
		out.println("<th>Repo Name</th>");
		out.println("</tr>");
		out.println("</thead>");
		while (rs.next()) {
			out.println("<tr>");
			out.println("<td>"+rs.getString(1)+"</td>");
			out.println("<td>"+rs.getString(2)+"</td>");
			out.println("</tr>");
				}
		
		out.println("</table>");

	}
	%>
	




</body>
</html>