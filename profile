# /etc/profile: system-wide .profile file for the Bourne shell (sh(1))
# and Bourne compatible shells (bash(1), ksh(1), ash(1), ...).

alias ..='cd ..'
alias ...='cd ../..'

if [ "$PS1" ]; then
  if [ "$BASH" ] && [ "$BASH" != "/bin/sh" ]; then
    # The file bash.bashrc already sets the default PS1.
    # PS1='\h:\w\$ '
    if [ -f /etc/bash.bashrc ]; then
      . /etc/bash.bashrc
    fi
  else
    if [ "`id -u`" -eq 0 ]; then
      PS1='# '
    else
      PS1='$ '
    fi
  fi
fi

# The default umask is now handled by pam_umask.
# See pam_umask(8) and /etc/login.defs.

if [ -d /etc/profile.d ]; then
  for i in /etc/profile.d/*.sh; do
    if [ -r $i ]; then
      . $i
    fi
  done
  unset i
fi

# Set java envrionment
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export JRE_HOME=/usr/lib/jvm/java-7openjdk-amd64/jre
export CLASSPATH=.:$CLASSPATH:JAVA_HOME/lib:$JRE_HOME/lib
export PATH=$PATH:JAVA_HOME/bin:$JRE_HOME/bin

# Set hadoop path
export HADOOP_HOME=/usr/hadoop
export PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH
export CLASSPATH=$CLASSPATH:$HADOOP_HOME/share/hadoop/common/hadoop-common-2.7.1.jar:$HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-client-core-2.7.1.jar:$HADOOP_HOME/share/common/lib/commons-cli-1.2.jar

# Set Gradle bin 
export GRADLE_HOME=/home/gathors/gradle
export PATH=$PATH:$GRADLE_HOME/bin

# Set HIPI CLASSPATH
export HIPI_HOME=/home/gathors/hipi
export CLASSPATH=$CLASSPATH:$HIPI_HOME/core/build/libs/hipi-2.1.0.jar

# Set maven bin
export PATH=/usr/local/maven/bin:$PATH

# Set ant environment
export ANT_HOME=~/ant-1.9.6
export PATH=$ANT_HOME/bin:$PATH
export CLASSPATH=$ANT_HOME/lib:$CLASSPATH

FeatureExtractionProj=/home/gathors/proj/v-opencv/FeatureExtraction
#FeatureExtractionProj=/home/gathors/proj/v-javacv/FeatureExtraction

export LD_LIBRARY_PATH=/usr/local/lib:$FeatureExtractionProj/libs/:$LD_LIBRARY_PATH

export CLASSPATH=$FeatureExtractionProj/libs/:$CLASSPATH
export PATH=$FeatureExtractionProj/libs:$PATH

