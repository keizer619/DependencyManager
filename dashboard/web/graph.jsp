<%@page import="java.sql.*"%>
<%@page import="java.sql.Connection"%>
<html>
<head>

    <meta charset="utf-8">
    <title>Dependency Graph</title>

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
</head>


<body onLoad="tryDraw();">


<%

    String graphJson = "";

    if (request.getParameter("graphJson") != null) {
        graphJson=request.getParameter("graphJson");
    }


    Class.forName("com.mysql.jdbc.Driver");
    Connection con = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/DependencyManager", "root",
            "Root@wso2");
    Statement st = con.createStatement();
    String json = "digraph {";


    String query="SELECT DISTINCT r.RepoName As DependRepo, rr.RepoName AS SourceRepo FROM (DependencyManager.RepositoryTable r join DependencyManager.RepositoryDependencyTable rd on r.RepoID = rd.DependRepoId) join DependencyManager.DependencyTable d on rd.ArtifactID = d.ArtifactId and rd.GroupId = d.GroupId and rd.Version = d.Version join DependencyManager.RepositoryTable rr on d.SourceRepoId = rr.RepoID where r.RepoName != rr.RepoName";
    ResultSet rs = st.executeQuery(query);

    while (rs.next()) {

        json += '"' +rs.getString(1) +'"' + "->" + '"'
                + rs.getString(2) + '"' + ";";

    }

    json += "}";
%>




    <form>
      <textarea id="inputGraph" rows="5" style="display: block" onKeyUp="tryDraw();"/></textarea>
    <a id="graphLink">Link for this graph</a>
       <script type="text/javascript">




        document.getElementById("inputGraph").value = '<%out.print(json);%>';
        document.getElementById("inputGraph").style.display = "none";
        document.getElementById("graphLink").style.display = "none";
      </script>


    </form>

    <svg width=100% height=600>
      <g/>
    </svg>

    <script type="text/javascript">
    // Input related code goes here

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
    </script>

</body>

</html>
