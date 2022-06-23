#! /bin/bash
DBNAME=$"dguti026_DB"
PORT=$"9998"
USER=$"dguti026"

# Example: source ./run.sh flightDB 5432 user
java -cp lib/*:bin/ MechanicShop $DBNAME $PORT $USER
