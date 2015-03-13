#!/bin/bash

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

FULLDIR=$DIR"/target/universal/stage"

PID=$FULLDIR"/RUNNING_PID"


cd "$DIR"


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
	randomString32
    if [ -e $PID ]
    then
        echo "Home Dash is already running. If it is not, delete the file $PID"
    else
    	if [ ! -e $DIR/conf/launch.conf ]
    	then
    		createlaunchconf
    	fi
    	
    	if [ ! -e $DIR/conf/user.conf ]
    	then
    		createdbconf
    	fi
    	
    	source $DIR/conf/launch.conf
    	
        echo "Compiling Home Dash...."

        $DIR/activator clean stage

        chmod +x $FULLDIR/bin/homedash

        echo "Starting Home Dash in the background"
        echo "Port: $PORT"
        nohup $FULLDIR/bin/homedash -Dhttp.port=$PORT -DapplyEvolutions.default=true  -DapplyDownEvolutions.default=true -J-Xms$XMS -J-Xmx$XMX &> "$DIR/homedash.log" &
    fi
}

function restart {
    stop
    start
}

function randomString32 {

	index=0

	str=""

	for i in {a..z}; do arr[index]=$i; index=`expr ${index} + 1`; done

	for i in {A..Z}; do arr[index]=$i; index=`expr ${index} + 1`; done

	for i in {0..9}; do arr[index]=$i; index=`expr ${index} + 1`; done

	for i in {1..64}; do str="$str${arr[$RANDOM%$index]}"; done

	APP_SECRET=$str

}

function createdbconf {
	CONF_FILE=$DIR/conf/user.conf
	
	echo "Setting up the database connection"
	
	read -p "Host:" DB
	read -p "Port [3306]:" DBPORT
	read -p "Database:" DBDB

	if [ "$DBPORT" = "" ]
	then
    	DBPORT=3306
	fi
	
	read -p "Username:" DBUSER
	read -s -p "Password:" DBPASS

	randomString32

	touch  $CONF_FILE
	echo "application.secret=\"$APP_SECRET\"" >> $CONF_FILE
	echo "db.default.driver=com.mysql.jdbc.Driver" >> $CONF_FILE
	echo "db.default.url=\"jdbc:mysql://$DB:$DBPORT/$DBDB\"" >> $CONF_FILE
	echo "db.default.user=$DBUSER" >> $CONF_FILE
	echo "db.default.password=\"$DBPASS\"" >> $CONF_FILE
	
	echo "Config written to $CONF_FILE"
}


function createlaunchconf {

	echo "First time launching HomeDash, let's set up few things first"
	
	read -p "Enter port to run HomeDash [9000]" PORT
	if [ "$PORT" = "" ]
	then
    	PORT=9000
	fi
	
	
	read -p "Minimum RAM used by the JVM Xms HomeDash [64M]" XMS
	if [ "$XMS" = "" ]
	then
    	XMS="64M"
	fi
	
	read -p "Maximum RAM used by the JVM Xmx HomeDash [128M]" XMX
	if [ "$XMX" = "" ]
	then
    	XMX="128M"
	fi
	
	touch  $DIR/conf/launch.conf
	echo "PORT=$PORT" >>  $DIR/conf/launch.conf
	echo "XMS=$XMS" >>  $DIR/conf/launch.conf
	echo "XMX=$XMX" >>  $DIR/conf/launch.conf

	
	echo "Config written in $DIR/conf/launch.conf, you can change the values anytime. Requires HomeDash restart."
	
}


if [ $# -eq 0 ]
then
    echo "Argument needed start|restart|stop|update"
elif [ $1 == "start" ]
then
    start
elif [ $1 == "restart" ]
then
    restart
elif [ $1 == "stop" ]
then
    stop
elif [ $1 == "update" ]
then
    git pull
    restart
else
    echo "Argument needed start|restart|stop|update"
fi