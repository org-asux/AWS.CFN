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

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p> This org.ASUX.yaml GitHub.com project and the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com projects.
 * </p>
 * <p> This is technically an independent class, but it is semantically a 'subclass' of org.ASUX.yaml.Cmd</p>
 * <p> This class helps process YAML files using the java.util Collections classes, by leveraging the EsotericSoftware's YamlBeans library-</p>
 * <p> This class is the "wrapper-processor" for the various "YAML-commands" (which traverse a YAML file to do what you want).</p>
 * <p> The 4 YAML-COMMANDS are: <b>read/query, list, delete</b> and <b>replace</b>. </p>
 * <p> See full details of how to use these commands - in this GitHub project's wiki<br>
 * - or - in<br>
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project and its wiki.
 * </p>
 *
 * <p>
 * Example: <br>
 * <code>java org.ASUX.yaml.Cmd --delete --yamlpath "paths.*.*.responses.200" -i $cwd/src/test/my-petstore-micro.yaml -o /tmp/output2.yaml  --double-quote</code><br>
 * Example: <b><code>java org.ASUX.yaml.Cmd</code></b> will show all command
 * line options supported.
 * </p>
 */
public class Cmd {

    public static final String CLASSNAME = Cmd.class.getName();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * This is NOT testing code. It's actual means by which user's command line arguments are read and processed
     * @param args user's commandline arguments
     */
    public static void main( String[] args )
    {
        final String HDR = CLASSNAME + ": main(String[]): ";

        final CmdLineArgs cmdlineargs = new CmdLineArgs( false );

        try {
            cmdlineargs.parse( args );

            CmdInvoker cmdinvoker = new CmdInvoker( cmdlineargs.verbose );
            if (cmdlineargs.verbose) System.out.println( HDR +"getting started with cmdline args = " + cmdlineargs + " " );

            cmdinvoker.getMemoryAndContext().setAllPropsRef( org.ASUX.common.OSScriptFileScanner.initProperties() );

            //=============================================================
            // read input, whether it's System.in -or- an actual input-file
            if (cmdlineargs.verbose) System.out.println( HDR +" jobSetName: " + cmdlineargs.getJobSetName() +", itemNumber: "+ cmdlineargs.getItemNumber() +" cmdlineargs.getCmdName()="+ cmdlineargs.getCmdName() );

            // -----------------------
            // PRECEDES the processing of the command
            switch ( cmdlineargs.getCmdName() ) {
                case VPC:
                case SUBNET:
                case SGSSH:
                case SGEFS:
                case EC2PLAIN:
                case FULLSTACK:
                    break; // do nothing for now.
                case VPNCLIENT:
                case UNDEFINED:
                default:
                    final String es = HDR +"Internal Error: INCOMPLETE CODE 1.  Switch(_cmdName) for _cmdName="+ cmdlineargs.getCmdName();
                    System.err.println( es );
                    throw new Exception( es );
            }

            //======================================================================
            // run the command requested by user
            final Object outputAsIs = cmdinvoker.processCommand( cmdlineargs, null );

            if (cmdlineargs.verbose) System.out.println( HDR +" processing of entire command returned outputAsIs as NULL!" );
            if (cmdlineargs.verbose && outputAsIs != null ) System.out.println( HDR +" processing of entire command returned [" + (outputAsIs.getClass().getName()) + "]" );

            // we're doing NOTHING with 'outputAsIs' .. for now..

            //======================================================================
            // post-completion of processing of command
            switch ( cmdlineargs.getCmdName() ) {
                case VPC:
                case SUBNET:
                case SGSSH:
                case SGEFS:
                case EC2PLAIN:
                case FULLSTACK:
                    break; // do nothing for now.
                case VPNCLIENT:
                case UNDEFINED:
                default:
                    final String es = HDR +"Internal Error: INCOMPLETE CODE 2.  Switch(_cmdName) for _cmdName="+ cmdlineargs.getCmdName();
                    System.err.println( es );
                    throw new Exception( es );
            }

        } catch ( org.apache.commons.cli.ParseException pe ) {
            // ATTENTION: If CmdLineArgs.java  and its subclasses threw an ParseException, they'll catch it themselves, showHelp(), and write debug output.
            // so.. do NOTHING in this class (Cmd.java)
            System.exit(9);
        // } catch (java.io.FileNotFoundException fnfe) {
        //     if ( cmdlineargs == null || cmdlineargs.verbose ) fnfe.printStackTrace(System.err);
        //     System.err.println( fnfe +"\n"+ HDR +"\n\nERROR: INPUT-File Not found: '" + cmdlineargs.inputFilePath + "'\nFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
        //     System.exit(8);
        // } catch (java.io.IOException ioe) {
        //     if ( cmdlineargs == null || cmdlineargs.verbose ) ioe.printStackTrace(System.err);
        //     System.err.println( ioe +"\n"+ HDR +"\n\nERROR: OUTPUT-File Not found: '" + cmdlineargs.outputFilePath + "'\nFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
        //     System.exit(7);
        } catch (Exception e) {
            if ( cmdlineargs == null || cmdlineargs.verbose ) e.printStackTrace(System.err);
            System.err.println( e +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(6);
        } catch (Throwable t) {
            t.printStackTrace(System.err); // main() unit-testing
            System.err.println( t +"\n"+ HDR +"\n\nINTERNAL ERROR!\tFYI: Cmdline arguments provided are: " + cmdlineargs + "\n" );
            System.exit(6);
        }

    }

}
