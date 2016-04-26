#!/bin/bash
#
DATOMICVERSION=0.9.5359
WORKDIR=`pwd`
#Set this to the location of your Datomic distribution
export DATOMIC=/opt/sw/datomic-pro-$DATOMICVERSION

#CONFIG=$WORKDIR/cassandra-txtor.properties
CONFIG=$WORKDIR/file-txtor.properties
#this setting needs to match what you configure in the transactor props
DATADIR=/var/lib/datomic

#this user will also run the transactor 
USER=`whoami`

trap "kill 0" SIGINT
#create distr data if it does not exist e.g. after fresh install 
sudo mkdir $DATOMIC/data
#remove read access to distr data dir
sudo chmod -R 555 $DATOMIC/data
#create configured data dir
sudo mkdir -p $DATADIR
sudo chown $USER $DATADIR
($DATOMIC/bin/transactor $CONFIG) &
TXPRC=$!
echo  "Datomic is running (PID $TXPRC)"
java -cp "$DATOMIC/lib/*:$DATOMIC/datomic-pro-$DATOMICVERSION.jar" clojure.main $WORKDIR/trigger-write.clj

