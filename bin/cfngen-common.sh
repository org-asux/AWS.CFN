#/bin/false


### This must be included - via '.'
### This will NOT run as a standalone script


###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### The following cmd-line arguments analysis MUST be the 1st thing in this cfngen-common.sh file

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

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### This code below to define the 'JobSetMaster' and to __LOAD__ the properties file pointed to by 'JobSetMaster' .. .. MUST precede the 'sourcing' of 'common.sh'

JobSetMaster=jobset-Master.properties
grep -v '^#' ${JobSetName}/${JobSetMaster} | grep -v 'AWS.profile' > /tmp/$$
. /tmp/$$
rm /tmp/$$
#____ properties JobProps=./jobset-${ASUX::SCRIPT}.properties

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### Make sure variables like AWSRegion are defined (by reading jobSet properties file) before sourcing 'common.sh'
### Make sure variables like ${SCRIPTFULLFLDRPATH} are defined (by 'topmost' script-file) before sourcing 'common.sh' and 'cfngen-common.sh'

if [ -z ${SCRIPTFULLFLDRPATH+x} ]; then  ### if [ -z "$var" ]    <-- does NOT distinguish between 'unset var' & var=""
	>&2 echo "The topmost script that 'includes/sources' common.sh and 'cfngen-common.sh' must define SCRIPTFULLFLDRPATH ${SCRIPTFULLFLDRPATH}"
	exit 9
fi

.   ${SCRIPTFULLFLDRPATH}/common.sh

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#_____ \grep -v '^#' ${AWSCFNHOME}/config/AWSRegionsLocations.properties | \grep -v '^$' | \sed -e 's/^AWS-/set /' > /tmp/$$
#_____ . /tmp/$$
#_____ \rm /tmp/$$
#_____ read -p '(3) enter to continue' USERRESPONSE

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

###		properties TagDefaultsProps=./Tags-MyMaster.properties
###		properties TagDefaultsProps=./Tags-${ASUX::SCRIPT}.properties

\grep -v '^#' ${JobSetName}/${JobSetMaster} | \grep -v '^$' | \sed -e 's/^AWS.profile=/AWSPROFILE=/'  > /tmp/$$
. /tmp/$$
\rm /tmp/$$
if [ "${VERBOSE}" == "1" ]; then
	echo "[y] parsed file ${JobSetName}/${JobSetMaster}"
	read -p '(1) enter to continue' USERRESPONSE

	\grep -v '^#' ${JobSetName}/${JobSetMaster} | \grep -v '^$' | \sed -e 's/^AWS.profile=/AWSPROFILE=/' | \sed -e 's/^AWS-/set /'
	read -p '(1) enter to continue' USERRESPONSE
fi

\grep -v '^#' ${JobSetName}/${JobSetMaster} | \grep -v '^$' | \sed -e 's/^AWS.profile=/AWSPROFILE=/' | \sed -e 's/^AWS-/set /' > /tmp/$$
. /tmp/$$
\rm /tmp/$$
if [ "${VERBOSE}" == "1" ]; then read -p '(2) enter to continue' USERRESPONSE; fi

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

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#EoF
