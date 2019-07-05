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
public final class CmdProcessor
{
    public static final String CLASSNAME = CmdProcessor.class.getName();

    public static final String CFNINIT_PACKAGES = "AWS-CFNInit-Standup";
    public static final String EC2INSTANCETYPE = "EC2InstanceType";
    public static final String EC2IAMROLES = "MyIAM-roles";

    //-----------------------------
    public boolean verbose;
    protected CmdInvoker cmdinvoker;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public CmdProcessor( final CmdInvoker _cmdInvoker ) {
        this.verbose = _cmdInvoker.verbose;
        this.cmdinvoker = _cmdInvoker;
    }

    //=================================================================================
    private LinkedHashMap<String, Properties> getAllPropsRef() {
        return this.cmdinvoker.getMemoryAndContext().getAllPropsRef();
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // *  @return YAML that should be the complete CloudFormation YAML
    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _cfnJobType a NotNull String (created by {@link BootstrapAndChecks#getCFNJobType})
     *  @param _awscfnhome a NotNull String (typically, obtained as: new BootstrapAndChecks().awscfnhome)
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genYAML( final CmdLineArgs _cmdLA, final String _cfnJobType, final String _awscfnhome ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genYAML(..,"+ _cfnJobType +",..): ";

        String batchFilePath = null;
        {
            // String[] batchcmdargs = null;
            // switch ( _cmdLA.getCmdName() ) {
            // case VPC:
            //     // batchcmdargs =  processor.genVPCCmdLine( _cmdLA, _boot );
            //     // final String[] batchcmdargs = { "--batch",
            //     //                                 "@"+ _boot.awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt",
            //     //                                 "-i", "/dev/null", // dummy value for '-i'
            //     //                                 "-o", "/dev/null" // this '-o' does Not matter, as we'll be getting the output as the return-value of batcher.go()
            //     //                             }; // without the '-i' and '-o' the 'claBatch.parse()' will fail below.
            //     break;
            // }
            //-------------------------------------
            // final CmdLineArgsBatchCmd claBatch = new CmdLineArgsBatchCmd( batchcmdargs, org.ASUX.yaml.Enums.CmdEnum.BATCH, CmdLineArgsBasic.BATCHCMD[0], CmdLineArgsBasic.BATCHCMD[1], CmdLineArgsBasic.BATCHCMD[2], 1, "BatchFileName" );  // Note: there's a trick in the parameter-string.. as setArgName() assumes a single 'word' and puts a '<' & '>' around that single-word.
            // claBatch.parse( batchcmdargs );
            // claBatch.verbose = this.verbose;
            // claBatch.quoteType = _cmdLA.quoteType;
            // claBatch.YAMLLibrary = _cmdLA.YAMLLibrary;
        } // block with 100% only commented out OLD-CODE

        //-------------------------------------
        switch ( _cmdLA.getCmdName() ) {
            case VPC:
            case SGSSH:
            case EC2PLAIN:
            case FULLSTACK:
                            batchFilePath = "@"+ _awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt";
                            break;
            case SUBNET:    final Properties globalProps = this.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
                            assertTrue( _cmdLA.publicOrPrivateSubnet != null && _cmdLA.publicOrPrivateSubnet.length() > 0 ); // CmdLineArgs.class guarantees that it will be 'public' or 'private', if NOT NULL.
                            globalProps.setProperty( "PublicOrPrivate", _cmdLA.publicOrPrivateSubnet );
                            if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

                            batchFilePath = "@"+ _awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-"+_cmdLA.publicOrPrivateSubnet+"-Create.ASUX-batch.txt";
                            break;
            // case SGEFS:     batchFilePath = ;       break;
            // case VPNCLIENT: batchFilePath = ;       break;

            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                            System.err.println( es );
                            throw new Exception( es );
        }

        //-------------------------------------
        // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --batch command.
        // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
        //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
        // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below 3 lines
        final BatchCmdProcessor batcher = new BatchCmdProcessor( _cmdLA.verbose, /* showStats */ false, _cmdLA.quoteType, this.cmdinvoker.dumperopt );
        batcher.setMemoryAndContext( this.cmdinvoker.getMemoryAndContext() ); // this will invoke.. batcher.initProperties()
        if ( _cmdLA.verbose ) new org.ASUX.common.Debug(_cmdLA.verbose).printAllProps( HDR +" FULL DUMP of propsSetRef = ", this.getAllPropsRef() );

        final Node emptyInput = NodeTools.getEmptyYAML( this.cmdinvoker.dumperopt );
        final Node outpData2 = batcher.go( batchFilePath, emptyInput );
        if ( this.verbose ) System.out.println( HDR +" outpData2 =" + outpData2 +"\n\n");

        final String outpfile   = "/tmp/"+ _cfnJobType +".yaml";
        InputsOutputs.saveDataIntoReference( "@"+ outpfile, outpData2, null, this.cmdinvoker.getYamlWriter(), this.cmdinvoker.dumperopt, _cmdLA.verbose );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // *  @param _boot a NotNull instance (created within {@link CmdInvoker#processCommand})
    // *  @return the cmdline string representing running a batch-yaml script like: ${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}

    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _envParams a NotNull object (created by {@link BootstrapAndChecks#exec})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genCFNShellScript( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";
        final String outpfile   = "/tmp/"+ _envParams.cfnJobTYPE +".yaml";
        final String scriptfile = "/tmp/"+ _envParams.cfnJobTYPE +".sh";

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        // final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}--${ASUX::JobSetName}${ASUX::ItemNumber}", this.allPropsRef );
        final String MyStackNamePrefix = globalProps.getProperty( "MyStackNamePrefix" );

        String preStr = null;
        switch ( _cmdLA.getCmdName() ) {
        case VPC:       preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-VPC  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey="+EnvironmentParameters.MYVPCSTACKPREFIX+",ParameterValue=${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"} --template-body file://"+ outpfile;
                        break;
        case SUBNET:    preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-subnets-"+ _cmdLA.publicOrPrivateSubnet +"-"+ _cmdLA.jobSetName + _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --template-body file://"+ outpfile;
                        break;
        case SGSSH:     preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-"+ _cmdLA.jobSetName +"-SG-SSH"+ _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey=MyVPC,ParameterValue=${ASUX::VPCID} --template-body file://"+ outpfile;
                        break;
        case EC2PLAIN:  // final String AMIIDCachePropsFileName = Macros.evalThoroughly( this.verbose, _awscfnhome +"/config/inputs/AMZNLinux2_AMI_ID-${ASUX::AWSLocation}.txt", this.getAllPropsRef() );
                        // final Properties AMIIDCacheProps = org.ASUX.common.Utils.parseProperties( "@"+ AMIIDCachePropsFileName );
                        // this.allPropsRef.put( "AMIIDCache", AWSRegionLocations );
                        // final String AMIIDCachePropsFileName = Macros.evalThoroughly( this.verbose, "AMZNLinux2_AMI_ID-${ASUX::AWSLocation}.txt", this.getAllPropsRef() );
                        final String AMIIDCachePropsFileName = "AMZNLinux2_AMI_ID-"+ _envParams.AWSLocation +".txt";
                        try {
                            BootstrapAndChecks.fileCheck( _envParams.awscfnhome +"/config/inputs", AMIIDCachePropsFileName );
                            globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _envParams.awscfnhome +"/config/inputs/"+ AMIIDCachePropsFileName ) );
                            // Will contain a SINGLE row like:-     AWSAMIID=ami-084040f99a74ce8c3
                        } catch (Exception e) {
                            if ( e.getMessage().startsWith("ERROR! File missing/unreadable/empty") ) {
                                final String EC2AMI_AMZN2Linux_LookupKey = Macros.evalThoroughly( this.verbose, "${ASUX::EC2AMI_AMZN2Linux_LookupKey}", this.getAllPropsRef() );
                                // The ABOVE 'EC2AMI_AMZN2Linux_LookupKey' is typically defined in AWSCFNHOME/config/DEFAULTS/job-DEFAULTS.propertied
                                System.out.println( HDR +"Need your _MANUAL_HELP_ in Querying AWS to figure out .. what the AMI-ID for "+ EC2AMI_AMZN2Linux_LookupKey +" is, in the Location "+ _envParams.AWSLocation +"." );
                                System.out.println( HDR +"aws ssm get-parameters --names '/aws/service/ami-amazon-linux-latest/"+ EC2AMI_AMZN2Linux_LookupKey +"' --region "+ _envParams.AWSRegion +" --profile ${AWSprofile} --output json" );
                                System.out.println( HDR +"aws ssm get-parameters --names '/aws/service/ami-amazon-linux-latest/"+ EC2AMI_AMZN2Linux_LookupKey +"' --region "+ _envParams.AWSRegion +" --profile ${AWSprofile} --output json > /tmp/o.json" );
                                System.out.println( HDR +"asux yaml batch 'useAsInput @/tmp/o.json ; yaml --read Parameters,0,Value --delimiter ,' --no-quote -i /dev/null -o '"+ _envParams.awscfnhome +"/config/inputs"+ AMIIDCachePropsFileName +"'" );
                                System.exit(111);
                                return;

                            } else
                                throw e;
                        } // try-catch

                        final String DefaultPublicSubnet1 = MyStackNamePrefix + "-Subnet-1-ID";// Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-Subnet-1-ID", this.getAllPropsRef()  );
                        final String MySSHSecurityGroup = MyStackNamePrefix + "-SG-SSH"; // Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-SG-SSH", this.allPropsRef  );
                        final String MySSHKeyName = Macros.evalThoroughly( this.verbose, "${ASUX::AWSLocation}-${ASUX::MyOrgName}-${ASUX::MyEnvironment}-LinuxSSH.pem", _envParams.getAllPropsRef() );
                        // final String MyIamInstanceProfiles = Macros.evalThoroughly( this.verbose, "${ASUX::?????????????????}-"+HDR, this.allPropsRef );
                        // 'MyIamInstanceProfiles' must be set within one of the JOB files (like Job-Master.properties or Job-ec2plain.properties)
                        // globalProps.setProperty( "DefaultPublicSubnet1", DefaultPublicSubnet1 );
                        // globalProps.setProperty( "MySSHSecurityGroup", MySSHSecurityGroup );
                        // globalProps.setProperty( "MySSHKeyName", MySSHKeyName );
                        if (this.verbose) System.out.println( HDR + "DefaultPublicSubnet1=" + DefaultPublicSubnet1 + " MySSHSecurityGroup=" + MySSHSecurityGroup + " MySSHKeyName=" + MySSHKeyName );

                        final String params =
                                    " ParameterKey=MyPublicSubnet1,ParameterValue="+ DefaultPublicSubnet1 +
                                    " ParameterKey=MySSHSecurityGroup,ParameterValue="+ MySSHSecurityGroup +
                                    " ParameterKey=MyIamInstanceProfiles,ParameterValue=${ASUX::"+ EC2IAMROLES +"}" +
                                    " ParameterKey=AWSAMIID,ParameterValue=${ASUX::AWSAMIID} "+
                                    " ParameterKey=EC2InstanceType,ParameterValue=${ASUX::EC2InstanceType} " +
                                    " ParameterKey=MySSHKeyName,ParameterValue=" + MySSHKeyName;

                        preStr ="aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-"+ _cmdLA.jobSetName +"-EC2-${ASUX::"+EnvironmentParameters.MYEC2INSTANCENAME+"}"+ _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} "+
                                "--profile ${AWSprofile} --parameters "+ params +" --template-body file://"+ outpfile;

                        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose );
                        final List keys = awssdk.listKeyPairEC2   ( _envParams.AWSRegion, MySSHKeyName );
                        if ( keys.size() > 0 ) {
                            if (this.verbose) System.out.println( HDR + "The key exists with the name: "+ MySSHKeyName );
                        } else {
                            if (this.verbose) System.out.println( HDR + "Will CREATE NEW SSH-login KeyPair with name: "+ MySSHKeyName );
                            awssdk.deleteKeyPairEC2 ( _envParams.AWSRegion, MySSHKeyName ); // just to be sure - and no other reason, that the create should succeed.
                            final String keyPairMaterial = awssdk.createKeyPairEC2 ( _envParams.AWSRegion, MySSHKeyName );
                            // "aws ec2 delete-key-pair --region ${AWSRegion} --profile \${AWSprofile} --key-name ${MySSHKeyName} "
                            // "aws ec2 create-key-pair --region ${AWSRegion} --profile \${AWSprofile} --key-name ${MySSHKeyName} > ~/.aws/${MySSHKeyName}"
                            final String homedir = System.getProperty("user.home");
                            assertTrue( homedir != null );
                            final File awsuserhome = new File( homedir +"/.aws" );
                            awsuserhome.mkdirs();
                            final String localKeyPairFilePath = awsuserhome +"/"+ MySSHKeyName;
                            write2File( localKeyPairFilePath, keyPairMaterial );
                            org.ASUX.common.IOUtils.setFilePerms( this.verbose, localKeyPairFilePath, true, true, false, true ); // rw------- file-permissions
                        }

                        // https://www.brautaset.org/articles/2017/route-53-cloudformation.html
                        // REFERENCE: https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/aws-properties-route53-recordset.html
                        // https://medium.com/boltops/a-simple-introduction-to-aws-cloudformation-part-2-d6d95ed30328

                        break;
        case FULLSTACK:
                        // read AWS,MyOrgName --delimiter ,              (put it into GLOBAL.PROPERTIES)
                        // read AWS,MyEnvironment --delimiter ,          (put it into GLOBAL.PROPERTIES)
                        // read AWS,AWSRegion --delimiter ,              (put it into GLOBAL.PROPERTIES)
                        // read AWS,VPC,VPCName --delimiter ,           else: a DEFAULT value based on {MyOrgName}-{MyEnvironment}-{AWSLocation}-{user.name} ..  (put them into GLOBAL.PROPERTIES)
                        // read AWS,VPC,subnet  --delimiter ,           to get _ANY_ KV-Pairs for subnet (put them into GLOBAL.PROPERTIES)
                        // read AWS,VPC,subnet,SERVERS --delimiter ,     iterate over how many ever such elements exist
                        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName> --delimiter ,
                        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,EC2InstanceType --delimiter ,
                        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,IAM-roles --delimiter ,
                        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,yum --delimiter ,
                        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,rpm --delimiter ,
                        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,configCustomCommands --delimiter ,
                        //-------------------------------------
                        ReadYamlEntry readcmd = null;
                        Node output = null;
                        final String fullStackJob_Filename = _cmdLA.jobSetName + ".yaml";
                        if ( this.verbose ) System.out.println( HDR +" about to read file '" + fullStackJob_Filename + "'" );
                        // final Node emptyInput = NodeTools.getEmptyYAML( this.cmdinvoker.dumperopt );

                        final java.io.InputStream is1 = new java.io.FileInputStream( fullStackJob_Filename );
                        final java.io.Reader filereader = new java.io.InputStreamReader(is1);
                        final GenericYAMLScanner yamlscanner = new GenericYAMLScanner( _cmdLA.verbose );
                        yamlscanner.setYamlLibrary( YAML_Libraries.NodeImpl_Library );
                        final Node inputNode = yamlscanner.load( filereader );
                        if ( this.verbose ) System.out.println( HDR +" file contents= '" + NodeTools.Node2YAMLString( inputNode ) + ".yaml'");

                        //-------------------------------------
                        // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --read YAML-command.
                        // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
                        //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
                        // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below method
                        readcmd = new ReadYamlEntry( _cmdLA.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );
                        final String MyOrgName      = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,MyOrgName" );
                        final String MyEnvironment  = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,MyEnvironment" );
                        final String AWSRegion      = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,AWSRegion" );
                        final String VPCName        = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,VPC,VPCName" );

                        readcmd.searchYamlForPattern( inputNode, "AWS,VPC,subnet", "," );
                        final Node subnet = readcmd.getOutput();
                        if ( this.verbose ) System.out.println( HDR +" subnet YAML-tree =\n" + NodeTools.Node2YAMLString( subnet ) +"\n" );

                        readcmd.searchYamlForPattern( subnet, "subnet,SERVERS", "," );
                        final Node servers = readcmd.getOutput();
                        if ( this.verbose ) System.out.println( HDR +" SERVERS YAML-tree =\n" + NodeTools.Node2YAMLString( servers ) +"\n" );

                        String ec2instanceName = null;
                        if ( servers instanceof MappingNode ) {
                            //       SERVERS:
                            //          OrgASUXplayEC2plain: ### This is the name of the 1st SERVER.
                            //              ..

                            final MappingNode mapNode = (MappingNode) servers;
                            final java.util.List<NodeTuple> tuples = mapNode.getValue();
                            // if ( this.verbose ) System.out.println( HDR +" SERVERS YAML-tree =\n" + NodeTools.Node2YAMLString( mapNode ) +"\n" );
                            for( NodeTuple kv: tuples ) {
                                final Node keyNode = kv.getKeyNode();
                                assertTrue( keyNode instanceof ScalarNode );
                                // @SuppressWarnings("unchecked")
                                final ScalarNode scalarKey = (ScalarNode) keyNode;
                                ec2instanceName = scalarKey.getValue();
                                final Node valNode = kv.getValueNode();
                                if ( valNode instanceof MappingNode ) {
                                    parseServerInfo( (MappingNode) valNode, readcmd, _envParams, "Standup" );  // ????????????????????? Need to parametrize "Standup"
                                } else {
                                    if ( this.verbose ) System.out.println( HDR +" (servers["+ ec2instanceName +"] instanceof MappingNode) is Not a mapping within:\n" + NodeTools.Node2YAMLString( mapNode ) + "\n");
                                    throw new Exception( "Invalid Node of type: "+ valNode.getNodeId() );
                                }
                            }

                        } else if ( servers instanceof SequenceNode ) {
                            //       SERVERS:
                            //        -   ### Sequence of UNNAMED servers
                            //            ..  ..
                            final SequenceNode seqNode = (SequenceNode) servers;
                            final java.util.List<Node> seqs = seqNode.getValue();
                            assertTrue( seqs.size() >= 1 );
                            int ix = 0;
                            for( Node seqItem: seqs ) {
                                if ( seqItem instanceof MappingNode ) {
                                    parseServerInfo( (MappingNode) seqItem, readcmd, _envParams, "??"  );
                                } else {
                                    if ( this.verbose ) System.out.println( HDR +" (servers["+ ix +"] instanceof SequenceNode) failed with:\n" + NodeTools.Node2YAMLString( seqItem ) + "\n");
                                    throw new Exception( "Invalid Node of type: "+ seqItem.getNodeId() );
                                }
                                ec2instanceName = Integer.toString( ix + 1 );
                                ix ++;
                            }
                        } else { // !  (servers instanceof MappingNode)   &&   !  (servers instanceof SequenceNode) )
                            throw new Exception( "the content of "+ fullStackJob_Filename +" at the YAML-path: 'AWS,VPC,subnet,SERVERS' is neither MappingNode nor SequenceNode" );
                        }

                        assertTrue( ec2instanceName != null );
                        globalProps.setProperty( EnvironmentParameters.MYEC2INSTANCENAME, ec2instanceName );

                        //-------------------------------------
                        // _cmdLA.verbose       <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
                        // _cmdLA.quoteType     <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
                        // _cmdLA.jobSetName    <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
                        // _cmdLA.publicOrPrivateSubnet <-- SAME VALUE FOR ALL CMDs (as other commands will IGNORE this)

//                         final CmdLineArgs claVPC = deepCloneWithChanges( _cmdLA, Enums.GenEnum.VPC, null );
//                         genCFNShellScript( claVPC, _envParams );
// ??????? For each subnet in jobSetName.yaml ...
//                         final CmdLineArgs claSubnet = deepCloneWithChanges( _cmdLA, Enums.GenEnum.SUBNET, ???????? );
//                         genCFNShellScript( claSubnet, _envParams );
// ??????? For each SG in jobSetName.yaml ...
//                         final CmdLineArgs claSGSSH = deepCloneWithChanges( _cmdLA, Enums.GenEnum.SGSSH, null );
//                         genCFNShellScript( claSGSSH, _envParams );
// ??????? For each SERVER in jobSetName.yaml ...
//                         final CmdLineArgs claEC2 = deepCloneWithChanges( _cmdLA, Enums.GenEnum.EC2PLAIN, ????????? );
//                         genCFNShellScript( claEC2, _envParams );

                        // final String outpfile   = "/tmp/"+ _cfnJobType +".yaml";
                        // InputsOutputs.saveDataIntoReference( "@"+ outpfile, outpData2, null, this.cmdinvoker.getYamlWriter(), this.cmdinvoker.dumperopt, _cmdLA.verbose );
                        break;
        case VPNCLIENT:
        case SGEFS:
        case UNDEFINED:
        default:    final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                    System.err.println( es );
                    throw new Exception( es );
        }

        //----------------------------
        final String postStr = Macros.evalThoroughly( this.verbose, preStr, this.getAllPropsRef() );
        if ( this.verbose ) System.out.println( postStr ); // dump the cmd to execute the CFN script to stdout.
        write2File( scriptfile, postStr );
        org.ASUX.common.IOUtils.setFilePerms( this.verbose, scriptfile, true, true, true, true ); // rwx------ file-permissions
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    private void write2File( final String filename, final String content) throws Exception
    {
        final String HDR = CLASSNAME + ": write2File("+ filename +", <content>): ";
        try {
            java.nio.file.Files.write(   java.nio.file.Paths.get( filename ),   content.getBytes()  );
            System.out.println( "File "+ filename +" created." );
        // } catch(IOException ioe) {
        // } catch(IllegalArgumentException ioe) { // thrown by java.nio.file.Paths.get()
        // } catch(FileSystemNotFoundException ioe) { // thrown by java.nio.file.Paths.get()
        } catch(java.nio.file.InvalidPathException ipe) {
            // if ( this.verbose ) ipe.printStackTrace( System.err );
            // if ( this.verbose ) System.err.println( "\n\n"+ HDR +"Serious internal error: Why would the Path be invalid?" );
            ipe.printStackTrace( System.err );
            System.err.println( "\n\n"+ HDR +"!!SERIOUS INTERNAL ERROR!! Why would the Path '"+ filename +"' be invalid?\n\n" );
            throw ipe;
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Assumes that the Node 'YAML-tree' passed in actually either a simple ScalarNode, or a SequenceNode with just one ScalarNode.</p>
     *  <p>If its not a valid assumption, either an Exception or an Assertion-RuntimeException is thrown</p>
     *  @param _n a NotNull Node object
     *  @return a simple String
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided.
     */
    private String getOnlyContent( final Node _n ) throws Exception {
        final String HDR = CLASSNAME + ": getOnlyContent(): ";
        if ( this.verbose ) System.out.println( HDR +" provided argument =\n" + NodeTools.Node2YAMLString( _n ) + "\n");
        assertTrue( _n != null );
        if ( _n instanceof ScalarNode ) {
            final ScalarNode scalar = (ScalarNode) _n;
            return scalar.getValue();
        } else if ( _n instanceof SequenceNode ) {
            final SequenceNode seqNode = (SequenceNode) _n;
            final java.util.List<Node> seqs = seqNode.getValue();
            assertTrue( seqs.size() == 1 );
            assertTrue( seqs.get(0) instanceof ScalarNode );
            final ScalarNode scalar = (ScalarNode) seqs.get(0);
            return scalar.getValue();
        } else {
            throw new Exception( "Invalid Node of type: "+ _n.getNodeId() );
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Common code refactored into a utility private method.  Given the YAML from the 'Jobfile.yaml', read the various YAML-entries like 'AWS,MyOrgName', 'AWS,MyEnvironment', ..
     * @param _inputNode NotNull Node object
     * @param _readcmd a NotNull instance
     * @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String
     * @return a Not-Null String (or else a runtime-assertion-exception is thrown, as determined within {@link #getOnlyContent}
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    private String readStringFromFullStackJobConfig( final Node _inputNode, final ReadYamlEntry _readcmd, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readStringFromFullStackJobConfig(<Node>, "+ _YAMLPath +"): ";
        _readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final Node output = _readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );
        final String s = getOnlyContent( output );
        return s;
    }

    /**
     *  Common code refactored into a utility private method.  Given the YAML from the 'Jobfile.yaml', read the various YAML-entries like 'AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,yum', 'AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,configCustomCommands'..
     *  @param _inputNode NotNull Node object
     *  @param _readcmd a NotNull instance
     *  @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String
     *  @return a Not-Null SequenceNode (or else a runtime-assertion-exception is thrown, as determined within {@link #getOnlyContent}
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    private SequenceNode readNodeFromFullStackJobConfig( final Node _inputNode, final ReadYamlEntry _readcmd, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readNodeFromFullStackJobConfig(<Node>, "+ _YAMLPath +"): ";
        _readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final SequenceNode output = _readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );
        return output;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Extract YAML-sub-trees from the 1st argument, and save them into "MemoryAndContext" for the 'AWSCFN-ec2plain-Create.ASUX-batch.txt' YamlBatchScript to use.</p>
     *  <p>Make sure the 'labels' for what's put in memory matches what is 'recalled' within the 'AWSCFN-ec2plain-Create.ASUX-batch.txt' YamlBatchScript</p>
     *  @param _mapNode should be the YAML-sub-tree determined by: read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName> --delimiter ,
     *  @param _readcmd a NotNull instance
     *  @param _envParams a NotNull object (created by {@link BootstrapAndChecks#exec})
     *  @param _cfnInitContext typically, it's one of the AWS cfn-init ConfigSets (StandupOnly, StandUpInstallAndRun, ..)
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    private void parseServerInfo( final MappingNode _mapNode, final ReadYamlEntry _readcmd, final EnvironmentParameters _envParams, final String _cfnInitContext ) throws Exception {
        final String HDR = CLASSNAME + ": parseServerInfo(<mapNode>,"+ _cfnInitContext +"): ";
        final Properties globalProps = this.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );

        //-----------------
        final String EC2InstanceType      = readStringFromFullStackJobConfig( _mapNode, _readcmd, "EC2InstanceType" );
        globalProps.setProperty( EC2INSTANCETYPE, EC2InstanceType );  // <<----------- <<-------------

        //-----------------
        final Node IAMRoles      = readNodeFromFullStackJobConfig( _mapNode, _readcmd, EC2IAMROLES );

        if ( IAMRoles instanceof ScalarNode ) {
            final ScalarNode scalar = (ScalarNode) IAMRoles;
            globalProps.setProperty( EC2IAMROLES, scalar.getValue() );
        } else if ( IAMRoles instanceof SequenceNode ) {
            final SequenceNode seqNode = (SequenceNode) IAMRoles;
            final java.util.List<Node> seqs = seqNode.getValue();
            assertTrue( seqs.size() >= 1 );
            StringBuffer strBuf = null;
            if ( seqs.get(0) instanceof ScalarNode ) {
                for ( Node n: seqs ) {
                    final ScalarNode scalar = (ScalarNode) n;
                    if (strBuf == null)
                        strBuf = new StringBuffer( scalar.getValue() );
                    else
                        strBuf.append( ", "+ scalar.getValue() );
                }
                globalProps.setProperty( EC2IAMROLES, strBuf.toString() );  // <<----------- <<-------------
            } else {
                if ( this.verbose ) System.out.println( HDR +" IAMRoles failed with:\n" + NodeTools.Node2YAMLString( IAMRoles ) + "\n");
                throw new Exception( "Invalid Node of type: "+ seqs.get(0).getNodeId() );
            }
        } else {
            if ( this.verbose ) System.out.println( HDR +" IAMRoles failed with:\n" + NodeTools.Node2YAMLString( IAMRoles ) + "\n");
            throw new Exception( "Invalid Node of type: "+ IAMRoles.getNodeId() );
        }

        //-----------------
        final SequenceNode yum      = readNodeFromFullStackJobConfig( _mapNode, _readcmd, "yum" );
        final SequenceNode rpm      = readNodeFromFullStackJobConfig( _mapNode, _readcmd, "rpm" );
        final SequenceNode configCustomCommands = readNodeFromFullStackJobConfig( _mapNode, _readcmd, "configCustomCommands" );
        // final Node parent   = NodeTools.getNewSingleMap( "Packages", "", this.cmdinvoker.dumperopt );
        final java.util.List<NodeTuple> tuples = new LinkedList<NodeTuple>();
        if ( yum != null && yum.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "yum", null, null, this.cmdinvoker.dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, yum );
            tuples.add( tuple );
        }
        if ( rpm != null && rpm.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "rpm", null, null, this.cmdinvoker.dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, rpm );
            tuples.add( tuple );
        }
        if ( configCustomCommands != null && configCustomCommands.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "configCustomCommands", null, null, this.cmdinvoker.dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, configCustomCommands );
            tuples.add( tuple );
        }

        final MappingNode parentMapN = new MappingNode ( Tag.MAP, false, tuples, null, null, this.cmdinvoker.dumperopt.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK

        //-----------------
        // now.. create the topmost '_cfnInitContext: ' YAML entry
        final java.util.List<NodeTuple> tuples2 = new LinkedList<NodeTuple>();
        final ScalarNode keyN2 = new ScalarNode( Tag.STR, _cfnInitContext, null, null, this.cmdinvoker.dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
        final NodeTuple tuple2 = new NodeTuple( keyN2, parentMapN );
        tuples2.add( tuple2 );
        final MappingNode superParentMapN = new MappingNode ( Tag.MAP, false, tuples2, null, null, this.cmdinvoker.dumperopt.getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK

        //-----------------
        this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( CFNINIT_PACKAGES, superParentMapN );   // <<----------- <<-------------
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( CFNINIT_PACKAGES +".yum", yum );
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( CFNINIT_PACKAGES +".rpm", rpm );
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( CFNINIT_PACKAGES +".configCustomCommands", configCustomCommands );

    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // // *  @return YAML that should be the complete CloudFormation YAML
    // /**
    //  *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
    //  *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
    //  *  @param _cla a NotNull instance (created within {@link CmdInvoker#processCommand})
    //  *  @param _boot a NotNull instance (created within {@link CmdInvoker#processCommand})
    //  *  @return the cmdline string representing running a batch-yaml script like: ${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
    //  *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
    //  *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
    //  */
    // public String[] genVPCCmdLine( final CmdLineArgs _cla, final BootstrapAndChecks _boot ) throws IOException, Exception
    // {
    //     // final String HDR = CLASSNAME + ": genVPC(): ";
    //     //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
    //     final String cfnJobType = _boot.getCFNJobType( _cla.cmdName );
    //     // final String outpfile   = "/tmp/"+ cfnJobType;

    //     final String[] batchcmdargs = { "--batch",
    //                     "@"+ _boot.awscfnhome +"/bin/AWSCFN-"+cfnJobType+"-Create.ASUX-batch.txt",
    //                     "-i", "/dev/null",
    //                     "-o", "/dev/null" // this '-o' does Not matter, as we'll be getting the output as the return-value of batcher.go()
    //                 };
    //     // final ArrayList<String> arrO = new ArrayList<String>( somebatchcmdargs );

    //     // if ( this.verbose ) arrO.add( 0, "--verbose" );
    //     // switch (_cla.quoteType) {
    //     //     case DOUBLE_QUOTED:  arrO.add( "--double-quote" );  break;
    //     //     case SINGLE_QUOTED:  arrO.add( "--single-quote" );  break;
    //     //     case PLAIN:          arrO.add( "--no-quote" );      break;
    //     //     case LITERAL:        break;
    //     //     case FOLDED:         break;
    //     //     case UNDEFINED:      break;
    //     //     default:    break;
    //     // }

    //     // final String[] batchCmdArgStrArr = arrO.toArray( somebatchcmdargs );
    //     // I can't pass thru this.allPropsRef  to the batch command below..
    //     // org.ASUX.yaml.Cmd.main( arrO.toArray( batchcmdargs ) );

    //     return batchcmdargs;
    // }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
