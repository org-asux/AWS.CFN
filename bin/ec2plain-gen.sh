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

CFNContext=ec2plain		### This defines which OTHER property files are loaded - - within 'cfngen-common-POST.sh'

.   ${SCRIPTFULLFLDRPATH}/cfngen-common-post.sh

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

# ${SCRIPTFULLFLDRPATH}/AWS-AMI-list-by-Region.sh ${AWSRegion} | grep -v '.-.*=' > /tmp/$$  ### Weed out 'ap-northeast-1LinuxAMI=...' and keep only 'TokyoLimuxAMI='
# if [ -e /tmp/$$ ] && [ -s /tmp/$$ ]; then
# 	COUNT=`wc -l < /tmp/$$`
# 	COUNT=`expr $COUNT + 0`  ### Convert 'string _output_ of wc-command' into integer
# 	if [ $count -eq 1 ]; then
# 		. /tmp/$$
# 		\rm /tmp/$$
# 		bAllOk = 1; ### <<--  bAllOk is ONLY defined here and NO WHERE else
# 	fi
# fi
# if [ -z ${bAllOk+x} ]; then  ### if [ -z "$bAllOk" ]    <-- does NOT distinguish between 'unset bAllOk' & bAllOk=""
# 	>&2 echo "ERROR!!!!!!! Failed to find a UNIQUE-SINGLE AMI, using: ${SCRIPTFULLFLDRPATH}/AWS-AMI-list-by-Region.sh ${AWSRegion} | grep -v '.-.*='"
# 	exit 19
# fi
# if [ "${VERBOSE}" == "1" ]; then	echo AWSAMIID=${AWSLocation}LinuxAMI; fi

AMIIDCache=${AWSCFNHOME}/config/inputs/AMZNLinux2_AMI_ID-${AWSLocation}.txt
if [ -e ${AMIIDCache} ] && [ -s ${AMIIDCache} ]; then
	. ${AMIIDCache}
else
	### !!!! NOTE !!!!! ${EC2AMI_LookupKey} is set within the job-file ${JobSetName}/jobset-ec2plain.properties <<---------
	### !! Note !! That value of the variable is determined by another script.       AWS/CFN/bin/AWS-AMI-list-by-Region.sh <AWSRegion>
	###	!! Note !! In the output of that command .. Remove the VERSION#.DATE from the middle of the AMI-TYPE, to get the EC2AMI_LookupKey
	echo "Querying AWS to figure out .. what the AMI-ID for ${EC2AMI_LookupKey} in the Location ${AWSLocation} is."
	if [ "${VERBOSE}" == "1" ]; then echo aws ssm get-parameters --names "/aws/service/ami-amazon-linux-latest/${EC2AMI_LookupKey}" --region "${AWSRegion}" --profile ${AWSprofile} --output json; fi
	aws ssm get-parameters --names "/aws/service/ami-amazon-linux-latest/${EC2AMI_LookupKey}" --region "${AWSRegion}" --profile ${AWSprofile} --output json > /tmp/o.json
	asux yaml batch "useAsInput @/tmp/o.json ; yaml --read Parameters,0,Value --delimiter ," --no-quote -i /dev/null -o - > /tmp/$$
	AWSAMIID=`cat /tmp/$$`
	echo AWSAMIID=`cat /tmp/$$` > ${AMIIDCache}
	\rm /tmp/o.json /tmp/$$
fi

###------------------------------
###???????????????????????????????????? In java make sure each and every REQUIRED CFN-Parameter below is defined before executing commands below.

CFNfile=/tmp/${CFNContext}

${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}

mkdir -p ~/.aws
echo "aws ec2 delete-key-pair --region ${AWSRegion} --profile \${AWSprofile} --key-name ${MySSHKeyName} " > ${CFNfile}.sh
echo "aws ec2 create-key-pair --region ${AWSRegion} --profile \${AWSprofile} --key-name ${MySSHKeyName} > ~/.aws/${MySSHKeyName}" >> ${CFNfile}.sh

PARAMS=" ParameterKey=MyPublicSubnet1,ParameterValue=${DefaultPublicSubnet1} "
PARAMS="${PARAMS} ParameterKey=MySSHSecurityGroup,ParameterValue=${MySSHSecurityGroup} ParameterKey=MyIamInstanceProfiles,ParameterValue=${MyIamInstanceProfiles} "
PARAMS="${PARAMS} ParameterKey=AWSAMIID,ParameterValue=${AWSAMIID} ParameterKey=EC2InstanceType,ParameterValue=${EC2InstanceType} "
PARAMS="${PARAMS} ParameterKey=MySSHKeyName,ParameterValue=${MySSHKeyName} "

echo "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-${JobSetName}-EC2-${MyEC2InstanceName}${ITEMNUMBER}  --region ${AWSRegion} --profile \${AWSprofile}  \
		--parameters ${PARAMS} --template-body file://${CFNfile} " \
		>> ${CFNfile}.sh
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! The STACK-NAME expression is VERY different vs. VPC and SG/EC2.. .. 
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! Especially, since EC2 instances have 'MyEC2InstanceName' added to Stack-name!
			#### !!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!! Make sure the STACK-NAME expression _OVERALL_ matching-up with 'MyStackNamePrefix' (see defn in cfngen-common.sh)

###------------------------------

#EoF
