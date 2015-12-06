#########################################################################
# File Name: clean.sh
# Author: ma6174
# mail: ma6174@163.com
# Created Time: Fri 20 Nov 2015 01:03:13 AM UTC
#########################################################################
#!/bin/bash
rm out.log
rm features.log
hadoop fs -rm -r user/zx/output/*
hadoop fs -rm -r user/zx/input/*
