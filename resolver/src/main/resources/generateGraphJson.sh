EXCEL_PATH="/Users/tharik/Desktop/git/Report.csv"
OUTPUT_FILE="/Users/tharik/Desktop/git/graph.json"
ARROW="->"
ENDLINE=";"
QOUTES='"'

echo "digraph {" >>  "$OUTPUT_FILE"

cat $EXCEL_PATH | while read line
do

if [[ $line == *"SNAPSHOT"* ]]
	then

	let COUNT=COUNT+1
	# Take first column and second column 
	PROJECT=$(echo $line | cut -d',' -f1) 
	DEPENDENCY=$(echo $line | cut -d',' -f3)
	#Concatinate depency in graph format "A"->"B";
	echo $QOUTES$PROJECT$QOUTES$ARROW$QOUTES$DEPENDENCY$QOUTES$ENDLINE >> "$OUTPUT_FILE"

	fi
	   
done 

echo "}" >> "$OUTPUT_FILE"