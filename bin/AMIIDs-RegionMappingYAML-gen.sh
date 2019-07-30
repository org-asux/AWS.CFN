#!/bin/sh -f

###------------------------------
### Even though this will do the 'AMIID-Mapping' for __ALL__ regions, the invocation of AWS APIs requires you to pick a region'
if [ $# -eq 1 ]; then
	AWSRegion=$1
else
	>&2 echo "Usage: $0 us-east-2"
	>&2 echo ''
	>&2 echo 'Even though this will do the AMIID-Mapping for __ALL__ regions, the invocation of AWS APIs requires you to pick a region'
	>&2 echo ''
	exit 1
fi

unset VERBOSE

###------------------------------

### The following line did NOT work on Windows
# CmdPathGuess="${BASH_SOURCE[0]}"

CmdPathGuess="$0"
# echo $CmdPathGuess
SCRIPTFLDR_RELATIVE="$(dirname "$CmdPathGuess")"
SCRIPTFULLFLDRPATH="$( cd "$(dirname "$0")" ; pwd -P )"
if [ "${VERBOSE}" == "1" ]; then echo SCRIPTFULLFLDRPATH=${SCRIPTFULLFLDRPATH}; fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

### Attention !!!!!!!!! SCRIPTFULLFLDRPATH must be set .. before sourcing/including '{SCRIPTFULLFLDRPATH}/../../bin.common.sh'
.   ${SCRIPTFULLFLDRPATH}/common.sh


### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### Make sure 'cd' command below comes __AFTER__ sourcing 'common.sh' (above)

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#___ AMIIDbyREGION_PROPSFILE="${AWSSDKHOME}/etc/offline-downloads/AMZNLinux2_AMI_ID-${AWSLocation}".txt
AMIIDbyREGIONMAPPING_YAMLFILE="${AWSCFNHOME}/AWSCFN.templates-pieces/AMIID-Region-Mapping.yaml"
TMPFILE=/tmp/t.txt

###-------------------
if [ -e "${AMIIDbyREGIONMAPPING_YAMLFILE}" ]; then
	>&2 echo "${AMIIDbyREGIONMAPPING_YAMLFILE} already exists."
	>&2 echo "		Please check the contents of the file and delete it if necessary"
	>&2 echo "		Cntl-C now.. else, it will be overwritten !!!!!!!!! (10s)"
	sleep 10
fi


ensureTempFileDoesNotExist ${TMPFILE}		### This function is defined within ${ORGASUXHOME}/bin/common.sh

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

###------------------------------
# asux yaml batch "		\
# 	properties AWSRegionLocations=${AWSSDKHOME}/config/AWSRegionsLocations.properties ;	\
# 	echo useAsInput @${AMIIDbyREGIONMAPPING_YAMLFILE} ;		\
# 	saveTo !AMIIDbyREGIONMAPPING ;						\
# 	aws.sdk --list-regions --offline ;					\
# 	foreach  ;											\
# 		print \${ASUX::foreach.iteration.value} ;		\
# 		setProperty AWSRegion=\${ASUX::foreach.iteration.value} ;		\
# 		setProperty AWSLocation=\${ASUX::AWS-\${ASUX::AWSRegion}} ;		\
# 		print \${ASUX::AWSLocation} \\n ;				\
# 		useAsInput  !AMIIDbyREGIONMAPPING ;				\
# 		yaml --delete AMIIDRegionMap.\${ASUX::AWSRegion} ;			\
# 		properties OneAMI=${AWSSDKHOME}/etc/offline-downloads/AMZNLinux2_AMI_ID-\${ASUX::AWSLocation}.txt ;		\
# 		yaml --insert AMIIDRegionMap.\${ASUX::AWSRegion} \${ASUX::AWSAMIID} ;	\
# 		saveTo  !AMIIDbyREGIONMAPPING ;					\
# 	end	 ;				\
# 	useAsInput !AMIIDbyREGIONMAPPING ;					\
# 	" \
asux  yaml  batch  @${AWSCFNHOME}/bin/AMIIDs-RegionMappingYAML.ASUX-batch.txt    --no-quote  -i /dev/null  -o ${TMPFILE}

###------------------------------
cat ${TMPFILE}

mv -i     ${TMPFILE}   "${AMIIDbyREGIONMAPPING_YAMLFILE}"

###------------------------------

#EoF
