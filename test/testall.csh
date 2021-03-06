#!/bin/tcsh -f

###------------------------------
if (  !   $?ORGASUXFLDR ) then
        which asux >& /dev/null
        if ( $status == 0 ) then
                set ORGASUXFLDR=`which asux`
                set ORGASUXFLDR=$ORGASUXFLDR:h
                if ( "${ORGASUXFLDR}" == "." ) set ORGASUXFLDR=$cwd
                setenv ORGASUXFLDR "${ORGASUXFLDR}"
                echo "ORGASUXFLDR=$ORGASUXFLDR"
        else
                foreach FLDR ( ~/org.ASUX   ~/github/org.ASUX   ~/github.com/org.ASUX  /mnt/development/src/org.ASUX     /opt/org.ASUX  /tmp/org.ASUX  )
                        set ORIGPATH=( $path )
                        if ( -x "${FLDR}/asux" ) then
                                set ORGASUXFLDR="$FLDR"
                                set path=( $ORIGPATH "${ORGASUXFLDR}" )
                                rehash
                        endif
                end
                setenv ORGASUXFLDR "${ORGASUXFLDR}"
        endif
endif

###------------------------------
source ${ORGASUXFLDR}/test/testAll-common.csh-source

###-------------------------------------------------------------------
### @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###-------------------------------------------------------------------

set PROJECTNAME=CFN
set PROJECTPATH="${ORGASUXFLDR}/AWS/${PROJECTNAME}"
set PROJECTPATH=${AWSCFNFLDR}

# set TESTSRCFLDR=${PROJECTPATH}/test
# chdir ${TESTSRCFLDR}
# if ( "$VERBOSE" != "" ) pwd
chdir ${AWSCFNFLDR}/test/myjobs      ### <---------- <<------------ Must be in ./myjobs subfolder to run any tests.
if ( "$VERBOSE" != "" ) pwd

#____   set OUTPUTFLDR=/tmp/test-output-AWSCFN${OFFLINE}
#____   \rm -rf ${OUTPUTFLDR}
#____   mkdir -p ${OUTPUTFLDR}

set RUNTESTCMD="java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs"
set RUNTESTCMD="asux aws.cfn"

set DIVIDER=${HOME}/etc/.line

###-------------------------------------------------------------------
### @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###-------------------------------------------------------------------

set TESTNUM=0

set JOBSET=simple

set SAMPLEJOBHOMEFLDR=${AWSCFNFLDR}/test/myjobs/${JOBSET}
set TEMPLATEFLDR=${SAMPLEJOBHOMEFLDR}/outputs${OFFLINE}

###------------------------------
set CMD=vpc
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
cat $DIVIDER

set CMD=subnet
set PublicOrPrivate=Public
set itemNumber=0
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} -n ${itemNumber}  --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-${PublicOrPrivate}-${itemNumber}.yaml  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}-${itemNumber}.yaml
diff /tmp/${CMD}-${PublicOrPrivate}-${itemNumber}.sh  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}-${itemNumber}.sh
cat $DIVIDER

set CMD=subnet
set PublicOrPrivate=Private
set itemNumber=1
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --itemNumber ${itemNumber}   --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-${PublicOrPrivate}-${itemNumber}.yaml  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}-${itemNumber}.yaml
diff /tmp/${CMD}-${PublicOrPrivate}-${itemNumber}.sh  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}-${itemNumber}.sh
cat $DIVIDER

set CMD=sg
set SGPORTS=ssh
set itemNumber=0
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${SGPORTS}  --itemNumber ${itemNumber}   --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-${SGPORTS}-${itemNumber}.yaml  ${TEMPLATEFLDR}/${CMD}-${SGPORTS}-${itemNumber}.yaml
diff /tmp/${CMD}-${SGPORTS}-${itemNumber}.sh  ${TEMPLATEFLDR}/${CMD}-${SGPORTS}-${itemNumber}.sh
cat $DIVIDER

set CMD=ec2plain
set PublicOrPrivate=Public
echo ${CMD}
echo "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-OrgASUXplayEC2plain.yaml  ${TEMPLATEFLDR}/${CMD}-OrgASUXplayEC2plain.yaml
diff /tmp/${CMD}-OrgASUXplayEC2plain.sh    ${TEMPLATEFLDR}/${CMD}-OrgASUXplayEC2plain.sh
cat $DIVIDER

# set CMD=vpnclient
# echo ${CMD}
# eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote"
# if ($status != 0) exit $status
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# cat $DIVIDER

###-------------------------------------------------------------------
### @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###-------------------------------------------------------------------

set JOBSET=2layer
echo -n "Proceed with ${JOBSET} ? .. (or press Cntl-C) >>";  set ANS=$<

set SAMPLEJOBHOMEFLDR=${AWSCFNFLDR}/test/myjobs/${JOBSET}
set TEMPLATEFLDR=${SAMPLEJOBHOMEFLDR}/outputs${OFFLINE}

###--------------
set CMD=fullstack
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote --s3 org-asux-aws-cfn@us-east-1"
if ($status != 0) exit $status
echo ''

set CMD=fullstack-vpc
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}.sh

set CMD=fullstack-subnet-Public
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}*.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}*.sh

set CMD=fullstack-subnet-Private
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}*.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}*.sh

set CMD=fullstack-sg
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}*.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}*.sh

set CMD=fullstack-ec2plain-MyWebASUXLinux1
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}.sh

set CMD=fullstack-ec2plain-MyPrivASUXLinux2
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}.sh

###--------------------
cat $DIVIDER; echo -n "\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!  git checkout --force ? .. (or press Cntl-C) >>"; set ANS=$<
git checkout --force ${JOBSET}/*.yaml ${JOBSET}/*.sh
git status

###-------------------------------------------------------------------
### @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###-------------------------------------------------------------------

set JOBSET=2layerExisting
echo -n "Proceed with ${JOBSET} ? .. (or press Cntl-C) >>";  set ANS=$<

set SAMPLEJOBHOMEFLDR=${AWSCFNFLDR}/test/myjobs/${JOBSET}
set TEMPLATEFLDR=${SAMPLEJOBHOMEFLDR}/outputs${OFFLINE}

###--------------
while ( "$ANS" != "yes" )
        echo ".. .. .. .. .. Does Sydney/ap-northeast-2 already have the ASUX.org created VPC & SG-SSH ?  If not, run ${cwd}/${JOBSET}/fullstack-vpc-existing.sh    &    ${cwd}/${JOBSET}/fullstack-sg-ssh-existing.sh"
        echo aws ec2 describe-vpcs --region ap-northeast-2 --output json --profile ${AWSprofile}˝
        echo -n "       Enter 'yes' to proceed /or/ press Cntl-C to abort >>";  set ANS=$<
end

eval "$RUNTESTCMD ${VERBOSE} fullstack-gen ${JOBSET} --no-quote --s3 org-asux-aws-cfn@us-east-1"
if ($status != 0) EXITSCRIPT $status
echo ''

# set CMD=fullstack-vpc
# set CMD=fullstack-sg-ssh

set CMD=fullstack-subnet-Public
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}*.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}*.sh

set CMD=fullstack-subnet-Private
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}*.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}*.sh

set CMD=fullstack-ec2plain-MyWebASUXLinux1
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}.sh

set CMD=fullstack-ec2plain-MyPrivASUXLinux2
cat $DIVIDER; echo -n "show diff for ${CMD} ? .. (or press Cntl-C) >>"; set ANS=$<
git diff ${JOBSET}/${CMD}.yaml
cat $DIVIDER
git diff ${JOBSET}/${CMD}.sh

###--------------------
cat $DIVIDER; echo -n "\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!\!  git checkout --force ? .. (or press Cntl-C) >>"; set ANS=$<
git checkout --force ${JOBSET}/*.yaml ${JOBSET}/*.sh
git status

###-------------------------------------------------------------------
### @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###-------------------------------------------------------------------

### Error situations
echo
echo -n 'Error conditions now.. (Y/N) :>'; set ANS=$<
if ( "$ANS" == "Y" ||  "$ANS" == "y" ) then
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} unknownCmd-gen ${JOBSET} --no-quote
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --subnets-gen ${JOBSET} --no-quote
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --sgssh-gen ${JOBSET} --no-quote
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --unknownCmd-gen ${JOBSET} --no-quote sdfasd asdf asdfa
endif

###-------------------------------------------------------------------
### @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###-------------------------------------------------------------------

# 2
# set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
# echo $OUTPFILE
# java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} ??????  #_____ >&! ${OUTPFILE}
# diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}
# if ($status != 0) exit $status

#EoInfo

