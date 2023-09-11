
# Script to compile all the project

javac -d ./bin/ ./src/*.java # Compile all the project and put the .class files in the bin folder
cd ./bin # Go to the bin folder

# Script to run the project
# Here you can change the port number and IP to test the project
# If you don't change the port number, the default port number is 45678 and the default IP is localhost
# Read the README.md file to know how to run the project
# - java Server 
# - java Server <port_number> 
# - java Client
# - java Client <IP> <port_number>

# Server (just one)
gnome-terminal -- bash -c 'java Server 45674; exec bash'

# Clients (as many as you want)
gnome-terminal -- bash -c 'java Client localhost 45674; exec bash'
gnome-terminal -- bash -c 'java Client localhost 45674; exec bash'


