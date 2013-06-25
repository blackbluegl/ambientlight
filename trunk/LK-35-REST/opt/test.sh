#!/bin/bash
counter=1
acounter=255
factorial=1
while [ $counter -le 256000 ]
do
counter=$(( $counter + 1 ))
acounter=$(( $acounter - 1 ))
wget -r -Nc -mk "http://localhost:8899/rest/LK35ColorHandler/color/rgb?zone=0&r=$counter&g=$acounter&b=0"
done
