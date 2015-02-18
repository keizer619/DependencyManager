<%@page import="java.sql.*"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Dependency Manager</title>
    <script type="text/javascript" src="js/jquery.js"></script>
    <link type="text/css" rel="stylesheet" href="css/main.css" />
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

<h1>Dependency Manager</h1>

<%
String groupId="";
String artifactId="";
String userName="root";
String password="Root@wso2";
Class.forName("com.mysql.jdbc.Driver");
Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DependencyManager", userName,password);
if(request.getParameter("groupId")!=null){;
	groupId=request.getParameter("groupId");	
}
if(request.getParameter("artifactId")!=null){;
	artifactId=request.getParameter("artifactId");	
}
%>
	<FORM name="formIndex1" action="DisplayData.jsp" METHOD="POST">
		<select id="cBoxChoice" name="cBoxChoice" onchange="loadValues(this.value)">
			<option selected disabled>--Select--</option>
			<option value="Artifact">Artifacts</option>
			<option value="Repository">Repositories</option>
		</select> 
		<select id="cBoxRepository" name="cBoxRepository" onchange="showButtons(this.value)" style="display: none">
			<option selected disabled id="selectOption">--Select Repository--</option>
		</select>

        <input type="checkbox" name="snapshotVersions" id="snapshotVersions"
               style="display: none" onchange="snapshotChange()" />
        <p id="text" style="display:none">SNAPSHOT Versions  </p>
        <input type="checkbox" name="thirdParty" id="thirdParty" style="display: none" onchange="thirdPartyChange()"/>
        <p id="textThirdParty" style="display:none">Third Party Dependencies</p>



		
		<select id="cBoxGroup" name="cBoxGroup" onChange="showArtifactValue(this.value)" style="display: none" >
			<option selected disabled id="selectOption">--Select GroupId--</option>
		</select>
		<select id="cBoxArtifact" name="cBoxArtifact" onchange="showVersion(this.value)" style="display: none">
			<option selected disabled id="selectOption">--Select ArtifactId--</option>
			<% if(!groupId.equals("")){			
				Statement st = con.createStatement();
				String query = "select ArtifactID from DependencyTable where GroupId='"+groupId+"' " +
                        "group by ArtifactID order by ArtifactID";
				ResultSet rs= st.executeQuery(query);
				while(rs.next()){		
                   %>
 					<option value=<%=rs.getString(1)%>><%=rs.getString(1)%></option>
                   <%
                }						
			}
			%>
		</select >
		<select id="cBoxVersion" name="cBoxVersion" style="display: none" onchange="displayUsgaeButton()">
					<option selected disabled id="selectOption">--Select Version--</option>
					<% if(!artifactId.equals("")){			
				Statement st = con.createStatement();
				String query = "select Version from DependencyTable where ArtifactId='"+artifactId+"'";
				ResultSet rs= st.executeQuery(query);
				while(rs.next()){		
                   %>
 					<option value=<%=rs.getString(1)%>><%=rs.getString(1)%></option>
                   <%
                }						
			}
			%>
		</select>

        <br/><br/>
        <input type="submit" value="Show Dependencies" id="btnShowDependencies" name="btnShowDependencies"
               style="display: none" />
        <input type="submit" value="Show Artifacts" id="btnShowArtifacts" name="btnShowArtifacts"
               style="display: none" />
        <input type="submit" value="Show Usage" id="btnShowUsage" name="btnShowUsage" style="display: none" />
		
	</FORM>

<button id="btnShowRepoGraph" name="btnShowRepoGraph" style="display: none" onclick="openGraph('repo')">
    Show Repository graph
</button>
<button id="btnShowArtifactGraph" name="btnShowArtifactGraph" style="display: none"
        onclick="openGraph('Artifact')">Show Artifact graph</button>




	<script>
	function loadValues(value){ 
		//when option 'Repository' is selected 
		if(value=="Repository"){
			document.getElementById("cBoxGroup").style="display:none";
			document.getElementById("cBoxArtifact").style="display:none";
			document.getElementById("cBoxVersion").style="display:none";
			document.getElementById("btnShowUsage").style="display:none";

			document.getElementById("cBoxRepository").style="display:inline";
			var opt = document.createElement("option");
			document.getElementById("cBoxRepository").options.add(opt);
			opt.text="All Repositories";
			opt.value="All";
			<%
			Statement st = con.createStatement();
			String query = "select RepoName from RepositoryTable order by RepoName";
			ResultSet rs = st.executeQuery(query);
			while (rs.next()) {
				int count = 0;
				Statement st1 = con.createStatement();
				String query1 = "select count(RD.GroupId) from RepositoryDependencyTable RD,RepositoryTable R" +
				 " where R.RepoId=RD.DependRepoId and R.RepoName='"+ rs.getString(1) + "' ";
				ResultSet rs1 = st1.executeQuery(query1);
				while (rs1.next()) {
					count = rs1.getInt(1);
				}
				String depndencyNo = Integer.toString(count);
				String optionValue = rs.getString(1).concat(
						"(No of dependencies:" + depndencyNo + ")");%>
				var opt = document.createElement("option");
				document.getElementById("cBoxRepository").options.add(opt);
				opt.text="<%out.print(optionValue);%>";
				opt.value="<%out.print(rs.getString(1));%>";
				document.getElementById("cBoxRepository").options.add(opt);
			<%}%>
			}else if(value=="Artifact"){
				
				document.getElementById("cBoxGroup").style="display:inline";
				document.getElementById("cBoxRepository").style="display:none";
				document.getElementById("btnShowDependencies").style="display:none";
				document.getElementById("btnShowArtifacts").style="display:none";	
				document.getElementById("btnShowRepoGraph").style="display:none";	
				document.getElementById("btnShowArtifactGraph").style="display:none";	
				document.getElementById("snapshotVersions").style="display:none";	
				document.getElementById("text").style="display:none";
				document.getElementById("thirdParty").style="display:none";	
				document.getElementById("textThirdParty").style="display:none";
								
				<%
				Statement st2 = con.createStatement();
				String query2 = "select GroupId from DependencyTable group by GroupId order by GroupId";
				ResultSet rs2 = st2.executeQuery(query2);
				while(rs2.next()){
					%>
		
					var opt = document.createElement("option");
					document.getElementById("cBoxGroup").options.add(opt);
					opt.text="<%out.print(rs2.getString(1));%>";
					opt.value="<%out.print(rs2.getString(1));%>";
					document.getElementById("cBoxGroup").options.add(opt);
				<%}
				%>
			}
		}

		function showButtons(value) {
			document.getElementById("btnShowDependencies").style = "display:inline";
			document.getElementById("btnShowArtifacts").style = "display:inline";
			document.getElementById("btnShowRepoGraph").style = "display:inline";
			document.getElementById("btnShowArtifactGraph").style = "display:inline";
			document.getElementById("snapshotVersions").style = "display:inline";
			document.getElementById("text").style = "display:inline";
			document.getElementById("thirdParty").style = "display:inline";
			document.getElementById("textThirdParty").style = "display:inline";

		}
		
		function showArtifactValue(value){	
			document.getElementById("cBoxArtifact").style = "display:inline";
			var form = document.createElement("form");
            form.setAttribute("method", "post");
            form.setAttribute("action", "index.jsp");
            form.setAttribute("target", "_self");
            
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'groupId';
            input.value = value;
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
            document.body.removeChild(form);
		}	
		
		function showVersion(value){
			document.getElementById("cBoxVersion").style = "display:inline";
			var form = document.createElement("form");
            form.setAttribute("method", "post");
            form.setAttribute("action", "index.jsp");
            form.setAttribute("target", "_self");
            
            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'groupId';
            input.value =  document.getElementById("cBoxGroup").value;
            form.appendChild(input);
           
            input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'artifactId';
            input.value =  document.getElementById("cBoxArtifact").value;
            form.appendChild(input);
            document.body.appendChild(form);
            form.submit();
            document.body.removeChild(form);
		}
		
		function displayUsgaeButton(){
			document.getElementById("btnShowUsage").style = "display:inline";
		}
		
		function snapshotChange(){
			if(document.getElementById('snapshotVersions').checked){
				document.getElementById("thirdParty").disabled = true;
			}else{
				document.getElementById("thirdParty").disabled = false;
			}
		}
		
		function thirdPartyChange(){
			if(document.getElementById('thirdParty').checked){
                document.getElementById("snapshotVersions").disabled = true;
                document.getElementById("btnShowArtifacts").style = "display:none";
                document.getElementById("btnShowArtifactGraph").style = "display:none";
                document.getElementById("btnShowRepoGraph").style = "display:none";

			}else{
                document.getElementById("snapshotVersions").disabled = false;
                document.getElementById("btnShowArtifacts").style = "display:inline";
                document.getElementById("btnShowArtifactGraph").style = "display:inline";
                document.getElementById("btnShowRepoGraph").style = "display:inline";
			}
		}
		
		function openGraph(value){
		
			var repo = document.getElementById("cBoxRepository").options[document.getElementById("cBoxRepository")
                                    .selectedIndex].text;
			var repository=repo.substring(0, repo.indexOf('(')); 
			
            var form = document.createElement("form");
            form.setAttribute("method", "post");
            form.setAttribute("action", "graph.jsp");
            form.setAttribute("target", "graph.jsp");

            var input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'graphType';
            if(value=="repo"){
            	input.value = 'repositories';
            }else{
            	input.value = 'artifacts';
            }
            form.appendChild(input);

            input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'repositoryName';
			if(repo!="All Repositories"){
				input.value =  repository;    	
			}else{
				input.value =  "";
			}			
       	 	form.appendChild(input);        

            input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'snapshots';
            if(document.getElementById("snapshotVersions").checked){
            	input.value = 'true';
            }else{
            	input.value = 'false';
            }
            form.appendChild(input);

            document.body.appendChild(form);
            form.submit();
            document.body.removeChild(form);
		}
	</script>
	
	<% if(!artifactId.equals("")){%>
	<script>			
	  	var element = document.getElementById('cBoxChoice');
	    element.value = "Artifact";
	    document.getElementById("cBoxChoice").onchange();
	    
	    element = document.getElementById("cBoxGroup");
	    element.value = "<%out.print(groupId);%>";
	    document.getElementById("cBoxArtifact").style = "display:inline";
	    
	    element = document.getElementById("cBoxArtifact");
	    element.value = "<%out.print(artifactId);%>";
	    document.getElementById("cBoxVersion").style = "display:inline";
				  
	</script>
		<%} %>
	
	
	<% if(!groupId.equals("")){%>
	<script>			
				  var element = document.getElementById('cBoxChoice');
				    element.value = "Artifact";
				    document.getElementById("cBoxChoice").onchange();
				    
				    element = document.getElementById("cBoxGroup");
				    element.value = "<%out.print(groupId);%>";
				    document.getElementById("cBoxArtifact").style = "display:inline";
				</script>
				 <%} %>
				 

	
	

</body>
</html>