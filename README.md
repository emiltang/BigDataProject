# Configs and scripts

Put all code and project specific config file here and use symlinks if strictly nesecary (ln -s) 

Use `git -c "user.name=Your Name" -c "user.email=Your email" commit` when comitting code.

# Flume

Apperantly this expection is harmless

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