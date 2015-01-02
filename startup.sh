#!/bin/sh

DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

chmod +x $DIR/activator

$DIR/activator clean stage

FULLDIR=$DIR"/target/universal/stage"

echo $FULLDIR

kill -9 $(cat $FULLDIR/RUNNING_PID)

rm $FULLDIR/RUNNING_PID


chmod +x $FULLDIR/bin/homedash



nohup $FULLDIR/bin/homedash -Dhttp.port=9000 -DapplyEvolutions.default=true  -DapplyDownEvolutions.default=true -J-Xms64M -J-Xmx128M >/dev/null 2>&1 &
