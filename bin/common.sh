#!/bin/false


## This must be sourced within other BASH files




### !!!!!!!!!! NOTE:  $0 is _NOT_ === this-file /aka/  {AWSSDKHOME}/bin/common.sh)"

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### Attention !!!!!!!!! SCRIPTFULLFLDRPATH must be set .. before sourcing/including '{SCRIPTFULLFLDRPATH}/../../bin.common.sh'
if [ -z ${SCRIPTFULLFLDRPATH+x} ]; then  ### if [ -z "$var" ]    <-- does NOT distinguish between 'unset var' & var=""
	>&2 echo "The topmost script that 'includes (a.k.a.) sources' common.sh must define SCRIPTFULLFLDRPATH ${SCRIPTFULLFLDRPATH}"
	kill -SIGUSR1 `ps --pid $$ -oppid=`
	exit $1
fi

ERRMSG1="Coding-ERROR: BEFORE sourcing common.sh, you MUST set variables like AWSRegion (=${AWSRegion}) and ORGASUXHOME (=${ORGASUXHOME}).   The best way is to run using 'asux.js aws .. .. ..' commands."
if [ -z ${ORGASUXHOME+x} ]; then  ### if [ -z "$var" ]    <-- does NOT distinguish between 'unset var' & var=""
	#__ echo SCRIPTFULLFLDRPATH=${SCRIPTFULLFLDRPATH}
	#__ ls -la ${SCRIPTFULLFLDRPATH}/../../..
	if [ -e ${SCRIPTFULLFLDRPATH}/../../../asux.js ] && [ -e ${SCRIPTFULLFLDRPATH}/../../../bin/common.sh ]; then
		### !!!!!!!! Unlike  {AWSSDKHOME}/bin/common.sh .. we're taking an EASIER route, but leveraging that file!
		#___ export ORGASUXHOME=${SCRIPTFULLFLDRPATH}/../../..
		.  ${SCRIPTFULLFLDRPATH}/../../AWS-SDK/bin/common.sh  ### <<--- This should be sourcing {ORGASUXHOME}/bin/common.sh, which will automatically set value of ORGASUXHOME
	else
		>&2 echo $ERRMSG1
		kill -SIGUSR1 `ps -p $$ -oppid=`	### On MacOS Shell, 'ps --pid' does _NOT_ work.  Instead using 'ps -p'
		exit $1
	fi
fi

###-------------------------------------

if [ -z ${AWSCFNHOME+x} ]; then 
	export AWSCFNHOME=${AWSHOME}/CFN
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#EoF
