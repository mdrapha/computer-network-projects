
# Script to compile all the project
cd ./src
javac -d ../bin/ *.java 
cd ../bin

# Script to run the project
gnome-terminal -- bash -c 'java Server; exec bash'
gnome-terminal -- bash -c 'java Client; exec bash'


