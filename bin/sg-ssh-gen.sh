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

.   ${SCRIPTFULLFLDRPATH}/cfngen-common-pre.sh

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### Make sure 'cd' command below comes __AFTER__ sourcing 'common.sh' (above)

CFNContext=sg-ssh

.   ${SCRIPTFULLFLDRPATH}/cfngen-common-post.sh

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

CFNfile=/tmp/${CFNContext}

${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
echo "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-${JobSetName}-SG-SSH${ITEMNUMBER}  --region ${AWSRegion} --profile \${AWSprofile}   \
		--parameters ParameterKey=MyVPC,ParameterValue=${VPCID} \
		--template-body file://${CFNfile} " > ${CFNfile}.sh
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! The STACK-NAME expression is VERY different vs. VPC and SG/EC2.. .. 
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! Make sure the STACK-NAME expression _OVERALL_ matching-up with 'MyStackNamePrefix' (see defn in cfngen-common.sh)

###------------------------------

#EoF
