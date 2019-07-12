#!/bin/tcsh -f

###------------------------------
echo "Usage: $0 [--verbose] [--offline]"
# if ( $#argv <= 1 ) then
#     echo "Usage: $0  [--verbose] --delete --yamlpath yaml.regexp.path $YAMLLIB --inputfile /tmp/input.yaml -o /tmp/output.yaml " >>& /dev/stderr
#     echo Usage: $0 'org.ASUX.yaml.Cmd [--verbose] --delete --double-quote --yamlpath "paths.*.*.responses.200" $YAMLLIB --inputfile $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml ' >>& /dev/stderr
#     echo '' >>& /dev/stderr
#     exit 1
# /Users/Sarma/Documents/Development/src/org.ASUX/ExecShellCommand.js :-
#                       java arguments: -cp :/Users/Sarma/.m2/repository/org/asux/common/1.0/common-1.0.jar:/Users/Sarma/.m2/repository/org/asux/yaml/1.0/yaml-1.0.jar:/Users/Sarma/.m2/repository/org/asux/yaml.collectionsimpl/1.0/yaml.collectionsimpl-1.0.jar:/Users/Sarma/.m2/repository/junit/junit/4.8.2/junit-4.8.2.jar:/Users/Sarma/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar:/Users/Sarma/.m2/repository/com/esotericsoftware/yamlbeans/yamlbeans/1.13/yamlbeans-1.13.jar:/Users/Sarma/.m2/repository/org/yaml/snakeyaml/1.24/snakeyaml-1.24.jar:/Users/Sarma/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.9.8/jackson-databind-2.9.8.jar:/Users/Sarma/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.9.6/jackson-annotations-2.9.6.jar:/Users/Sarma/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.8/jackson-core-2.9.8.jar:/Users/Sarma/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.6/jackson-core-2.9.6.jar:/Users/Sarma/.m2/repository/com/opencsv/opencsv/4.0/opencsv-4.0.jar:/Users/Sarma/.m2/repository/org/apache/commons/commons-lang3/3.6/commons-lang3-3.6.jar:/Users/Sarma/.m2/repository/org/apache/commons/commons-text/1.1/commons-text-1.1.jar:/Users/Sarma/.m2/repository/commons-beanutils/commons-beanutils/1.9.3/commons-beanutils-1.9.3.jar:/Users/Sarma/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/Users/Sarma/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/Users/Sarma/.m2/repository/com/amazonaws/aws-java-sdk-core/1.11.541/aws-java-sdk-core-1.11.541.jar:/Users/Sarma/.m2/repository/org/apache/httpcomponents/httpclient/4.5.5/httpclient-4.5.5.jar:/Users/Sarma/.m2/repository/org/apache/httpcomponents/httpcore/4.4.10/httpcore-4.4.10.jar:/Users/Sarma/.m2/repository/commons-codec/commons-codec/1.10/commons-codec-1.10.jar:/Users/Sarma/.m2/repository/software/amazon/ion/ion-java/1.2.0/ion-java-1.2.0.jar:/Users/Sarma/.m2/repository/com/fasterxml/jackson/dataformat/jackson-dataformat-cbor/2.9.6/jackson-dataformat-cbor-2.9.6.jar:/Users/Sarma/.m2/repository/joda-time/joda-time/2.8.1/joda-time-2.8.1.jar:/Users/Sarma/.m2/repository/com/amazonaws/aws-java-sdk-ec2/1.11.541/aws-java-sdk-ec2-1.11.541.jar:/Users/Sarma/.m2/repository/com/amazonaws/jmespath-java/1.11.541/jmespath-java-1.11.541.jar
#                       org.ASUX.yaml.Cmd --verbose --yamllibrary CollectionsImpl
#                       --batch @./mapsBatch1.txt -i /dev/null -o -
#
# endif

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
                        set ORIGPATH=$path
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
set AWSCFNFLDR=${ORGASUXFLDR}/AWS/CFN
set TESTSRCFLDR=${AWSCFNFLDR}/test

###------------------------------
if ( $#argv == 1 && "$1" == "--verbose" ) then
        set VERBOSE="--verbose"
        shift
else
        set VERBOSE=""
endif

if ( $#argv == 1 && "$1" == "--offline" ) then
        set OFFLINE="$1"
        shift
else
        set OFFLINE=""
endif

chdir ${TESTSRCFLDR}
chdir ${AWSCFNFLDR}/myjobs
if ( "$VERBOSE" != "" ) pwd

###------------------------------
# echo -n "Sleep interval? >>"; set DELAY=$<
# if ( "$DELAY" == "" ) set DELAY=2

set JARFLDR=${ORGASUXFLDR}/lib
set UBERJARPATH=${JARFLDR}/org.asux.mvn-shade-uber-jar-1.0.jar

#_____ ${JARFLDR}/org.asux.aws-sdk.aws-sdk-1.0.jar
#_____ ${JARFLDR}/org.asux.yaml.nodeimpl.yaml.nodeimpl-1.0.jar
#_____ ${JARFLDR}/org.asux.yaml.yaml-1.0.jar
#_____ ${JARFLDR}/org.asux.yaml.collectionsimpl.yaml.collectionsimpl-1.0.jar

# set COMMONSCLIJAR=${JARFLDR}/commons-cli.commons-cli.commons-cli-1.4.jar
# #___ commons-cli-1.4.jar
# set JUNITJAR=${JARFLDR}/junit.junit.junit-4.8.2.jar
# set ASUXCOMMON=${JARFLDR}/org.asux.common.common-1.0.jar
# set ASUXCOMMON=/Users/Sarma/.m2/repository/org/asux/common/1.0/common-1.0.jar
# set ASUXYAML=${JARFLDR}/org.asux.yaml.yaml-1.0.jar
# set ASUXYAML=/Users/Sarma/.m2/repository/org/asux/yaml/1.0/yaml-1.0.jar
# set ASUXAWSCFN=${JARFLDR}/org.asux.aws-cfn.aws-cfn-1.0.jar
# set ASUXAWSCFN=/Users/Sarma/.m2/repository/org/asux/aws-cfn/1.0/aws-cfn-1.0.jar

if ( $?CLASSPATH ) then
        # setenv CLASSPATH  ${CLASSPATH}:${ASUXAWSCFN}:${ASUXYAML}:${ASUXCOMMON}:${COMMONSCLIJAR}:${JUNITJAR}
        setenv CLASSPATH ${CLASSPATH}:${UBERJARPATH}
else
        # setenv CLASSPATH  ${ASUXAWSCFN}:${ASUXYAML}:${ASUXCOMMON}:${COMMONSCLIJAR}:${JUNITJAR}
        setenv CLASSPATH ${UBERJARPATH}
endif

if ( "$VERBOSE" != "" ) echo $CLASSPATH

set DIVIDER='-----------------------------------------------------------------------------------------------------------'

###---------------------------------
set noglob ### Very important to allow us to use '*' character on cmdline arguments
set noclobber

set TESTNUM=1

###---------------------------------
set JOBSET=simple

###---------------------------------

set SAMPLEJOBHOMEFLDR=${AWSCFNFLDR}/myjobs/${JOBSET}
set TEMPLATEFLDR=${SAMPLEJOBHOMEFLDR}/outputs${OFFLINE}
#____   set OUTPUTFLDR=/tmp/test-output-AWSCFN${OFFLINE}

#____   \rm -rf ${OUTPUTFLDR}
#____   mkdir -p ${OUTPUTFLDR}

set RUNTESTCMD="java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs"
set RUNTESTCMD="asux aws.cfn"

###------------------------------
# 1

set CMD=vpc
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}.yaml  ${TEMPLATEFLDR}/${CMD}.yaml
diff /tmp/${CMD}.sh  ${TEMPLATEFLDR}/${CMD}.sh
echo $DIVIDER

set CMD=subnets
set PublicOrPrivate=public
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} ${PublicOrPrivate} --no-quote ${OFFLINE}"
if ($status != 0) exit $status
diff /tmp/${CMD}-${PublicOrPrivate}.yaml  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}.yaml
diff /tmp/${CMD}-${PublicOrPrivate}.sh  ${TEMPLATEFLDR}/${CMD}-${PublicOrPrivate}.sh
echo $DIVIDER

set CMD=subnets
set PublicOrPrivate=private
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
set PublicOrPrivate=public
echo ${CMD}
eval "$RUNTESTCMD ${VERBOSE} ${CMD}-gen ${JOBSET} -${PublicOrPrivate} --no-quote ${OFFLINE}"
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

