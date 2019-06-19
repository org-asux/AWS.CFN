#/bin/false


### This must be included - via '.'
### This will NOT run as a standalone script


###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### The following cmd-line arguments analysis MUST be the 1st thing in this cfngen-common.sh file

if [ $# -ge 2 ]; then
	if [ "$1" == "--verbose" ]; then
		VERBOSE=1
		shift
		JobSetName="$1";	### 1st REQUIRED cmd-line argument (after optional --verbose)
		ITEMNUMBER="-$2";	### 2nd REQUIRED cmd-line argument (after optional --verbose)
	else
		if [ "$2" == "--verbose" ]; then
			VERBOSE=1
			JobSetName="$1";	### 1st REQUIRED cmd-line argument (after optional --verbose)
			if [ $# -ge 3 ]; then
				ITEMNUMBER="-$3";	### 2nd REQUIRED cmd-line argument (after optional --verbose)
			else
				ITEMNUMBER="";
			fi
		else
			JobSetName="$1";	### 1st REQUIRED cmd-line argument (after optional --verbose)
			ITEMNUMBER="-$2";	### 2nd REQUIRED cmd-line argument (after optional --verbose)
		fi
	fi
else
	if [ $# -eq 1 ]; then
		if [ "$1" == "--verbose" ]; then
			echo "Usage: $0 [--verbose] JobSetName"
			exit 2
		fi
		JobSetName="$1" ### 1PublicEC2plain
		ITEMNUMBER=""
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

.	${JobSetName}/${JobSetMaster}
# grep -v '^#' ${JobSetName}/${JobSetMaster} > /tmp/$$
# . /tmp/$$
# rm /tmp/$$
#____ properties JobProps=./jobset-${ASUX::SCRIPT}.properties
if [ "${VERBOSE}" == "1" ]; then
	echo "[y] parsed file ${JobSetName}/${JobSetMaster}"
	read -p '(1) enter to see what was loaded' USERRESPONSE

	\grep -v '^#' ${JobSetName}/${JobSetMaster} | \grep -v '^$' ###------ | \sed -e 's/^AWS-/set /'
	read -p '(1) enter to continue' USERRESPONSE
fi

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

### ------ These represent the 'EXPORTS' from executing the CFN-templates via 'aws cloudformation' commands ------
MyVPCStackPrefix="${MyOrgName}-${MyEnvironment}-${AWSLocation}"
MyStackNamePrefix="${MyVPCStackPrefix}-${JobSetName}${ITEMNUMBER}"  ### Attention: 'ITEMNUMBER' if it's NOT empty-string, will automatically have a '-' as it's 1st character
VPCID="${MyVPCStackPrefix}-VPCID"			### ATTENTION!!   JobSets will HAVE to share a VPC.  All VPC components should be within the single JobSet.
DefaultAZ=${MyVPCStackPrefix}-AZ-ID			### ATTENTION!!   DefaultAZ has a 1:1 relationship with a VPC.

DefaultPublicSubnet1=${MyStackNamePrefix}-Subnet-1-ID
MySSHSecurityGroup=${MyStackNamePrefix}-SG-LinuxSSH
MySSHKeyName=${AWSRegion}-${MyOrgName}-${MyEnvironment}-LinuxSSH.pem

if [ "${VERBOSE}" == "1" ]; then
	echo MyVPCStackPrefix=${MyVPCStackPrefix}
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
