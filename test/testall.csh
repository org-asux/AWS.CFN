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

###---------------------------------
set PROJECTNAME=CFN
set PROJECTPATH="${ORGASUXFLDR}/AWS/${PROJECTNAME}"
set PROJECTPATH=${AWSCFNFLDR}

# set TESTSRCFLDR=${PROJECTPATH}/test
# chdir ${TESTSRCFLDR}
# if ( "$VERBOSE" != "" ) pwd
chdir ${AWSCFNFLDR}/myjobs
if ( "$VERBOSE" != "" ) pwd

set JOBSET=simple

set SAMPLEJOBHOMEFLDR=${AWSCFNFLDR}/myjobs/${JOBSET}
set TEMPLATEFLDR=${SAMPLEJOBHOMEFLDR}/outputs${OFFLINE}
#____   set OUTPUTFLDR=/tmp/test-output-AWSCFN${OFFLINE}

#____   \rm -rf ${OUTPUTFLDR}
#____   mkdir -p ${OUTPUTFLDR}

set RUNTESTCMD="java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs"
set RUNTESTCMD="asux aws.cfn"

set DIVIDER='-----------------------------------------------------------------------------------------------------------'

###---------------------------------
set TESTNUM=0

###------------------------------
set CMD=vpc
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
echo $DIVIDER

set CMD=subnets
set PublicOrPrivate=Public
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-${PublicOrPrivate}.yaml  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}.yaml
diff /tmp/${CMD}-${PublicOrPrivate}.sh  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}.sh
echo $DIVIDER

set CMD=subnets
set PublicOrPrivate=Private
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-${PublicOrPrivate}.yaml  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}.yaml
diff /tmp/${CMD}-${PublicOrPrivate}.sh  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}.sh
echo $DIVIDER

set CMD=sg-ssh
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
echo $DIVIDER

# set CMD=sg-efs
# echo ${CMD}
# eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote"
# if ($status != 0) exit $status
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# echo $DIVIDER

set CMD=ec2plain
set PublicOrPrivate=Public
echo ${CMD}
echo "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-OrgASUXplayEC2plain.yaml  ${TEMPLATEFLDR}/${CMD}-OrgASUXplayEC2plain.yaml
diff /tmp/${CMD}-OrgASUXplayEC2plain.sh  ${TEMPLATEFLDR}/${CMD}-OrgASUXplayEC2plain.sh
echo $DIVIDER

# set CMD=vpnclient
# echo ${CMD}
# eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote"
# if ($status != 0) exit $status
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# echo $DIVIDER

###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

set TEMPLATEFLDR=${AWSCFNFLDR}/myjobs/outputs${OFFLINE}

# ??????????????????????????????????????????????? I have to test fullstack ??????????????????????????????????
# set CMD=fullstack
# echo ${CMD}
# eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote"
# if ($status != 0) exit $status
# echo ''
# set CMD=vpc
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# echo ''
# set CMD=subnets
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# echo ''
# set CMD=sg-ssh
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# echo ''
# set CMD=ec2plain
# diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
# diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
# echo $DIVIDER

### Error situations
echo
echo -n 'Error conditions now.. (Y/N) :>'; set ANS=$<
if ( "$ANS" == "Y" ||  "$ANS" == "y" ) then
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} unknownCmd-gen ${JOBSET} --no-quote
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --subnets-gen ${JOBSET} --no-quote
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --sgssh-gen ${JOBSET} --no-quote
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --unknownCmd-gen ${JOBSET} --no-quote sdfasd asdf asdfa
endif

###---------------------------------
# 2
# set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
# echo $OUTPFILE
# java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} ??????  #_____ >&! ${OUTPFILE}
# diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}
# if ($status != 0) exit $status


#EoInfo

