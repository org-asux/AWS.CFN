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

import org.ASUX.YAML.NodeImpl.BatchCmdProcessor;
import org.ASUX.YAML.NodeImpl.NodeTools;
import org.ASUX.YAML.NodeImpl.GenericYAMLWriter;
import org.ASUX.YAML.NodeImpl.InputsOutputs;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
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

            // case SUBNET:
            // case SGSSH:
            // case SGEFS:
            // case VPNCLIENT:
            // case UNDEFINED:
            // case EC2PLAIN:
            // default:    final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
            //             System.err.println( es );
            //             throw new Exception( es );
            // }

            //-------------------------------------
            // final CmdLineArgsBatchCmd claBatch = new CmdLineArgsBatchCmd( batchcmdargs, org.ASUX.yaml.Enums.CmdEnum.BATCH, CmdLineArgsBasic.BATCHCMD[0], CmdLineArgsBasic.BATCHCMD[1], CmdLineArgsBasic.BATCHCMD[2], 1, "BatchFileName" );  // Note: there's a trick in the parameter-string.. as setArgName() assumes a single 'word' and puts a '<' & '>' around that single-word.
            // claBatch.parse( batchcmdargs );
            // if (this.verbose) System.out.println( HDR +" about to start BATCH command [" + claBatch + "]");

            // claBatch.verbose = this.verbose;
            // claBatch.quoteType = _cmdLA.quoteType;
            // claBatch.YAMLLibrary = _cmdLA.YAMLLibrary;
        } // block with 100% only commented out OLD-CODE

        //-------------------------------------
        switch ( _cmdLA.getCmdName() ) {
            case VPC:
            case SGSSH:
            case EC2PLAIN:
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

        final Properties globalProps = _envParams.allPropsRef.get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        // final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::MyVPCStackPrefix}--${ASUX::JobSetName}${ASUX::ItemNumber}", this.allPropsRef );
        final String MyStackNamePrefix = globalProps.getProperty( "MyStackNamePrefix" );

        String preStr = null;
        switch ( _cmdLA.getCmdName() ) {
        case VPC:       preStr = "aws cloudformation create-stack --stack-name ${ASUX::MyVPCStackPrefix}-VPC  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${ASUX::MyVPCStackPrefix} --template-body file://"+ outpfile;
                        break;
        case SUBNET:    preStr = "aws cloudformation create-stack --stack-name ${ASUX::MyVPCStackPrefix}-subnets-"+ _cmdLA.publicOrPrivateSubnet +"-"+ _cmdLA.jobSetName + _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --template-body file://"+ outpfile;
                        break;
        case SGSSH:     preStr = "aws cloudformation create-stack --stack-name ${ASUX::MyVPCStackPrefix}-"+ _cmdLA.jobSetName +"-SG-SSH"+ _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey=MyVPC,ParameterValue=${ASUX::VPCID} --template-body file://"+ outpfile;
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

                        final String DefaultPublicSubnet1 = MyStackNamePrefix + "-Subnet-1-ID";// Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-Subnet-1-ID", this.allPropsRef  );
                        final String MySSHSecurityGroup = MyStackNamePrefix + "-SG-SSH"; // Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-SG-SSH", this.allPropsRef  );
                        final String MySSHKeyName = Macros.evalThoroughly( this.verbose, "${ASUX::AWSLocation}-${ASUX::MyOrgName}-${ASUX::MyEnvironment}-LinuxSSH.pem", _envParams.allPropsRef );
                        // final String MyIamInstanceProfiles = Macros.evalThoroughly( this.verbose, "${ASUX::?????????????????}-"+HDR, this.allPropsRef );
                        // 'MyIamInstanceProfiles' must be set within one of the JOB files (like Job-Master.properties or Job-ec2plain.properties)
                        // globalProps.setProperty( "DefaultPublicSubnet1", DefaultPublicSubnet1 );
                        // globalProps.setProperty( "MySSHSecurityGroup", MySSHSecurityGroup );
                        // globalProps.setProperty( "MySSHKeyName", MySSHKeyName );
                        if (this.verbose) System.out.println( HDR + "DefaultPublicSubnet1=" + DefaultPublicSubnet1 + " MySSHSecurityGroup=" + MySSHSecurityGroup + " MySSHKeyName=" + MySSHKeyName );

                        final String params =
                                    " ParameterKey=MyPublicSubnet1,ParameterValue="+ DefaultPublicSubnet1 +
                                    " ParameterKey=MySSHSecurityGroup,ParameterValue="+ MySSHSecurityGroup +
                                    " ParameterKey=MyIamInstanceProfiles,ParameterValue=${ASUX::MyIamInstanceProfiles}" +
                                    " ParameterKey=AWSAMIID,ParameterValue=${ASUX::AWSAMIID} "+
                                    " ParameterKey=EC2InstanceType,ParameterValue=${ASUX::EC2InstanceType} " +
                                    " ParameterKey=MySSHKeyName,ParameterValue=" + MySSHKeyName;

                        preStr ="aws cloudformation create-stack --stack-name ${ASUX::MyVPCStackPrefix}-"+ _cmdLA.jobSetName +"-EC2-${ASUX::MyEC2InstanceName}"+ _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} "+
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
                        }
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
