<%@page import="java.sql.*"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Map" %>

<%!
    public static int MAX_RECUSION_DEPTH = 2;
    private static HashMap<String, ArrayList<String>> nodes;

    public String loadJson(String graphType, String repositoryName, String isSnapshots, String json) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/DependencyManager", "root",
                    "Root@wso2");
            Statement st = con.createStatement();

            String query;

            if (graphType.equals("artifacts")) {
                query = "SELECT DISTINCT r.RepoName As DependRepo , " +
                        "CONCAT(rr.RepoName,' (',d.GroupId,':',d.ArtifactId,':',d.Version,')') AS SourceArtifact" +
                        " FROM (DependencyManager.RepositoryTable r JOIN DependencyManager.RepositoryDependencyTable rd " +
                        "ON r.RepoID = rd.DependRepoId) JOIN DependencyManager.DependencyTable d " +
                        "ON rd.ArtifactID = d.ArtifactId AND rd.GroupId = d.GroupId AND rd.Version = d.Version " +
                        "JOIN DependencyManager.RepositoryTable rr ON d.SourceRepoId = rr.RepoID " +
                        "WHERE r.RepoName != rr.RepoName";
            } else {
                query = "SELECT DISTINCT r.RepoName AS DependRepo , rr.RepoName AS SourceRepo " +
                        "FROM (DependencyManager.RepositoryTable r JOIN DependencyManager.RepositoryDependencyTable rd " +
                        "ON r.RepoID = rd.DependRepoId) JOIN DependencyManager.DependencyTable d " +
                        "ON rd.ArtifactID = d.ArtifactId AND rd.GroupId = d.GroupId AND rd.Version = d.Version " +
                        "JOIN DependencyManager.RepositoryTable rr ON d.SourceRepoId = rr.RepoID " +
                        "WHERE r.RepoName != rr.RepoName";
            }

            if (isSnapshots.equals("true")) {
                query += " AND d.Version LIKE '%snapshot%'";
            }

            ResultSet rs = st.executeQuery(query);

            nodes = new HashMap<String, ArrayList<String>>();

            while (rs.next()) {

                if (!repositoryName.equals("")){
                    ArrayList<String> dep =  nodes.get(rs.getString(1));

                    if (dep == null){
                        dep = new ArrayList<String>();
                    }

                    dep.add(rs.getString(2));

                    nodes.put(rs.getString(1), dep);
                }
                else {
                    json += '"' + rs.getString(1) + '"' + "->" + '"'
                            + rs.getString(2) + '"' + ";";

                }
            }

            st.close();
            con.close();

            if (!repositoryName.equals("")) {
                json += constructJson(repositoryName, 0);
            }

            System.out.println(json);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return json;
    }

    private String constructJson(String repoName, int count) {

        String json = "";
        ArrayList<String> dep =nodes.get(repoName);

        if (dep != null){
            for (int i = 0; i  < dep.size(); i++){
                json += '"' + repoName + '"' + "->" + '"'
                        + dep.get(i) + '"' + ";";
                if ( count < MAX_RECUSION_DEPTH) {
                    json += constructJson(dep.get(i), count + 1);
                }
            }
        }

        return  json;
    }
%>

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



<!-- graph content-->
<script type="text/javascript" src="js/d3.v3.js"></script>
<script type="text/javascript" src="js/graphlib-dot.js"></script>
<script type="text/javascript" src="js/dagre-d3.js"></script>

<style type="text/css">
    svg {
        border: 1px solid #999;
        overflow: hidden;
    }

    .node {
        white-space: nowrap;
    }

    .node rect,
    .node circle,
    .node ellipse {
        stroke: #333;
        fill: #fff;
        stroke-width: 1.5px;
    }

    .cluster rect {
        stroke: #333;
        fill: #000;
        fill-opacity: 0.1;
        stroke-width: 1.5px;
    }

    .edgePath path.path {
        stroke: #333;
        stroke-width: 1.5px;
        fill: none;
    }
</style>

<style>
    h1, h2 {
        color: #333;
    }

    textarea {
        width: 800px;
    }

    label {
        margin-top: 1em;
        display: block;
    }

    .error {
        color: red;
    }
</style>

    <script>
        function showDependencies(repoId, source){

        var form = document.createElement("form");
        form.setAttribute("method", "post");
        form.setAttribute("action", "index.jsp");
        form.setAttribute("target", "_self");

        input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'cBoxRepository';
        input.value= repoId;
        form.appendChild(input);

        input = document.createElement('input');
        input.type = 'hidden';

        if (source == "dependencies") {
            input.name = 'btnShowDependencies';
            input.value= 'Show Dependencies';
        }
        else {
            input.name = 'btnShowArtifacts';
            input.value= 'Show Artifacts';
        }
        form.appendChild(input);

        document.body.appendChild(form);
        form.submit();
        document.body.removeChild(form);

    }
    </script>


</head>



<body>

<h1>Dependency Manager</h1>

<%

HashMap<String, ArrayList<String>> groupIds = new HashMap<String, ArrayList<String>>();
HashMap<String, ArrayList<String>> artifactIds = new HashMap<String, ArrayList<String>>();

String showDependency = "";
String showArtifact = "";
String showUsage = "";
String snapshot = "";
String thirdParty = "";
String json ="";
String choice = "";
String groupId = "";
String artifactId = "";
String repositoryId = "";
String version ="";
String userName="root";
String password="Root@wso2";
Class.forName("com.mysql.jdbc.Driver");

Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/DependencyManager", userName,password);
if(request.getParameter("groupId")!=null){
	groupId=request.getParameter("groupId");	
}
if(request.getParameter("artifactId")!=null){
	artifactId=request.getParameter("artifactId");	
}
if(request.getParameter("choice")!=null){
    choice=request.getParameter("choice");
}

if(request.getParameter("repositoryId")!=null){
    repositoryId=request.getParameter("repositoryId");
}

if (request.getParameter("btnShowDependencies") != null) {
    showDependency = request.getParameter("btnShowDependencies");
    repositoryId =  request.getParameter("cBoxRepository");
    choice="Repository";
}

if(request.getParameter("btnShowArtifacts") != null) {
    showArtifact = request.getParameter("btnShowArtifacts");
    repositoryId =  request.getParameter("cBoxRepository");
    choice="Repository";
}

if(request.getParameter("btnShowUsage") != null) {
    showUsage = request.getParameter("btnShowUsage");
    choice="Artifact";
    groupId = request.getParameter("cBoxGroup");
    artifactId = request.getParameter("cBoxArtifact");
    version = request.getParameter("cBoxVersion");
}

if (request.getParameter("snapshotVersions") != null) {
    snapshot = request.getParameter("snapshotVersions");
}

if(request.getParameter("thirdParty") != null) {
    thirdParty = request.getParameter("thirdParty");
}




    Statement st = con.createStatement();
    String query = "select GroupId, ArtifactID, Version from DependencyTable";
    ResultSet rs= st.executeQuery(query);
    while(rs.next()){

        ArrayList<String> artifacts = groupIds.get(rs.getString(1));

        if (artifacts == null) {
            artifacts = new ArrayList<String>();
        }

        artifacts.add(rs.getString(2));
        groupIds.put(rs.getString(1), artifacts);

        ArrayList<String> versions =  artifactIds.get(rs.getString(2));

        if (versions == null) {
            versions = new ArrayList<String>();
        }

        versions.add(rs.getString(3));
        artifactIds.put(rs.getString(2), versions);

    }


%>
	<FORM name="formIndex1" action="index.jsp" METHOD="POST">
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
            <option id="All">All Groups</option>
		</select>
		<select id="cBoxArtifact" name="cBoxArtifact" onchange="showVersion(this.value)" style="display: none">
			<option selected disabled id="selectOption">--Select ArtifactId--</option>
            <option id="All">All Artifacts</option>
            <% if(!groupId.equals("")){

                ArrayList<String> artifacts = new ArrayList<String>();

                if (!groupId.equals("All Groups")){
                    artifacts = groupIds.get(groupId);
                }
                else {
                    Iterator it = groupIds.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        artifacts.addAll((ArrayList<String>) pair.getValue());
                        it.remove();
                    }

                }



                for (int i =0; i < artifacts.size(); i++){
            %>
            <option value=<%=artifacts.get(i)%>><%=artifacts.get(i)%></option>
            <%
                    }
                }
            %>
		</select >
		<select id="cBoxVersion" name="cBoxVersion" style="display: none" onchange="displayUsgaeButton()">
					<option selected disabled id="selectOption">--Select Version--</option>
                    <option id="All">All Versions</option>
            <% if(!artifactId.equals("")){

                ArrayList<String> versions = new ArrayList<String>();


                if (!artifactId.equals("All Artifacts")){
                    versions = artifactIds.get(artifactId);
                }
                else {
                    Iterator it = artifactIds.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        versions.addAll((ArrayList<String>)pair.getValue());
                        it.remove();
                    }

                }

                for (int i =0; i < versions.size(); i++){
            %>
            <option value=<%=versions.get(i)%>><%=versions.get(i)%></option>
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
    Show Graph (by Repositories)
</button>
<button id="btnShowArtifactGraph" name="btnShowArtifactGraph" style="display: none"
        onclick="openGraph('Artifact')">Show Graph (by Artifacts)</button>

<form>
    <textarea id="inputGraph" rows="5" style="display: block"/></textarea>
    <a id="graphLink">Link for this graph</a>
    <svg id="svgGraph" width=100% height=600>
        <g/>
    </svg>
</form>



	<script>
    function loadCombos(){
    	document.getElementById("cBoxRepository").style="display:inline";
			var opt = document.createElement("option");
			document.getElementById("cBoxRepository").options.add(opt);
			opt.text="All Repositories";
			opt.value="All";
			<%
        st = con.createStatement();
        query = "select RepoName from RepositoryTable order by RepoName";
        rs = st.executeQuery(query);
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

    <%

        Iterator it = groupIds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

    %>

                var opt = document.createElement("option");
                document.getElementById("cBoxGroup").options.add(opt);
                opt.text="<%out.print(pair.getKey());%>";
                opt.value="<%out.print(pair.getKey());%>";
                document.getElementById("cBoxGroup").options.add(opt);
                <%
            it.remove(); // avoids a ConcurrentModificationException
        }


    %>

    }

	function loadValues(value){
		//when option 'Repository' is selected
		if(value=="Repository"){
			document.getElementById("cBoxGroup").style="display:none";
			document.getElementById("cBoxArtifact").style="display:none";
			document.getElementById("cBoxVersion").style="display:none";
			document.getElementById("btnShowUsage").style="display:none";


			}else if(value=="Artifact"){
			    document.getElementById("svgGraph").style.display = "none";
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
            form.setAttribute("action", "index.jsp");
            form.setAttribute("target", "_self");

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

            input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'choice';
            input.value= document.getElementById("cBoxChoice").value;
            form.appendChild(input);

            input = document.createElement('input');
            input.type = 'hidden';
            input.name = 'repositoryId';
            input.value= document.getElementById("cBoxRepository").value;
            form.appendChild(input);

            document.body.appendChild(form);
            form.submit();
            document.body.removeChild(form);
		}

		loadCombos();
	</script>

<%
    if (!choice.equals("")) {
%>
<script>
            document.getElementById("cBoxChoice").value = "<%out.print(choice);%>";
            loadValues(document.getElementById("cBoxChoice").value);
        </script>
<%
    }
%>

<%
    if (!repositoryId.equals("")) {
%>
<script>
            document.getElementById("cBoxRepository").value = "<%out.print(repositoryId);%>";
            showButtons(document.getElementById("cBoxRepository").value);
        </script>
<%
    }
%>
	
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


<% if(!version.equals("")){%>
<script>
				  var element = document.getElementById('cBoxChoice');
				    element.value = "Artifact";
				    document.getElementById("cBoxChoice").onchange();

				    element = document.getElementById("cBoxVersion");
				    element.value = "<%out.print(version);%>";
				    document.getElementById("cBoxVersion").style = "display:inline";
				</script>
<%} %>




	
<!-- graph content-->

<%
    if(request.getParameter("graphType")!=null && request.getParameter("repositoryName")!=null &&
            request.getParameter("snapshots")!=null) {
        json = "digraph {" + loadJson(request.getParameter("graphType"), request.getParameter("repositoryName"),
                request.getParameter("snapshots"), "") + "}";
    }
%>



    <script type="text/javascript">

        document.getElementById("inputGraph").value = '<%out.print(json);%>';
        document.getElementById("inputGraph").style.display = "none";
        document.getElementById("graphLink").style.display = "none";
    </script>

    <script type="text/javascript">
        function graphToURL() {
            var elems = [window.location.protocol, '//',
                window.location.host,
                window.location.pathname,
                '?'];

            var queryParams = [];
            if (debugAlignment) {
                queryParams.push('alignment=' + debugAlignment);
            }
            queryParams.push('graph=' + encodeURIComponent(inputGraph.value));
            elems.push(queryParams.join('&'));

            return elems.join('');
        }

        var inputGraph = document.querySelector("#inputGraph");

        var graphLink = d3.select("#graphLink");

        var oldInputGraphValue;

        var graphRE = /[?&]graph=([^&]+)/;
        var graphMatch = window.location.search.match(graphRE);
        if (graphMatch) {
            inputGraph.value = decodeURIComponent(graphMatch[1]);
        }
        var debugAlignmentRE = /[?&]alignment=([^&]+)/;
        var debugAlignmentMatch = window.location.search.match(debugAlignmentRE);
        var debugAlignment;
        if (debugAlignmentMatch) debugAlignment = debugAlignmentMatch[1];

        // Set up zoom support
        var svg = d3.select("svg"),
                inner = d3.select("svg g"),
                zoom = d3.behavior.zoom().on("zoom", function() {
                    inner.attr("transform", "translate(" + d3.event.translate + ")" +
                            "scale(" + d3.event.scale + ")");
                });
        svg.call(zoom);

        // Create and configure the renderer
        var render = dagreD3.render();

        function tryDraw() {
            var g;
            if (oldInputGraphValue !== inputGraph.value) {
                inputGraph.setAttribute("class", "");
                oldInputGraphValue = inputGraph.value;
                try {
                    g = graphlibDot.read(inputGraph.value);
                } catch (e) {
                    inputGraph.setAttribute("class", "error");
                    throw e;
                }

                // Save link to new graph
                graphLink.attr("href", graphToURL());

                // Set margins, if not present
                if (!g.graph().hasOwnProperty("marginx") &&
                        !g.graph().hasOwnProperty("marginy")) {
                    g.graph().marginx = 20;
                    g.graph().marginy = 20;
                }

                g.graph().transition = function(selection) {
                    return selection.transition().duration(500);
                };

                // Render the graph into svg g
                d3.select("svg g").call(render, g);
            }
        }

        <% if ( !(json.equals(""))){ %>
        document.getElementById("svgGraph").style.display = "inline";
        tryDraw();
        <% }
            else {
            %>

        document.getElementById("svgGraph").style.display = "none";

            <%} %>

    </script>




<%

    if (!showDependency.equals("") ||
            !showArtifact.equals("") ||
            !showUsage.equals("") ||
            !snapshot.equals("") ||
            !thirdParty.equals("") ) {


        int repoId = 0;

        if (thirdParty != null) {
             st = con.createStatement();
            String queryOther = "Select RepoID from RepositoryTable where RepoName='Other'";
             rs = st.executeQuery(queryOther);
            while (rs.next()) {
                repoId = rs.getInt(1);
            }
        }

        //if Show Dependency button is clicked
        if (showDependency != "" && showDependency.equals("Show Dependencies")) {
            String repository = request.getParameter("cBoxRepository");
            Statement st1 = con.createStatement();
            String query1 = "";
            if (repository.equalsIgnoreCase("All")) {
                query1 = "SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName " +
                        "FROM (DependencyManager.RepositoryTable R " +
                        "join DependencyManager.RepositoryDependencyTable RD " +
                        "on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D " +
                        "on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version " +
                        "join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID";
                if (thirdParty != "") {
                    query1 = "SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName " +
                            "FROM (DependencyManager.RepositoryTable R " +
                            "" +
                            "join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) " +
                            "join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId " +
                            "and RD.GroupId = D.GroupId and RD.Version = D.Version " +
                            "join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID " +
                            "WHERE D.SourceRepoId='" + repoId + "'";
                }
            } else {
                query1 = "SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName " +
                        "FROM (DependencyManager.RepositoryTable R " +
                        "join DependencyManager.RepositoryDependencyTable RD " +
                        "on R.RepoID = RD.DependRepoId) join DependencyManager.DependencyTable D " +
                        "on RD.ArtifactID = D.ArtifactId and RD.GroupId = D.GroupId and RD.Version = D.Version " +
                        "join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID " +
                        "WHERE R.RepoName = '" + repository + "'";
                if (thirdParty != "") {
                    query1 = "SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion,RR.RepoName, R.RepoName " +
                            "FROM (DependencyManager.RepositoryTable R " +
                            "join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) " +
                            "join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId " +
                            "and RD.GroupId = D.GroupId and RD.Version = D.Version" +
                            " join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID " +
                            "WHERE R.RepoName = '" + repository + "' and D.SourceRepoId='" + repoId + "'";
                }
            }

            if (snapshot != "") {
                query1 = query1.concat(" AND D.Version LIKE '%snapshot%'");
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
        } else if (showArtifact != "" && showArtifact.equals("Show Artifacts")) {


            String repository = request.getParameter("cBoxRepository");
            Statement st1 = con.createStatement();
            String query1 = "";
            if (repository.equalsIgnoreCase("All")) {
                query1 = "select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName,'' " +
                        "from DependencyTable D, RepositoryTable R where D.SourceRepoId=R.RepoId ";
            } else {
                query1 = "select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName,'' " +
                        "from DependencyTable D, RepositoryTable R where R.RepoName='"
                        + repository
                        + "' and R.RepoId=D.SourceRepoId";
            }

            if (snapshot != "") {
                query1 = query1.concat(" AND D.Version LIKE '%snapshot%'");
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

        } else if (showUsage != null && showUsage.equals("Show Usage")) {

%>
<script>
    displayUsgaeButton();
</script>
<%

            String groupId1 = request.getParameter("cBoxGroup");
            String artifactId1 = request.getParameter("cBoxArtifact");
            String version1 = request.getParameter("cBoxVersion");
            Statement st1 = con.createStatement();

            String query1 = "SELECt RD.GroupId,RD.ArtifactId,RD.Version, D.LatestVersion, RR.RepoName, R.RepoName, R.RepoID " +
                    "FROM (DependencyManager.RepositoryTable R " +
                    "join DependencyManager.RepositoryDependencyTable RD on R.RepoID = RD.DependRepoId) " +
                    "join DependencyManager.DependencyTable D on RD.ArtifactID = D.ArtifactId " +
                    "and RD.GroupId = D.GroupId and RD.Version = D.Version " +
                    "join DependencyManager.RepositoryTable RR on D.SourceRepoId = RR.RepoID" +
                    " where  RD.DependRepoId=R.RepoId " +
                    "and RD.GroupId=D.GroupId and RD.ArtifactId=D.ArtifactID and RD.Version=D.Version";


            if (!groupId1.equals("All Groups")){
                query1 += " and RD.GroupId='" + groupId1 + "'";
            }
            if (!artifactId1.equals("All Artifacts")){
                query1 +=  " and RD.ArtifactId='" + artifactId1 + "' ";
            }
            if (!version1.equals("All Versions")){
                query1 +=  " and RD.Version='" + version1 + "'";
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
            out.println("<th>Show Dependencies </th>");
            out.println("<th>Show Artifacts</th>");
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
                out.println("<td><a href='' class='myButton' onclick='showDependencies(\""+ rs1.getString(6)+"\",\"dependencies\")' >Show Dependencies</a></td>");
                out.println("<td><a href='' class='myButton' onclick='showDependencies(\""+ rs1.getString(6)+"\",\"artifacts\")' >Show Artifacts</a></td>");
                out.println("</tr>");
            }

            out.println("</table>");
        }
    }
%>



</body>
</html>