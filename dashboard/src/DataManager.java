import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataManager {

    Connection con;
    Statement st;

    public DataManager()
    {
        try {
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/DependencyManager", "root",
                    "Root@wso2");
            st = con.createStatement();
        }
        catch (Exception ex){

        }
    }

    public ResultSet getAllDependencies(){

        String query = "Select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName from DependencyTable D,RepositoryTable R where D.SourceRepoId=R.RepoId";
        ResultSet rs = null;

        try {
            rs = st.executeQuery(query);
        }
        catch (Exception ex){

        }

        return rs;
    }

    public ResultSet getAllDependenciesByRepository(String repositoryName){

        String query="select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName from DependencyTable D,RepositoryTable R, RepositoryDependencyTable DR where D.GroupId=DR.GroupId and D.ArtifactId = DR.ArtifactId and D.Version = DR.Version  and R.RepoName='"+repositoryName+"' ";
        ResultSet rs = null;

        try {
            rs = st.executeQuery(query);
        }
        catch (Exception ex){

        }

        return rs;
    }

    public ResultSet getAllArtifactsByRepository(String repositoryName){

        String query="select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName from DependencyTable D,RepositoryTable R where D.SourceRepoId=R.RepoId and R.RepoName='"+repositoryName+"' ";
        ResultSet rs = null;

        try {
            rs = st.executeQuery(query);
        }
        catch (Exception ex){

        }

        return rs;
    }

    public ResultSet getUsage(String groupId, String artifactId, String version){

        String query="select D.GroupId,D.ArtifactId,D.Version,D.LatestVersion,R.RepoName from DependencyTable D,RepositoryTable R where D.SourceRepoId=R.RepoId and D.GroupId='"+groupId+"' and D.ArtifactId ='"+artifactId+"' and D.Version='"+version+"'";
        ResultSet rs = null;

        try {
            rs = st.executeQuery(query);
        }
        catch (Exception ex){

        }

        return rs;
    }


}
