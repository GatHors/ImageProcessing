#########################################################################
# File Name: run.sh
# Author: ma6174
# mail: ma6174@163.com
# Created Time: Fri 20 Nov 2015 01:02:32 AM UTC
#########################################################################
#!/bin/bash
./clean.sh
gradle build
hadoop jar build/libs/FeatureExtraction.jar input.hib > out.log
#cat out.log
