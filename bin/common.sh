#!/bin/false


## This must be sourced within other BASH files



###-------------------
if [ -z ${SCRIPTFULLFLDRPATH+x} ]; then  ### if [ -z "$var" ]    <-- does NOT distinguish between 'unset var' & var=""
	>&2 echo "The topmost script that 'includes/sources' common.sh and 'cfngen-common.sh' must define SCRIPTFULLFLDRPATH ${SCRIPTFULLFLDRPATH}"
	exit 9
fi

ERRMSG1="Coding-ERROR: BEFORE sourcing common.sh, you MUST set variables like AWSRegion (=${AWSRegion}) and ORGASUXHOME (=${ORGASUXHOME}).   The best way is to run using 'asux.js' and to _LOAD_ job-Master.properties"
if [ -z ${ORGASUXHOME+x} ]; then  ### if [ -z "$var" ]    <-- does NOT distinguish between 'unset var' & var=""
	>&2 echo $ERRMSG1
	exit 9
fi
if [ -z ${AWSRegion+x} ]; then  ### if [ -z "$var" ]    <-- does NOT distinguish between 'unset var' & var=""
	>&2 echo $ERRMSG1
	exit 10
fi

###-------------------------------------
#___ ORGASUXHOME=/mnt/development/src/org.ASUX
if [ -z ${AWSHOME+x} ]; then
	export AWSHOME=${ORGASUXHOME}/AWS
fi
if [ -z ${AWSCFNHOME+x} ]; then 
	export AWSCFNHOME=${AWSHOME}/CFN
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

export PATH=${PATH}:${ORGASUXHOME}

if [ ! -e "${ORGASUXHOME}/asux.js" ]; then
	>&2 echo "Please edit this file $0 to set the correct value of 'ORGASUXHOME'"
	>&2 echo "	This command will fail until correction is made"
	exit 5
fi

###-------------------
### Check to see if org.ASUX project (Specifically, the program 'node' and asux.js) exists - and.. is in the path.

command -v asux.js > /dev/null
if [ $? -ne 0 ]; then
	>&2 echo ' '
	>&2 echo "Either Node.JS (node) is NOT installed or .. org.ASUX git-project's folder is NOT in the path."
	>&2 echo "ATTENTION !!! Unfortunately, you have to do fix this MANUALLY."
	>&2 echo "	This command will fail until correction is made"
	sleep 2
	exit 6
else
	if [ "${VERBOSE}" == "1" ]; then
		echo "[y] verified that Node.JS (node) is installed"
		echo "[y] verified that ${ORGASUXHOME}/asux.js can be executed"
	fi
fi

if [ ! -e ${AWSPROFILE} ]; then
        echo "AWS login credentials missing in a file ${AWSPROFILE}"
        exit 7
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

grep ${AWSRegion} ${AWSCFNHOME}/config/AWSRegionsLocations.properties | \sed -e 's/.*=/AWSLocation=/' > /tmp/$$
. /tmp/$$
\rm /tmp/$$

#_____ read -p "ATTENTION! Need manual help to convert AWS-REGION ${AWSRegion} into a Location.  Enter Location:>" AWSLocation
if [ "${VERBOSE}" == "1" ]; then
	echo "AWSRegion=${AWSRegion}"
	read -p "The official Location of AWS-REGION ${AWSRegion} is ${AWSLocation}.  Correct?" USERRESPONSE
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#EoF
