OUTPUT_PATH="/Users/tharik/Desktop/git/rep"
EXCEL_PATH="/Users/tharik/Desktop/git/WSO2BuildStablization2015Plan.csv"
KEY_WORD="github.com"

cat $EXCEL_PATH | while read line
do
	let COUNT=COUNT+1
	#Get repository URL from CSV line
	REPO_URL=$(echo $line | cut -d, -f2)
	#Checks if it is a github url
	if [[ $REPO_URL == *$KEY_WORD* ]]
	then
	   #Get repository name from the URL
	   REPO_NAME=$(echo $REPO_URL | cut -d'/' -f5)

       cd $OUTPUT_PATH/$REPO_NAME

	   #Make a directory for repository and clone
	   git pull $REPO_URL
	fi
done 