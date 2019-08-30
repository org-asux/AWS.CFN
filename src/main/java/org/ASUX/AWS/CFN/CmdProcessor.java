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
import org.ASUX.common.Tuple;

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
import java.util.Date;
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
    protected StackSet stackset = null; // new StackSet(_verbose, _awsregion, _awslocation);

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
     *  @param _cfnJobType a NotNull String (created by {@link UserInputEnhanced#getCFNJobTypeAsString}).  Either "vpc", "subnet", .. "fullstack-vpc", "fullstack-ec2", ..
     *  @param _myEnv a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genYAML( final CmdLineArgs _cmdLA, final String _cfnJobType, final Environment _myEnv ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genYAML(..,"+ _cfnJobType +",..): ";
        final Properties globalProps    = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );

        final String CreatedDateTimeStamp = new Date().toString();
        globalProps.setProperty( "CreatedDateTimeStamp", CreatedDateTimeStamp );
        final String IAMUserARN = awssdk.getUserARN();
        globalProps.setProperty( "IAMUserARN", IAMUserARN );

        String batchFilePath = null;
        {   // block with 100% only commented out OLD-CODE <<-------- <<----------
            // String[] batchcmdargs = null;
            // switch ( _cmdLA.getCmdName() ) {
            // case VPC:
            //     // batchcmdargs =  processor.genVPCCmdLine( _cmdLA, _boot );
            //     // final String[] batchcmdargs = { "--batch",
            //     //                                 "@"+ _myEnv.awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt",
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
                            final CmdProcessorEC2 ec2Processor = new CmdProcessorEC2( this );
                            batchFilePath = ec2Processor.genYAMLBatchFile( _cmdLA, _myEnv );
                            break;
            case VPC:
                            batchFilePath = "@"+ _myEnv.get_awscfnhome() +"/bin/AWSCFN-"+ _cfnJobType +"-Create.ASUX-batch.txt";
                            break;
            case SG:
                            batchFilePath = "@"+ _myEnv.get_awscfnhome() +"/bin/AWSCFN-"+ _cfnJobType +"-Create.ASUX-batch.txt";
                            // batchFilePath = "@"+ _myEnv.get_awscfnhome() +"/bin/AWSCFN-"+ _cfnJobType +"-"+ _cmdLA.scope +"-Create.ASUX-batch.txt";
                            // we're re-purposing '_cmdLA.scope' for passing/storing the SG-PORT# (ssh/https/..) as provided by user on commandline.
                            break;
            case SUBNET:
                            assertTrue( _cmdLA.scope != null && _cmdLA.scope.length() > 0 ); // CmdLineArgs.class guarantees that it will be 'Public' or 'Private' or 'Public+natgw', if NOT NULL.
                            if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

                            batchFilePath = "@"+ _myEnv.get_awscfnhome() +"/bin/AWSCFN-"+ _cfnJobType +"-"+ _cmdLA.scope +"-Create.ASUX-batch.txt";
                            // batchFilePath = "@"+ _myEnv.get_awscfnhome() +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt";
                            break;
            case FULLSTACK:
                            final CmdProcessorFullStack fullStackProcessor = new CmdProcessorFullStack( this, this.cmdinvoker );
                            // do NOT set 'preStr' for FULLSTACK
                            fullStackProcessor.genAllCFNs( _cmdLA, _myEnv );

                            batchFilePath = "UNDEFINED-for --fullstack-gen"; // set a default value - to help catch any logic-errors
                            break;

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
            case SG:
                    // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --batch command.
                    // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
                    //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
                    // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below 5 lines
                    final NodeTools nodetools = (NodeTools) this.cmdinvoker.getYAMLImplementation();
                    assertTrue( nodetools != null );
                    final BatchCmdProcessor batcher = new BatchCmdProcessor( _cmdLA.verbose, /* showStats */ false, _cmdLA.isOffline(), _cmdLA.getQuoteType(), nodetools.getDumperOptions() );
                    batcher.setMemoryAndContext( this.cmdinvoker.getMemoryAndContext() ); // this will invoke.. batcher.initProperties()
                    if ( _cmdLA.verbose ) org.ASUX.common.Debug.printAllProps( HDR +" FULL DUMP of propsSetRef = ", _myEnv.getAllPropsRef() );

                    final Node emptyInput = NodeTools.getEmptyYAML( nodetools.getDumperOptions() );
                    final Node outpData2 = batcher.go( batchFilePath, emptyInput );
                    if ( this.verbose ) System.out.println( HDR +" outpData2 =" + outpData2 +"\n\n");
                    if ( outpData2 == null ) {
                        System.err.println("Failure!  See error-messages above.  The "+ _cmdLA.getCmdName() + " command abruptly ends immediately." );
                        System.exit(99);
                        throw new Exception( "Failure to successfully complete user-command. rerun with --verbose option!" );
                    }
                    _myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( _cmdLA.getCmdName(), _cmdLA, _myEnv ) );
                    final String YAMLoutpfile = _myEnv.enhancedUserInput.getOutputFolderPath() +"/"+ _myEnv.getStack().getCFNTemplateFileName();
                    InputsOutputs.saveDataIntoReference( "@"+ YAMLoutpfile, outpData2, null, nodetools.getYAMLWriter(), nodetools.getDumperOptions(), _cmdLA.verbose );
                    break;
            case VPNCLIENT:
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
     *  @param _myEnv a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @param _dependsOnSubnetStack Nullable reference to another Stack, that this EC2 instance's CFN depends on.
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genCFNShellScript( final CmdLineArgs _cmdLA, final Environment _myEnv, final Stack _dependsOnSubnetStack )
                        throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genCFNShellScript("+ _cmdLA.getCmdName() +",_myEnv): ";
        final UserInputEnhanced enhancedUserInput = _myEnv.enhancedUserInput;

        final Properties globalProps = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        // final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::"+Environment.MYVPCSTACKPREFIX+"}--${ASUX::JobSetName}${ASUX::ItemNumber}", this.allPropsRef );
        final String MyStackNamePrefix = globalProps.getProperty( "MyStackNamePrefix" );
        // final String outp file = CmdProcessor.get Output FilePath( _cmdLA, _myEnv ); //myEnv.enhancedUserInput.outputFolderPath +"/"+ _myEnv.getCfnJobTYPEString() +".yaml";

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );

        // String stackName = null;
        // switch( _cmdLA.getCmdName() ) {
        //     case SUBNET:        stackName = Stack.genSubnetStackName( _cmdLA );      break;
        //     case EC2PLAIN:      stackName = Stack.genEC2StackName( _cmdLA );         break;
        //     case VPC:           stackName = Stack.genVPCStackName( _cmdLA );         break;
        //     case SG:            stackName = Stack.genSGStackName( _cmdLA );          break;
        //     case FULLSTACK: 
        //     case VPNCLIENT:
        //     case UNDEFINED:
        //     default:        final String es = HDR +" Unimplemented command: " + _cmdLA.getCmdName();
        //                     System.err.println( es );
        //                     throw new Exception( es );
        // } // switch

        // final String properStackname = Macros.evalThoroughly( this.verbose,    stackName,    _myEnv.getAllPropsRef() );
        // _myEnv.getStack().setStackName( properStackname );

        final String itemSuffix = ( _cmdLA.itemNumber == null || "".equals(_cmdLA.itemNumber.trim()) ) ? "" : "-"+ _cmdLA.itemNumber;

        switch ( _cmdLA.getCmdName() ) {
        case VPC:       // preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-VPC  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey="+Environment.MYVPCSTACKPREFIX+",ParameterValue=${ASUX::"+Environment.MYVPCSTACKPREFIX+"} --template-body file://"+ outpfile;
                        _myEnv.getStack().setStackName( Stack.genVPCStackName(_cmdLA) );
                        _myEnv.getStack().addParameter( Environment.MYVPCSTACKPREFIX, "${ASUX::"+Environment.MYVPCSTACKPREFIX+"}" );
                        _myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( _cmdLA.getCmdName(), _cmdLA, _myEnv ) );
                        // scriptfile = enhancedUserInput.getOutputFolderPath() +"/"+ _myEnv.getCfnJobTYPEString() +".sh";
                        break;
        case SUBNET:    // preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-subnets-"+ _cmdLA.scope +"-"+ _cmdLA.jobSetName + itemSuffix +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --template-body file://"+ outpfile;
                        _myEnv.getStack().setStackName( Stack.genSubnetStackName(_cmdLA) );
                        _myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( _cmdLA.getCmdName(), _cmdLA, _myEnv ) );
                        // scriptfile = enhancedUserInput.getOutputFolderPath() +"/"+ _myEnv.getCfnJobTYPEString() +"-"+ _cmdLA.scope + itemSuffix +".sh";
                        break;
        case SG:        // preStr = "aws cloudformation create-stack --stack-name ${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-"+ _cmdLA.jobSetName +"-SG-SSH"+ itemSuffix +"  --region ${ASUX::AWSRegion} --profile ${AWSprofile} --parameters ParameterKey=MyVPC,ParameterValue=${ASUX::VPCID} --template-body file://"+ outpfile;
                        _myEnv.getStack().setStackName( Stack.genSGStackName(_cmdLA) );
                        _myEnv.getStack().addParameter( "MyVPC", "${ASUX::VPCID}" );
                        _myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( _cmdLA.getCmdName(), _cmdLA, _myEnv ) );
                        // scriptfile = enhancedUserInput.getOutputFolderPath() +"/"+ _myEnv.getCfnJobTYPEString() +"-"+ _cmdLA.scope + itemSuffix +".sh";
                        break;
        case EC2PLAIN:  
                        final CmdProcessorEC2 ec2Processor = new CmdProcessorEC2( this );
                        ec2Processor.genCFNShellScript( _cmdLA, _myEnv, _dependsOnSubnetStack );
                        _myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( _cmdLA.getCmdName(), _cmdLA, _myEnv ) );
                        // scriptfile = enhancedUserInput.getOutputFolderPath() +"/"+ _myEnv.getCfnJobTYPEString() +"-"+ globalProps.getProperty( Environment.MYEC2INSTANCENAME ) +".sh";
                        break;
        case FULLSTACK:
                        // Do Nothing.
                        break;
        case VPNCLIENT:
        case UNDEFINED:
        default:    final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                    System.err.println( es );
                    throw new Exception( es );
        }

        //----------------------------
        switch ( _cmdLA.getCmdName() ) {
            case EC2PLAIN:
            case VPC:
            case SG:
            case SUBNET:
                            // final String postStr = Macros.evalThoroughly( this.verbose, preStr, _myEnv.getAllPropsRef() );
                            CmdProcessor.evalMacros( this.verbose, _myEnv.getStack(), _myEnv );
                            final String postStr = _myEnv.getStack().genCLICmd( enhancedUserInput.getOutputFolderPath() );
                            if ( this.verbose ) System.out.println( postStr ); // dump the cmd to execute the CFN script to stdout.

                            final String scriptfile = enhancedUserInput.getOutputFolderPath() +"/"+ InputOutput.genStackScriptFileName( _cmdLA.getCmdName(), _cmdLA, _myEnv);;
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

    public static void evalMacros( final boolean _verbose, final Stack _stackCmd, final Environment _myEnv ) throws Exception
    {   final String HDR = CLASSNAME + ": evalMacros(_stackCmd): ";

        final String AWSRegion = Macros.evalThoroughly( _verbose, _stackCmd.getAWSRegion(), _myEnv.getAllPropsRef() );
        _stackCmd.setAWSRegion( AWSRegion );

        final String AWSLocation = Macros.evalThoroughly( _verbose, _stackCmd.getAWSLocation(), _myEnv.getAllPropsRef() );
        _stackCmd.setAWSLocation( AWSLocation );

        final String newStackName = Macros.evalThoroughly( _verbose, _stackCmd.getStackName(), _myEnv.getAllPropsRef() );
        _stackCmd.setStackName( newStackName ); // dont bother to check whether or not.. .. newStackName === _stackCmd.getStackName()

        final String newStackFileName = Macros.evalThoroughly( _verbose, _stackCmd.getCFNTemplateFileName(), _myEnv.getAllPropsRef() );
        _stackCmd.setCFNTemplateFileName( newStackFileName );

        final LinkedHashMap<String,String> params = _stackCmd.getParams();
        final LinkedHashMap<String,String> newparams = new LinkedHashMap<>();
        final ArrayList<String> keysThatChanged = new ArrayList<>();

        for( String key: params.keySet() ) {
            final String val = params.get( key );
            final String keywom = Macros.evalThoroughly( _verbose, key, _myEnv.getAllPropsRef() ); // 'wom' === with out macros
            final String valwom = Macros.evalThoroughly( _verbose, val, _myEnv.getAllPropsRef() ); // 'wom' === with out macros
            if ( key.equals( keywom ) ) {
                if ( val.equals( valwom ) ) {
                    ; // do nothing.  Nothing changed for EITHER the key or the value (in 'params').
                } else {
                    newparams.put( key, valwom ); // update the value
                }
            } else {
                if ( _verbose ) System.out.println( HDR +"Running macros changed the OLD-Kay'"+ key +"' to '"+ keywom +"." );
                keysThatChanged.add( key );
                newparams.put( keywom, valwom ); // add the new pair
            }
        } // for-loop

        // 1st remove the "changed" keys, then 'putAll' new keys-value pairs.
        for( String key: keysThatChanged ) {
            params.remove( key );
        }

        params.putAll( newparams );
    }

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
    // public String[] genVPCCmdLine( final CmdLineArgs _cla, final BootCheckAndConfig _boot ) throws IOException, Exception
    // {
    //     // final String HDR = CLASSNAME + ": genVPC(): ";
    //     //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}
    //     final String cfnJobType = _boot.getCFNJobType( _cla.cmdName );
    //     // final String outpfile   = _myEnv.enhancedUserInput.getOutputFolderPath() +"/"+ cfnJobType;

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
