EXCEL_PATH="/Users/tharik/Desktop/git/Report.csv"
OUTPUT_FILE="/Users/tharik/Desktop/git/graph.json"
ARROW="->"
ENDLINE=";"
QOUTES='"'


echo "digraph {" >>  "$OUTPUT_FILE"

cat $EXCEL_PATH | while read line
do
	let COUNT=COUNT+1

	   PROJECT=$(echo $line | cut -d',' -f1)
	   DEPENDENCY=$(echo $line | cut -d',' -f2)

	   echo $QOUTES$PROJECT$QOUTES$ARROW$QOUTES$DEPENDENCY$QOUTES$ENDLINE >> "$OUTPUT_FILE"
	   
done 

echo "}" >> "$OUTPUT_FILE"