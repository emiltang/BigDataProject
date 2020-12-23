Report: https://www.overleaf.com/8539624991rxbmgcnrfcps

# Configs and Scripts

Put all code and project specific config file here and use symlinks if strictly nesecary (ln -s) 

Use `git -c "user.name=Your Name" -c "user.email=Your email" commit` when comitting code.

## Twitter Producer

Start in background with:

```
./twitter_producer.py > /dev/null 2> twitter_producer.log &
disown <pid> # pid here
```

## Flume

Apperantly this exception is harmless

```
ARN - org.apache.hadoop.hdfs.DataStreamer.closeResponder(DataStreamer.java:988)] Caught exception
java.lang.InterruptedException
        at java.lang.Object.wait(Native Method)
        at java.lang.Thread.join(Thread.java:1252)
        at java.lang.Thread.join(Thread.java:1326)
        at org.apache.hadoop.hdfs.DataStreamer.closeResponder(DataStreamer.java:986)
        at org.apache.hadoop.hdfs.DataStreamer.endBlock(DataStreamer.java:640)
        at org.apache.hadoop.hdfs.DataStreamer.run(DataStreamer.java:810)
```
https://stackoverflow.com/questions/39351690/got-interruptedexception-while-executing-word-count-mapreduce-job

https://issues.apache.org/jira/browse/HDFS-10429

## SSH Port Forwarding

To acces the webserver add:

`ssh -L 5000:127.0.0.1:5000 group7-node0`

or add:

`LocalForward 127.0.0.1:5000 bddst-group7-node0.uvm.sdu.dk:5000`

to:

`~/.ssh/config`

## Spark streaming

start job with

```
spark-submit \
        --packages org.apache.spark:spark-sql-kafka-0-10_2.12:3.0.1 \
        --class Cleaner \
        --master yarn \
        --deploy-mode cluster \
        --driver-memory 4G \
        --executor-memory  \
        --executor-cores 1 \
        target/scala-2.12/cleaner_2.12-0.1.jar
```
