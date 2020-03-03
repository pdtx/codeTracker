scp $(find $(pwd) -name *-*-SNAPSHOT.jar) fdse@10.141.221.85:/home/fdse/codeTrackerForFrontEnd/

ssh fdse@10.141.221.85 "cd /home/fdse/codeTrackerForFrontEnd/ ; ./codeTrackerStop.sh ; sleep 2 ; java -jar code-tracker-0.0.1-SNAPSHOT.jar &"