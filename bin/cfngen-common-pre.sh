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

###-----------------------
### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### This code below to __LOAD__ the 'job-DEFAULTS.properties' file  MUST __PRECEDE__ the __LOAD__ of 'jobset-Master.properties' (which is a file specific to your job)

.  ${AWSCFNHOME}/config/defaults/job-DEFAULTS.properties

###-----------------------
JobSetMaster=jobset-Master.properties

.	${JobSetName}/${JobSetMaster}

if [ "${VERBOSE}" == "1" ]; then
	echo "[y] parsed file ${JobSetName}/${JobSetMaster}"
	read -p '(1) enter to see what was loaded' USERRESPONSE

	\grep -v '^#' ${JobSetName}/${JobSetMaster} | \grep -v '^$' ###------ | \sed -e 's/^AWS-/set /'
	read -p '(1) enter to continue' USERRESPONSE
fi

#____ echo INSIDE cfngen-common-PRE.sh .. TripletConstants are ="${MyOrgName}-${MyEnvironment}-${AWSLocation}"

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

#____ echo INSIDE cfngen-common-PRE.sh .. TripletConstants are ="${MyOrgName}-${MyEnvironment}-${AWSLocation}"

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#_____ \grep -v '^#' ${AWSCFNHOME}/config/AWSRegionsLocations.properties | \grep -v '^$' | \sed -e 's/^AWS-/set /' > ${AUTOGENPROPSFILE}
#_____ . /tmp/$$
#_____ \rm /tmp/$$
#_____ read -p '(3) enter to continue' USERRESPONSE

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

AUTOGENPROPSFILE=${JobSetName}/AutoGen.properties

echo MyVPCStackPrefix="${MyOrgName}-${MyEnvironment}-${AWSLocation}"	> ${AUTOGENPROPSFILE}   ### Attention: Its NOT '>>' (append2file).  Its create a __NEW__ File
MyVPCStackPrefix="${MyOrgName}-${MyEnvironment}-${AWSLocation}"			### We need to ensure these variables ARE __FULLY__ defined, for the upcoming asux.js cmdline arguments.

echo JobSetName="${JobSetName}"						>> ${AUTOGENPROPSFILE}
echo ITEMNUMBER="${ITEMNUMBER}"						>> ${AUTOGENPROPSFILE}

echo MyStackNamePrefix="${MyVPCStackPrefix}-${JobSetName}${ITEMNUMBER}"			>> ${AUTOGENPROPSFILE}
MyStackNamePrefix="${MyVPCStackPrefix}-${JobSetName}${ITEMNUMBER}"		### We need to ensure these variables ARE __FULLY__ defined, for the upcoming asux.js cmdline arguments.

echo VPCID="${MyVPCStackPrefix}-VPCID"				>> ${AUTOGENPROPSFILE}
echo DefaultAZ=${MyVPCStackPrefix}-AZ-ID			>> ${AUTOGENPROPSFILE}

echo DefaultPublicSubnet1="${MyStackNamePrefix}-Subnet-1-ID"		>> ${AUTOGENPROPSFILE}
echo MySSHSecurityGroup="${MyStackNamePrefix}-SG-SSH"				>> ${AUTOGENPROPSFILE}
echo MyIamInstanceProfiles=${MyIamInstanceProfiles}					>> ${AUTOGENPROPSFILE}
echo MySSHKeyName="${AWSLocation}-${MyOrgName}-${MyEnvironment}-LinuxSSH.pem"		>> ${AUTOGENPROPSFILE}

### ------ These represent the 'EXPORTS' from executing the CFN-templates via 'aws cloudformation' commands ------
# MyVPCStackPrefix="${MyOrgName}-${MyEnvironment}-${AWSLocation}"
# MyStackNamePrefix="${MyVPCStackPrefix}-${JobSetName}${ITEMNUMBER}"  ### Attention: 'ITEMNUMBER' if it's NOT empty-string, will automatically have a '-' as it's 1st character
# VPCID="${MyVPCStackPrefix}-VPCID"			### ATTENTION!!   JobSets will HAVE to share a VPC.  All VPC components should be within the single JobSet.
# DefaultAZ=${MyVPCStackPrefix}-AZ-ID			### ATTENTION!!   DefaultAZ has a 1:1 relationship with a VPC.

# DefaultPublicSubnet1="${MyStackNamePrefix}-Subnet-1-ID"
# MySSHSecurityGroup="${MyStackNamePrefix}-SG-LinuxSSH"
# MySSHKeyName="${AWSRegion}-${MyOrgName}-${MyEnvironment}-LinuxSSH.pem"

.	${AUTOGENPROPSFILE}

#____ echo INSIDE cfngen-common-PRE.sh .. MyStackNamePrefix=${MyStackNamePrefix}


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
