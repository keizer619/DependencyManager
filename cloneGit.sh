OUTPUT_PATH="/Users/tharik/Desktop/git/rep"
EXCEL_PATH="/Users/tharik/Desktop/git/WSO2BuildStablization2015Plan.csv"

cat $EXCEL_PATH | while read line
do
	let COUNT=COUNT+1
	REPO_URL=$(echo $line | cut -d, -f2)

	if [[ $REPO_URL == *"github.com"* ]]
	then
	   
	   REPO_NAME=$(echo $REPO_URL | cut -d'/' -f5)
	   mkdir $OUTPUT_PATH/$REPO_NAME
	   git clone $REPO_URL $OUTPUT_PATH/$REPO_NAME

	   
	fi


done 



#git clone https://github.com/keizer619/practical01.git $OUTPUT_PATH