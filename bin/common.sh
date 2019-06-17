#!/bin/false

## This must be sourced within other BASH files

###------------------------------
### The following line did NOT work on Windows
# CmdPathGuess="${BASH_SOURCE[0]}"

CmdPathGuess="$0"
# echo $CmdPathGuess
SCRIPTFLDR_RELATIVE="$(dirname "$CmdPathGuess")"
SCRIPTFULLFLDRPATH="$( cd "$(dirname "$0")" ; pwd -P )"
if [ "${VERBOSE}" == "1" ]; then echo SCRIPTFULLFLDRPATH=${SCRIPTFULLFLDRPATH}; fi
#___	if [ "${SCRIPTFLDR_RELATIVE}" != "." ]; then
#___	fi

###-------------------
ORGASUXFLDR=/mnt/development/src/org.ASUX
AWSCFNFLDR=${ORGASUXFLDR}/AWS/CFN

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

export PATH=${PATH}:${ORGASUXFLDR}

if [ ! -e "${ORGASUXFLDR}/asux.js" ]; then
	>&2 echo "Please edit this file $0 to set the correct value of 'ORGASUXFLDR'"
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
	if [ "${VERBOSE}" == "1" ]; then echo "[y] verified that Node.JS (node) is installed"; fi
	if [ "${VERBOSE}" == "1" ]; then echo "[y] verified that ${ORGASUXFLDR}/asux.js can be executed"; fi
fi

if [ ! -e ${AWSPROFILE} ]; then
        echo "AWS login credentials missing in a file ${AWSPROFILE}"
        exit 7
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

grep ${AWSRegion} ${AWSCFNFLDR}/config/AWSRegionsLocations.properties | \sed -e 's/.*=/AWSLocation=/' > /tmp/$$
. /tmp/$$
\rm /tmp/$$

#_____ read -p "ATTENTION! Need manual help to convert AWS-REGION ${AWSRegion} into a Location.  Enter Location:>" AWSLocation
if [ "${VERBOSE}" == "1" ]; then read -p "The official Location of AWS-REGION ${AWSRegion} is ${AWSLocation}.  Correct?" USERRESPONSE; fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#EoF
