#!/bin/bash
#
trap "kill 0" SIGINT
export DATOMIC=/opt/sw/datomic-pro-0.9.5350
WORKDIR=`pwd`
CONFIG=$WORKDIR/cassandra-txtor.properties
DATADIR=/var/lib/datomic
USER=`whoami`
#remove read access to distr data dir
sudo chmod 555 $DATOMIC/data
#create configured data dir
sudo mkdir -p $DATADIR
sudo chown $USER $DATADIR
($DATOMIC/bin/transactor $CONFIG) &
TXPRC=$!
echo  "Datomic is running (PID $TXPRC)"
java -cp "$DATOMIC/lib/*:$DATOMIC/datomic-pro-0.9.5350.jar" clojure.main $WORKDIR/trigger-write.clj

