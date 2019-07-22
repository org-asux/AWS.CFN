/*
 BSD 3-Clause License
 
 Copyright (c) 2019, Udaybhaskar Sarma Seetamraju
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
 
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
 
 * Neither the name of the copyright holder nor the names of its
 contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ASUX.AWS.CFN;

import org.ASUX.common.Macros;

import org.ASUX.yaml.YAML_Libraries;

import org.ASUX.YAML.NodeImpl.ReadYamlEntry;
import org.ASUX.YAML.NodeImpl.BatchCmdProcessor;
import org.ASUX.YAML.NodeImpl.NodeTools;
import org.ASUX.YAML.NodeImpl.GenericYAMLScanner;
import org.ASUX.YAML.NodeImpl.GenericYAMLWriter;
import org.ASUX.YAML.NodeImpl.InputsOutputs;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.Mark; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/error/Mark.java
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * This enum class is a bit extensive, only because the ENNUMERATED VALUEs are strings.
 * For variations - see https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
 */
public final class CmdProcessorEC2
{
    public static final String CLASSNAME = CmdProcessorEC2.class.getName();

    public boolean verbose;
    protected CmdProcessor cmdProcessor;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public CmdProcessorEC2( final CmdProcessor _cmdProcessor ) {
        this.verbose = _cmdProcessor.verbose;
        this.cmdProcessor = _cmdProcessor;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @return a String (containing {ASUX::_} macros) that should be used to executed using BATCH-YAML-Processor within {@link CmdProcessor#genCFNShellScript}
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public String genYAMLBatchFile( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genYAML(): ";
        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );

        final String MyDomainName       = globalProps.getProperty( EnvironmentParameters.MYDOMAINNAME );
        if (this.verbose) System.out.println( HDR + "MyDomainName " + MyDomainName );
        final String Rt53HostedZoneId   = awssdk.getHostedZoneId( _envParams.getAWSRegion(), MyDomainName, "Public".equals(_cmdLA.PublicOrPrivate) );
        if (this.verbose) System.out.println( HDR + "MyDomainName " + MyDomainName + " Rt53HostedZoneId " + Rt53HostedZoneId  );
        globalProps.setProperty( EnvironmentParameters.MYRT53HOSTEDZONEID, Rt53HostedZoneId ); // will define ${ASUX::MyRt53HostedZoneId}

        final String batchFilePath = "@"+ _envParams.get_awscfnhome() +"/bin/AWSCFN-"+ _envParams.getCfnJobTYPEString() +"-Create.ASUX-batch.txt";
        return batchFilePath;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @return a String (containing {ASUX::_} macros) that should be used to executed using BATCH-YAML-Processor within {@link CmdProcessor#genCFNShellScript}
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public String genCFNShellScript( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final String MyStackNamePrefix = globalProps.getProperty( EnvironmentParameters.MYSTACKNAMEPREFIX );
        final String outpfile = CmdProcessor.getOutputFilePath( _cmdLA, _envParams, globalProps );  // _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +".yaml";

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );

        String preStr = null;
        // final String AMIIDCachePropsFileName = Macros.evalThoroughly( this.verbose, _awscfnhome +"/config/inputs/AMZNLinux2_AMI_ID-${ASUX::AWSLocation}.txt", _envParams.getAllPropsRef() );
        // final Properties AMIIDCacheProps = org.ASUX.common.Utils.parseProperties( "@"+ AMIIDCachePropsFileName );
        // this.getAllPropsRef().put( "AMIIDCache", AWSRegionLocations );
        // final String AMIIDCachePropsFileName = Macros.evalThoroughly( this.verbose, "AMZNLinux2_AMI_ID-${ASUX::AWSLocation}.txt", _envParams.getAllPropsRef() );
        final String AMIIDCachePropsFileName = "AMZNLinux2_AMI_ID-"+ _envParams.getAWSLocation() +".txt";
        try {
            // BootCheckAndConfig.fileCheck( _envParams.get_awssdkhome() +"/etc/offline-downloads", AMIIDCachePropsFileName, _envParams.bInRecursionByFullStack );
            globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _envParams.get_awssdkhome() +"/etc/offline-downloads/"+ AMIIDCachePropsFileName ) );
            // Will contain a SINGLE row like:-     AWSAMIID=ami-084040f99a74ce8c3
        } catch (Exception e) {
            if ( e.getMessage().startsWith("ERROR! File missing/unreadable/empty") ) {
                final String EC2AMI_AMZN2Linux_LookupKey = Macros.evalThoroughly( this.verbose, "${ASUX::EC2AMI_AMZN2Linux_LookupKey}", _envParams.getAllPropsRef() );
                // The ABOVE 'EC2AMI_AMZN2Linux_LookupKey' is typically defined in AWSCFNHOME/config/DEFAULTS/job-DEFAULTS.propertied
                System.out.println( HDR +"Need your _MANUAL_HELP_ in Querying AWS to figure out .. what the AMI-ID for "+ EC2AMI_AMZN2Linux_LookupKey +" is, in the Location "+ _envParams.getAWSLocation() +"." );
                System.out.println( HDR +"aws ssm get-parameters --names '/aws/service/ami-amazon-linux-latest/"+ EC2AMI_AMZN2Linux_LookupKey +"' --region "+ _envParams.getAWSRegion() +" --profile ${AWSprofile} --output json" );
                System.out.println( HDR +"aws ssm get-parameters --names '/aws/service/ami-amazon-linux-latest/"+ EC2AMI_AMZN2Linux_LookupKey +"' --region "+ _envParams.getAWSRegion() +" --profile ${AWSprofile} --output json > /tmp/o.json" );
                System.out.println( HDR +"asux yaml batch 'useAsInput @/tmp/o.json ; yaml --read Parameters,0,Value --delimiter ,' --no-quote -i /dev/null -o '"+ _envParams.get_awssdkhome() +"/etc/offline-downloads/"+ AMIIDCachePropsFileName +"'" );
                System.exit(111);
                return null;

            } else
                throw e;
        } // try-catch

        final String DefaultSubnet1wMacro = MyStackNamePrefix + "-Subnet-${ASUX::PublicOrPrivate}1-ID";// Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-Subnet-1-ID", _envParams.getAllPropsRef()  );
        final String DefaultSubnet1 = Macros.evalThoroughly( this.verbose, DefaultSubnet1wMacro, _envParams.getAllPropsRef() );
        final String MySSHSecurityGroup = MyStackNamePrefix + "-SG-SSH"; // Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-SG-SSH", this.getAllPropsRef()  );
        final String MySSHKeyName = Macros.evalThoroughly( this.verbose, "${ASUX::AWSLocation}-${ASUX::MyOrgName}-${ASUX::MyEnvironment}-LinuxSSH.pem", _envParams.getAllPropsRef() );
        // final String MyIamInstanceProfiles = Macros.evalThoroughly( this.verbose, "${ASUX::?????????????????}-"+HDR, this.getAllPropsRef() );
        // 'MyIamInstanceProfiles' must be set within one of the JOB files (like Job-Master.properties or Job-ec2plain.properties)
        // globalProps.setProperty( "DefaultSubnet1", DefaultSubnet1 );
        // globalProps.setProperty( "MySSHSecurityGroup", MySSHSecurityGroup );
        // globalProps.setProperty( "MySSHKeyName", MySSHKeyName );
        if (this.verbose) System.out.println( HDR + "DefaultSubnet1=" + DefaultSubnet1 + " MySSHSecurityGroup=" + MySSHSecurityGroup + " MySSHKeyName=" + MySSHKeyName );

        final String params =
                    " ParameterKey=My${ASUX::PublicOrPrivate}Subnet1,ParameterValue="+ DefaultSubnet1 +
                    " ParameterKey=MySSHSecurityGroup,ParameterValue="+ MySSHSecurityGroup +
                    " ParameterKey=MyIamInstanceProfiles,ParameterValue=${ASUX::"+ EnvironmentParameters.EC2IAMROLES +"}" +
                    " ParameterKey=AWSAMIID,ParameterValue=${ASUX::AWSAMIID} "+
                    " ParameterKey=EC2InstanceType,ParameterValue=${ASUX::EC2InstanceType} " +
                    " ParameterKey=MySSHKeyName,ParameterValue=" + MySSHKeyName;

        preStr ="aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-"+ _cmdLA.jobSetName +"-EC2-${ASUX::"+EnvironmentParameters.MYEC2INSTANCENAME+"}"+ _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} "+
                "--profile ${AWSprofile} --parameters "+ params +" --template-body file://"+ outpfile;

        final List keys = awssdk.listKeyPairEC2   ( _envParams.getAWSRegion(), MySSHKeyName );
        if ( keys.size() > 0 || _cmdLA.isOffline() ) {
            if (this.verbose) System.out.println( HDR + "The key exists with the name: "+ MySSHKeyName );
        } else {
            if (this.verbose) System.out.println( HDR + "Will CREATE NEW SSH-login KeyPair with name: "+ MySSHKeyName );
            awssdk.deleteKeyPairEC2 ( _envParams.getAWSRegion(), MySSHKeyName ); // just to be sure - and no other reason, that the create should succeed.
            final String keyPairMaterial = awssdk.createKeyPairEC2 ( _envParams.getAWSRegion(), MySSHKeyName );
            // "aws ec2 delete-key-pair --region ${AWSRegion} --profile \${AWSprofile} --key-name ${MySSHKeyName} "
            // "aws ec2 create-key-pair --region ${AWSRegion} --profile \${AWSprofile} --key-name ${MySSHKeyName} > ~/.aws/${MySSHKeyName}"
            final String homedir = System.getProperty("user.home");
            assertTrue( homedir != null );
            final File awsuserhome = new File( homedir +"/.aws" );
            awsuserhome.mkdirs();
            final String localKeyPairFilePath = awsuserhome +"/"+ MySSHKeyName;
            org.ASUX.common.IOUtils.write2File( localKeyPairFilePath, keyPairMaterial );
            org.ASUX.common.IOUtils.setFilePerms( this.verbose, localKeyPairFilePath, true, true, false, true ); // rw------- file-permissions
        }

        // https://www.brautaset.org/articles/2017/route-53-cloudformation.html
        // REFERENCE: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-route53-recordset.html
        // https://medium.com/boltops/a-simple-introduction-to-aws-cloudformation-part-2-d6d95ed30328

        return preStr;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
