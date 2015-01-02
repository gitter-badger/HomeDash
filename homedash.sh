#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

FULLDIR=$DIR"/target/universal/stage"

PID=$FULLDIR"/RUNNING_PID"




function stop {
    if [ -e $PID ]
    then
        echo "Stopping Home Dash"
        kill -9 $(cat $FULLDIR/RUNNING_PID)

        rm $FULLDIR/RUNNING_PID
    else
        echo "Home dash not running or PID file has already been deleted"
    fi

}

function start {
    if [ -e $PID ]
    then
        echo "Home Dash is already running. If it is not, delete the file $PID"
    else
        echo "Compiling Home Dash...."

        $DIR/activator clean stage

        chmod +x $FULLDIR/bin/homedash

        echo Starting Home Dash
        $FULLDIR/bin/homedash -Dhttp.port=9000 -DapplyEvolutions.default=true  -DapplyDownEvolutions.default=true -J-Xms64M -J-Xmx128M

    fi
}

function restart {
    stop
    start
}


if [ $# -eq 0 ]
then
    echo "Argument needed start|restart|stop"
elif [ $1 == "start" ]
then
    start
elif [ $1 == "restart" ]
then
    restart
elif [ $1 == "stop" ]
then
    stop
else
    echo "Argument needed start|restart|stop"
fi





