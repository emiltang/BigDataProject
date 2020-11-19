#!/usr/bin/env bash

CURRENT_DIR=`dirname "$0"`

$HOME/flume/bin/flume-ng agent --conf $HOME/flume/conf/ -f $CURRENT_DIR/flume-kafka-to-hdfs.conf --name kafka-agent -Dflume.root.logger=INFO,console
