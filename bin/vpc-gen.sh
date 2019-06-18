#!/bin/sh -f

### The following line did NOT work on Windows
# CmdPathGuess="${BASH_SOURCE[0]}"

CmdPathGuess="$0"
# echo $CmdPathGuess
SCRIPTFLDR_RELATIVE="$(dirname "$CmdPathGuess")"
SCRIPTFULLFLDRPATH="$( cd "$(dirname "$0")" ; pwd -P )"
if [ "${VERBOSE}" == "1" ]; then echo SCRIPTFULLFLDRPATH=${SCRIPTFULLFLDRPATH}; fi
#____	if [ "${SCRIPTFLDR_RELATIVE}" != "." ]; then
#____	fi

.   ${SCRIPTFULLFLDRPATH}/cfngen-common.sh

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### Make sure 'cd' command below comes __AFTER__ sourcing 'common.sh' (above)

if [ -d ${JobSetName} ]; then
	cd ${JobSetName}
fi
USERFLDR=`pwd`
if [ "${VERBOSE}" == "1" ]; then echo ${USERFLDR}; fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

CFNfile=/tmp/network

${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-Network-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
echo "aws cloudformation create-stack --stack-name --region ${AWSRegion} --profile {AWSPROFILE}" > ${CFNfile}.sh

###------------------------------

#EoF
