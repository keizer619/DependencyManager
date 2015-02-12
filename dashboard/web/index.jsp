<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Dependency Check</title>
</head>
<body>
		<FORM name="formIndex" action="DisplayData.jsp" METHOD="POST">
<select name="cBoxDisplay" onchange="undisableTxt(this.value)">
            <option value="AllDep">All Dependencies</option>
            <!-- <option value="SpecDep">Specified Dependency</option>  -->
            <option value="AllRepo">All Repositories</option>
            <!-- <option value="SpecRepo">Specified Repository</option> -->
            <option value="Dep_Repo">Dependencies of a specified Repository</option>
            <option value="Repo_Dep">Repositories of a specified Dependency</option>
        </select>
<br>
<br>
		Group Id: <INPUT TYPE="TEXT" NAME="txtGroupId" ID="txtGroupId" disabled = "true">
		Artifact Id: <INPUT TYPE="TEXT" NAME="txtArtifactId" ID="txtArtifactId" disabled = "true">
		Version: <INPUT TYPE="TEXT" NAME="txtVersion" ID="txtVersion" disabled = "true">
		<!-- <INPUT TYPE="checkbox" NAME="checkSnapshots"> SNAPSHOT Versions	 -->	
		Repository: <INPUT TYPE="TEXT" NAME="txtRepository" ID="txtRepository" disabled = "true">
		<input type="submit" value="Submit" />		
		</FORM>
		
<script>
function undisableTxt(selectedValue) {
	if(selectedValue=="Dep_Repo"){
    document.getElementById("txtRepository").disabled = false;
    document.getElementById("txtGroupId").disabled = true;
	document.getElementById("txtArtifactId").disabled = true;
	document.getElementById("txtVersion").disabled = true;
	}else if(selectedValue=="Repo_Dep"){
		document.getElementById("txtGroupId").disabled = false;
		document.getElementById("txtArtifactId").disabled = false;
		document.getElementById("txtVersion").disabled = false;
		document.getElementById("txtRepository").disabled = true;
	}else{
		document.getElementById("txtRepository").disabled = true;
		document.getElementById("txtGroupId").disabled = true;
		document.getElementById("txtArtifactId").disabled = true;
		document.getElementById("txtVersion").disabled = true;
	}
  }

</script>
		
</body>
</html>