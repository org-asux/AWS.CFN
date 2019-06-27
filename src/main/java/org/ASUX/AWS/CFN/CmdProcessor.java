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
            case VPC:       batchFilePath = "@"+ _awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-Create.ASUX-batch.txt";      break;
            case SUBNET:    final Properties globalProps = this.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
                            assertTrue( _cmdLA.publicOrPrivateSubnet != null && _cmdLA.publicOrPrivateSubnet.length() > 0 ); // CmdLineArgs.class guarantees that it will be 'public' or 'private', if NOT NULL.
                            globalProps.setProperty( "PublicOrPrivate", _cmdLA.publicOrPrivateSubnet );
                            if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

                            batchFilePath = "@"+ _awscfnhome +"/bin/AWSCFN-"+_cfnJobType+"-"+_cmdLA.publicOrPrivateSubnet+"-Create.ASUX-batch.txt";
                            break;
            // case SGSSH:     batchFilePath = ;       break;
            // case SGEFS:     batchFilePath = ;       break;
            // case VPNCLIENT: batchFilePath = ;       break;
            // case EC2PLAIN:  batchFilePath = ;       break;

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
     *  @param _cfnJobType a NotNull String (created by {@link BootstrapAndChecks#getCFNJobType})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genCFNShellScript( final CmdLineArgs _cmdLA, final String _cfnJobType ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";
        final String outpfile   = "/tmp/"+ _cfnJobType;
        final String scriptfile = "/tmp/"+ _cfnJobType +".sh";

        String preStr = null;
        switch ( _cmdLA.getCmdName() ) {
        case VPC:   preStr = "aws cloudformation create-stack --stack-name ${ASUX::MyVPCStackPrefix}-VPC  --region ${ASUX::AWSRegion} --profile \\${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${ASUX::MyVPCStackPrefix} --template-body file://"+ outpfile;
                    break;

        case SUBNET:preStr = "aws cloudformation create-stack --stack-name ${ASUX::MyVPCStackPrefix}-subnets-"+ _cmdLA.publicOrPrivateSubnet +"-"+ _cmdLA.jobSetName +"  --region ${ASUX::AWSRegion} --profile \\${AWSprofile} --template-body file://"+ outpfile;
                    break;

        case SGSSH:
        case SGEFS:
        case VPNCLIENT:
        case EC2PLAIN:

        case UNDEFINED:
        default:    final String es = HDR +" Unimplemented command: " + _cmdLA.toString();
                    System.err.println( es );
                    throw new Exception( es );
        }
        final String postStr = Macros.evalThoroughly( this.verbose, preStr, getAllPropsRef() );
        if ( this.verbose ) System.out.println( postStr ); // dump the cmd to execute the CFN script to stdout.

        try {
            java.nio.file.Files.write(   java.nio.file.Paths.get( scriptfile ),   postStr.getBytes()  );
            System.out.println( "File "+scriptfile+" created." );
        } catch(java.nio.file.InvalidPathException ipe) {
            // if ( this.verbose ) ipe.printStackTrace( System.err );
            // if ( this.verbose ) System.err.println( "\n\n"+ HDR +"Serious internal error: Why would the Path be invalid?" );
            ipe.printStackTrace( System.err );
            System.err.println( "\n\n"+ HDR +"!!SERIOUS INTERNAL ERROR!! Why would the Path '"+ scriptfile +"' be invalid?\n\n" );
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
