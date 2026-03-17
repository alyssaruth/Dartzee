#!/bin/bash
set -eu

touch dartzee.log

desiredLog=$1

nohup java -jar Dartzee.jar > dartzee.log 2>&1 &
printf "Waiting for Dartzee to start."
attempt_counter=0
max_attempts=20

search_result=""
while [ -z "$search_result" ] ; do
    if [ ${attempt_counter} -eq ${max_attempts} ];then
      echo " max attempts reached"
      echo ""
      echo "$(cat dartzee.log)"
      exit 1
    fi

    printf '.'
    attempt_counter=$(($attempt_counter+1))
    search_result=$(grep $desiredLog dartzee.log || true)
    sleep 2
done

echo ""
echo "Dartzee started successfully:"
echo ""
echo "$(cat dartzee.log)"