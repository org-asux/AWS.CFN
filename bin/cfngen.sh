#!/bin/sh -f

###------------------------------
if [ $# -eq 2 ]; then
	if [ "$1" == "--verbose" ]; then
		VERBOSE=1
		shift
	else
		if [ "$2" == "--verbose" ]; then
			VERBOSE=1
		else
			echo "Usage: $0 [--verbose] <JobSetName>"
			exit 1
		fi
	fi
	JobSetName="$1" ### 1PublicEC2plain

else
	if [ $# -eq 1 ]; then
		if [ "$1" == "--verbose" ]; then
			echo "Usage: $0 [--verbose] JobSetName"
			exit 2
		fi
		JobSetName="$1" ### 1PublicEC2plain
	else
		echo "Usage: $0 [--verbose] JobSetName"
		exit 3
	fi
fi

###------------------------------

### The following line did NOT work on Windows
# CmdPathGuess="${BASH_SOURCE[0]}"

CmdPathGuess="$0"
# echo $CmdPathGuess
SCRIPTFLDR_RELATIVE="$(dirname "$CmdPathGuess")"
SCRIPTFULLFLDRPATH="$( cd "$(dirname "$0")" ; pwd -P )"
#___ echo ${SCRIPTFULLFLDRPATH}
#___	if [ "${SCRIPTFLDR_RELATIVE}" != "." ]; then
#___	fi

###-------------------
if [ -d ${JobSetName} ]; then
	cd ${JobSetName}
fi
USERFLDR=`pwd`
if [ "${VERBOSE}" == "1" ]; then echo ${USERFLDR}; fi

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
	if [ "${VERBOSE}" == "1" ]; then
		echo "[y] verified that Node.JS (node) is installed"
		echo "[y] verified that ${ORGASUXFLDR}/asux.js can be executed"
	fi
fi

if [ ! -e ${AWSPROFILE} ]; then
        echo "AWS login credentials missing in a file ${AWSPROFILE}"
        exit 7
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#___ chdir ${TESTSRCFLDR}

###		setProperty AWSCFN.HOME=/Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN
###		setProperty SCRIPT=MySGSSH

###		properties JobPropsMaster=./jobset-Master.properties
###		properties JobProps=./jobset-${ASUX::SCRIPT}.properties

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

###		properties TagDefaultsProps=./Tags-MyMaster.properties
###		properties TagDefaultsProps=./Tags-${ASUX::SCRIPT}.properties

\grep -v '^#' ${USERFLDR}/jobset-Master.properties | \grep -v '^$' | \sed -e 's/^AWS.profile=/AWSPROFILE=/'  > /tmp/$$
. /tmp/$$
\rm /tmp/$$
if [ "${VERBOSE}" == "1" ]; then
	echo "[y] parsed file ${USERFLDR}/jobset-Master.properties"
	read -p '(1) enter to continue' USERRESPONSE

	\grep -v '^#' ${USERFLDR}/jobset-Master.properties | \grep -v '^$' | \sed -e 's/^AWS.profile=/AWSPROFILE=/' | \sed -e 's/^AWS-/set /'
	#_____ read -p '(1) enter to continue' USERRESPONSE
fi

\grep -v '^#' ${USERFLDR}/jobset-Master.properties | \grep -v '^$' | \sed -e 's/^AWS.profile=/AWSPROFILE=/' | \sed -e 's/^AWS-/set /' > /tmp/$$
. /tmp/$$
\rm /tmp/$$
#_____ read -p '(2) enter to continue' USERRESPONSE

#_____ \grep -v '^#' ${AWSCFNFLDR}/config/AWSRegionsLocations.properties | \grep -v '^$' | \sed -e 's/^AWS-/set /' > /tmp/$$
#_____ . /tmp/$$
#_____ \rm /tmp/$$
#_____ read -p '(3) enter to continue' USERRESPONSE
grep ${AWSRegion} ${AWSCFNFLDR}/config/AWSRegionsLocations.properties | \sed -e 's/.*=/AWSLocation=/' > /tmp/$$
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

### ------ EXPORTS from CFN executions ------
MyStackNamePrefix=${MyOrgName}-${MyEnvironment}-${AWSLocation}
VPCID=${MyStackNamePrefix}-VPCID
DefaultAZ=${MyStackNamePrefix}-AZ-ID
DefaultPublicSubnet1=${MyStackNamePrefix}-Subnet-1-ID
MySSHSecurityGroup=${MyStackNamePrefix}-SG-LinuxSSH-1
MySSHKeyName=${AWSRegion}-${MyOrgName}-${MyEnvironment}-LinuxSSH.pem

if [ "${VERBOSE}" == "1" ]; then
	echo MyStackNamePrefix=${MyStackNamePrefix}
	echo VPCID=${VPCID}
	echo DefaultAZ=${DefaultAZ}
	echo DefaultPublicSubnet1=${DefaultPublicSubnet1}
	echo MySSHSecurityGroup=${MySSHSecurityGroup}
	echo MyIamInstanceProfiles=${MyIamInstanceProfiles}
	echo MySSHKeyName=${MySSHKeyName}
fi

${SCRIPTFULLFLDRPATH}/AMIlisting.sh ${AWSRegion} | grep -v '.-' > /tmp/$$  ### Weed out 'ap-northeast-1LinuxAMI=...' and keep only 'TokyoLimuxAMI='
. /tmp/$$
\rm /tmp/$$

if [ "${VERBOSE}" == "1" ]; then
	echo AWSAMIID=${AWSLocation}LinuxAMI
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

CFNfile=/tmp/network
asux.js yaml batch @${AWSCFNFLDR}/bin/AWSCFN-Network-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
echo "aws cloudformation create-stack --stack-name --region ${AWSRegion} --profile {AWSPROFILE}" > ${CFNfile}.sh

###-------------------
CFNfile=/tmp/sg
asux.js yaml batch @${AWSCFNFLDR}/bin/AWSCFN-SGforSSH-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
echo "aws cloudformation create-stack --stack-name --parameters ParameterKey=MyVPC,ParameterValue==${VPCID}" > ${CFNfile}.sh

###-------------------
CFNfile=/tmp/ec2
asux.js yaml batch @${AWSCFNFLDR}/bin/AWSCFN-MyEC2plain-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
PARAMS=" ParameterKey=MyVPC,ParameterValue==${VPCID} ParameterKey=MyPublicSubnet1,ParameterValue==${DefaultPublicSubnet1} "
PARAMS="${PARAMS} ParameterKey=MySSHSecurityGroup,ParameterValue==${MySSHSecurityGroup}  ParameterKey=MyIamInstanceProfiles,ParameterValue==${MyIamInstanceProfiles} "
PARAMS="${PARAMS} ParameterKey=AWSAMIID,ParameterValue==${AWSAMIID} ParameterKey=MySSHKeyName,ParameterValue==${MySSHKeyName} "
echo "aws cloudformation create-stack --stack-name --parameters ${PARAMS} " > ${CFNfile}.sh

#EoF
