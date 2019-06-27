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

CFNContext=subnets

.   ${SCRIPTFULLFLDRPATH}/cfngen-common-post.sh

if [ "${VERBOSE}" == "1" ]; then echo "In subnets-gen.sh 3rd command line-arg = $2"; fi

if [ $# -ge 2 ] && [[ "$2" == "public"  || "$2" == "private" ]]; then
	PublicOrPrivate=$2
else
	echo "Usage: ${CmdPathGuess} $1 $2 <public|private>"
	exit 4
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

CFNfile=/tmp/${CFNContext}

${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-${PublicOrPrivate}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
echo "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-subnets-${PublicOrPrivate}-${JobSetName}  --region ${AWSRegion} --profile \${AWSprofile} --template-body file://${CFNfile} " > ${CFNfile}.sh
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! The STACK-NAME for VPC already has {ENVIRONMENT} in it.
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! DO ___NOT___ ADD {JobSetName} to Stack-name
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! The STACK-NAME expression is VERY different between VPC -vs- SG / EC2.. .. 
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! Make sure the STACK-NAME expression _OVERALL_ matching-up with 'MyStackNamePrefix' (see defn in cfngen-common.sh)

###------------------------------

#EoF
