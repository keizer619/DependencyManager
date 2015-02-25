Dependency Manager

Depedency Manager is consist of 2 modules namely resolver and dashboard. Resolver will resolve depedencies of given maven repository and source codes using Apache Aether. All these dependencies will be stored in a MySQL database. On the other dashboard is a web application which presents the Depedency information stored in MySQL database. 

You have set following constants according to your configurations. Before execute the resolver you may need to update all repositories and do a maven build on all the repositories.

M2_PATH = Maven M2 directory of your machine
READ_FILE_PATH = Temporary txt file path
ROOT_PATH  = git repository directories root path

