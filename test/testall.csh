#!/bin/tcsh -f

###------------------------------
echo "Usage: $0 [--verbose]"
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
set ORGASUXFLDR=/mnt/development/src/org.ASUX
set path=( $path ${ORGASUXFLDR} )

set TESTSRCFLDR=${ORGASUXFLDR}/AWS/CFN/test

###------------------------------
if ( $#argv == 1 && "$1" == "--verbose" ) then
        set VERBOSE="--verbose"
        shift
else
        set VERBOSE=""
endif

chdir ${TESTSRCFLDR}
if ( "$VERBOSE" != "" ) pwd

###------------------------------
# echo -n "Sleep interval? >>"; set DELAY=$<
# if ( "$DELAY" == "" ) set DELAY=2

set TEMPLATEFLDR=${TESTSRCFLDR}/outputs
set OUTPUTFLDR=/tmp/test-output-AWS

\rm -rf ${OUTPUTFLDR}
mkdir -p ${OUTPUTFLDR}

###------------------------------
set JARFLDR=${ORGASUXFLDR}/lib

#_____ ${JARFLDR}/org.asux.aws-sdk.aws-sdk-1.0.jar
#_____ ${JARFLDR}/org.asux.yaml.nodeimpl.yaml.nodeimpl-1.0.jar
#_____ ${JARFLDR}/org.asux.yaml.yaml-1.0.jar
#_____ ${JARFLDR}/org.asux.yaml.collectionsimpl.yaml.collectionsimpl-1.0.jar

set COMMONSCLIJAR=${JARFLDR}/commons-cli.commons-cli.commons-cli-1.4.jar
#___ commons-cli-1.4.jar
set JUNITJAR=${JARFLDR}/junit.junit.junit-4.8.2.jar

set ASUXCOMMON=${JARFLDR}/org.asux.common.common-1.0.jar
set ASUXCOMMON=/Users/Sarma/.m2/repository/org/asux/common/1.0/common-1.0.jar
set ASUXYAML=${JARFLDR}/org.asux.yaml.yaml-1.0.jar
set ASUXYAML=/Users/Sarma/.m2/repository/org/asux/yaml/1.0/yaml-1.0.jar
set ASUXAWSCFN=${JARFLDR}/org.asux.aws-cfn.aws-cfn-1.0.jar
set ASUXAWSCFN=/Users/Sarma/.m2/repository/org/asux/aws-cfn/1.0/aws-cfn-1.0.jar

if ( $?CLASSPATH ) then
        setenv CLASSPATH  ${CLASSPATH}:${ASUXAWSCFN}:${ASUXYAML}:${ASUXCOMMON}:${COMMONSCLIJAR}:${JUNITJAR}
else
        setenv CLASSPATH  ${ASUXAWSCFN}:${ASUXYAML}:${ASUXCOMMON}:${COMMONSCLIJAR}:${JUNITJAR}
endif

if ( "$VERBOSE" != "" ) echo $CLASSPATH

###---------------------------------
set noglob ### Very important to allow us to use '*' character on cmdline arguments
set noclobber

set TESTNUM=1

###---------------------------------
# 1
# set OUTPFILE=${OUTPUTFLDR}/test-${TESTNUM}
# echo $OUTPFILE
# java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} ??????  #_____ >&! ${OUTPFILE}
# diff ${OUTPFILE} ${TEMPLATEFLDR}/test-${TESTNUM}
# if ($status != 0) exit $status

###---------------------------------
# 2
echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --vpc-gen simple
if ($status != 0) exit $status
echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --subnets-gen simple private
if ($status != 0) exit $status
echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --sg-ssh-gen simple
if ($status != 0) exit $status
echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --sg-efs-gen simple
if ($status != 0) exit $status
echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --vpnclient-gen simple
if ($status != 0) exit $status
echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --ec2plain-gen simple
if ($status != 0) exit $status

echo -n .
java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --ec2plain-gen simple 2ndinstance
if ($status != 0) exit $status

### Error situations
echo
echo -n 'Error conditions now.. (Y/N) :>'; set ANS=$<
if ( "$ANS" == "Y" ||  "$ANS" == "y" ) then
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} unknownCmd-gen simple
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --subnets-gen simple
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --sgssh-gen simple
        java -cp ${CLASSPATH} org.ASUX.AWS.CFN.CmdLineArgs ${VERBOSE} --unknownCmd-gen simple sdfasd asdf asdfa
endif

#EoInfo

