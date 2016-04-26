Reproducing the data-dir issue
==============================

Preconditions
=============

1. Working Java setup 
2. Datomic Pro > 0.9.5390
3. transactor config file: 
   1. You need to set up your license plus the data-dir setting.
   2. The data-dir setting needs to match the DATADIR in `run.sh`
4. Write permissions on the DATADIR    
5. bash


How to run
==========

1. Start your storage (skip when using dev storage)
2. ```./run.sh``` (make it executable first)

Platform Specifics/Tested with
===========
* Mac OS X/datomic pro 0.9.5390/datomic pro 0.9.5359/cassandra + dev storage
* You might need to adjust the hardcoded 2 sec wait for the transactor to boot up in trigger-write.clj



What it does
============
1. removes write permissions from the datomic distribution data dir
2. startes a transactor with a custom data-dir configured via property file
3. creates a new database
4. installs two string attributes without index
5. adds data (~ 100 facts per attribute are enough)
6. alters the schema for each attribute to have an index
7. transactor ignores the data-dir setting tries to write to distribution directory which fails (see step 1) and exits.





