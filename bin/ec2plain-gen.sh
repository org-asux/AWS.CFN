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

${SCRIPTFULLFLDRPATH}/AWS-AMI-list-by-Region.sh ${AWSRegion} | grep -v '.-.*=' > /tmp/$$  ### Weed out 'ap-northeast-1LinuxAMI=...' and keep only 'TokyoLimuxAMI='
if [ -e /tmp/$$ ] && [ -s /tmp/$$ ]; then
	. /tmp/$$
	\rm /tmp/$$
else
	>&2 echo "ERROR!!!!!!! No AMIs found using: ${SCRIPTFULLFLDRPATH}/AWS-AMI-list-by-Region.sh ${AWSRegion} | grep -v '.-.*='"
	exit 19
fi

if [ "${VERBOSE}" == "1" ]; then	echo AWSAMIID=${AWSLocation}LinuxAMI; fi

###------------------------------

CFNfile=/tmp/ec2

${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-MyEC2plain-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
PARAMS=" ParameterKey=MyVPC,ParameterValue==${VPCID} ParameterKey=MyPublicSubnet1,ParameterValue==${DefaultPublicSubnet1} "
PARAMS="${PARAMS} ParameterKey=MySSHSecurityGroup,ParameterValue==${MySSHSecurityGroup}  ParameterKey=MyIamInstanceProfiles,ParameterValue==${MyIamInstanceProfiles} "
PARAMS="${PARAMS} ParameterKey=AWSAMIID,ParameterValue==${AWSAMIID} ParameterKey=MySSHKeyName,ParameterValue==${MySSHKeyName} "
echo "aws cloudformation create-stack --stack-name --parameters ${PARAMS} " > ${CFNfile}.sh

###------------------------------

#EoF
