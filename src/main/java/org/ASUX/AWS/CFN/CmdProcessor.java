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
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // *  @return YAML that should be the complete CloudFormation YAML
    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _cfnJobType a NotNull String (created by {@link BootCheckAndConfig#getCFNJobTypeAsString})
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genYAML( final CmdLineArgs _cmdLA, final String _cfnJobType, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genYAML(..,"+ _cfnJobType +",..): ";
        final Properties globalProps    = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );

        String batchFilePath = null;
        {
            // String[] batchcmdargs = null;
            // switch ( _cmdLA.getCmdName() ) {
            // case VPC:
            //     // batchcmdargs =  processor.genVPCCmdLine( _cmdLA, _boot );
            //     // final String[] batchcmdargs = { "--batch",
            //     //                                 "@"+ _envParams.awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt",
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
            case EC2PLAIN:
                            final String MyDomainName       = globalProps.getProperty( EnvironmentParameters.MYDOMAINNAME );
                            if (this.verbose) System.out.println( HDR + "MyDomainName " + MyDomainName );
                            final String Rt53HostedZoneId   = awssdk.getHostedZoneId( _envParams.getAWSRegion(), MyDomainName );
                            if (this.verbose) System.out.println( HDR + "MyDomainName " + MyDomainName + " Rt53HostedZoneId " + Rt53HostedZoneId  );
                            globalProps.setProperty( EnvironmentParameters.MYRT53HOSTEDZONEID, Rt53HostedZoneId ); // will define ${ASUX::MyRt53HostedZoneId}
                            // fall thru below.
                            batchFilePath = "@"+ _envParams.get_awscfnhome() +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt";
                            break;
            case VPC:
            case SGSSH:
                            batchFilePath = "@"+ _envParams.get_awscfnhome() +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt";
                            break;
            case SUBNET:
                            assertTrue( _cmdLA.PublicOrPrivate != null && _cmdLA.PublicOrPrivate.length() > 0 ); // CmdLineArgs.class guarantees that it will be 'Public' or 'Private', if NOT NULL.
                            // globalProps.setProperty( "PublicOrPrivate", _cmdLA.PublicOrPrivate );  // already set in BootCheckAndConfig.configure()
                            if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

                            batchFilePath = "@"+ _envParams.get_awscfnhome() +"/bin/AWSCFN-"+_cfnJobType+"-"+_cmdLA.PublicOrPrivate+"-Create.ASUX-batch.txt";
                            // batchFilePath = "@"+ _envParams.get_awscfnhome() +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt";
                            break;
            case FULLSTACK:
                            break;
            // case SGEFS:     batchFilePath = ;       break;
            // case VPNCLIENT: batchFilePath = ;       break;

            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                            System.err.println( es );
                            throw new Exception( es );
        } // switch

        //-------------------------------------
        switch ( _cmdLA.getCmdName() ) {
            case FULLSTACK:
                            break;
            case SUBNET:
            case EC2PLAIN:
            case VPC:
            case SGSSH:
                    // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --batch command.
                    // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
                    //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
                    // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below 3 lines
                    final BatchCmdProcessor batcher = new BatchCmdProcessor( _cmdLA.verbose, /* showStats */ false, _cmdLA.getQuoteType(), this.cmdinvoker.dumperopt );
                    batcher.setMemoryAndContext( this.cmdinvoker.getMemoryAndContext() ); // this will invoke.. batcher.initProperties()
                    if ( _cmdLA.verbose ) new org.ASUX.common.Debug(_cmdLA.verbose).printAllProps( HDR +" FULL DUMP of propsSetRef = ", _envParams.getAllPropsRef() );

                    final Node emptyInput = NodeTools.getEmptyYAML( this.cmdinvoker.dumperopt );
                    final Node outpData2 = batcher.go( batchFilePath, emptyInput );
                    if ( this.verbose ) System.out.println( HDR +" outpData2 =" + outpData2 +"\n\n");
                    if ( outpData2 == null ) {
                        System.err.println("Failure!  See error-messages above.  The "+ _cmdLA.getCmdName() + " command abruptly ends immediately." );
                        System.exit(99);
                        throw new Exception( "Failure to successfully complete user-command. rerun with --verbose option!" );
                    }
                    final String outpfile = CmdProcessor.getOutputFilePath( _cmdLA, _envParams, globalProps );
                    InputsOutputs.saveDataIntoReference( "@"+ outpfile, outpData2, null, this.cmdinvoker.getYamlWriter(), this.cmdinvoker.dumperopt, _cmdLA.verbose );
                    break;
            case VPNCLIENT:
            case SGEFS:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
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
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genCFNShellScript( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        // final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}--${ASUX::JobSetName}${ASUX::ItemNumber}", this.allPropsRef );
        final String MyStackNamePrefix = globalProps.getProperty( "MyStackNamePrefix" );
        final String outpfile = getOutputFilePath( _cmdLA, _envParams, globalProps ); //_envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +".yaml";

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );

        String preStr = null;
        String scriptfile;

        switch ( _cmdLA.getCmdName() ) {
        case VPC:       preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-VPC  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey="+EnvironmentParameters.MYVPCSTACKPREFIX+",ParameterValue=${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"} --template-body file://"+ outpfile;
                        scriptfile = _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +".sh";
                        break;
        case SUBNET:    preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-subnets-"+ _cmdLA.PublicOrPrivate +"-"+ _cmdLA.jobSetName + _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --template-body file://"+ outpfile;
                        scriptfile = _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +"-"+ _cmdLA.PublicOrPrivate +".sh";
                        break;
        case SGSSH:     preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-"+ _cmdLA.jobSetName +"-SG-SSH"+ _cmdLA.itemNumber +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey=MyVPC,ParameterValue=${ASUX::VPCID} --template-body file://"+ outpfile;
                        scriptfile = _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +".sh";
                        break;
        case EC2PLAIN:  
                        final CmdProcessorEC2 ec2Processor = new CmdProcessorEC2( this );
                        preStr = ec2Processor.genCFNShellScript( _cmdLA, _envParams );
                        scriptfile = _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +"-"+ globalProps.getProperty( EnvironmentParameters.MYEC2INSTANCENAME ) +".sh";
                        break;
        case FULLSTACK:
                        final CmdProcessorFullStack fullStackProcessor = new CmdProcessorFullStack( this, this.cmdinvoker );
                        // do NOT set 'preStr' for FULLSTACK
                        fullStackProcessor.genCFNShellScript( _cmdLA, _envParams );
                        scriptfile = "??UNDEFINED for FullStack-gen"; // OTHERWISE, Compiler will complain about uninitialized-variable.. .. .. per formula, you can blindly set it to:- _envParams.outputFolderPath +"/"+ _envParams.cfnJobTYPEString +".sh";
                        break;
        case VPNCLIENT:
        case SGEFS:
        case UNDEFINED:
        default:    final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                    System.err.println( es );
                    throw new Exception( es );
        }

        //----------------------------
        switch ( _cmdLA.getCmdName() ) {
            case EC2PLAIN:
            case VPC:
            case SGSSH:
            case SUBNET:
                            final String postStr = Macros.evalThoroughly( this.verbose, preStr, _envParams.getAllPropsRef() );
                            if ( this.verbose ) System.out.println( postStr ); // dump the cmd to execute the CFN script to stdout.
                            org.ASUX.common.IOUtils.write2File( scriptfile, postStr );
                            org.ASUX.common.IOUtils.setFilePerms( this.verbose, scriptfile, true, true, true, true ); // rwx------ file-permissions
                            System.out.println( scriptfile );
                            break;
            case FULLSTACK: // do Nothing for this
            case UNDEFINED:
            default:
                            break;
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Based on whether the VPC, Subnet or EC2 instance is being created - in isolation by itself .. or, as part of a full-stack, the output file-name changes.
     * This variability is feasible due to {@link EnvironmentParameters#getCfnJobTYPEString()}.
     * @param _cmdLA NotNull instance obtained from {@link CmdInvoker#processCommand}
     * @param _envParams MotNull instance obtained from {@link BootCheckAndConfig#configure}
     * @param _globalProps NotNull instance of java.util.Properties, that is saved in {@link #cmdinvoker}'s MemoryAndContext attribute.
     * @return a Nullable string
     * @throws Exception if unimplemented logic or logic-errors
     */
    public static String getOutputFilePath( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams, final Properties _globalProps )
                        throws Exception
    {   final String HDR = CLASSNAME + ": getOutputFilePath(): ";
        switch ( _cmdLA.getCmdName() ) {
            case SUBNET:    return _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +"-"+ _cmdLA.PublicOrPrivate +".yaml";

            case EC2PLAIN:
                            return _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +"-"+ _globalProps.getProperty( EnvironmentParameters.MYEC2INSTANCENAME ) +".yaml";
            case VPC:
            case SGSSH:
                            return _envParams.outputFolderPath +"/"+ _envParams.getCfnJobTYPEString() +".yaml";
            case FULLSTACK:
                            return null;
            case VPNCLIENT:
            case SGEFS:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmdLA.getCmdName();
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

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
    // public String[] genVPCCmdLine( final CmdLineArgs _cla, final BootCheckAndConfig _boot ) throws IOException, Exception
    // {
    //     // final String HDR = CLASSNAME + ": genVPC(): ";
    //     //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
    //     final String cfnJobType = _boot.getCFNJobType( _cla.cmdName );
    //     // final String outpfile   = _envParams.outputFolderPath +"/"+ cfnJobType;

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
