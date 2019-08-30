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

import org.ASUX.common.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 *  <p>This class encapsulates all interactions with the file-system, especially in standardizing file-names and folder-paths.</p>
 *  <p>This standardization of file-names and folder-paths are important as a SHELL-script refers to a YAML-CFN-template, while a YAML-CFN-templates has nested-templates, and finally, some CFN-templates need to be uploaded into S3 in a way other scripts/CFN-templates can find them via standard-names.</p>
 */
public final class InputOutput implements Serializable
{
    private static final long serialVersionUID = 467L;

    public static final String CLASSNAME = InputOutput.class.getName();

    public boolean verbose;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public InputOutput( final boolean _verbose ) {
        this.verbose = _verbose;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Yet-another utility-function - to help STANDARDIZE the naming of FILE-NAMES for CFN-Stacks</p>
     *  @param _cmd see {@link Enums.GenEnum}
     *  @param _cmdLA a NotNull instance
     *  @param _myEnv a NotNull instance
     *  @return a NotNull String representing JUST the file-name ONLY.  !!ATTENTION!! Prepend it with a Folder-path, to exactly place the file in the right location in the file-system.
     *  @throws Exception for unimplemented commands (code-safety checks)
     */
    public static final String genStackCFNFileName( final Enums.GenEnum _cmd, final CmdLineArgs _cmdLA, final Environment _myEnv ) throws Exception {
        return genStackCFNFileName(_cmd, _cmdLA, _myEnv, ".yaml");
    }

    /**
     *  <p>Yet-another utility-function - to help STANDARDIZE the naming of FILE-NAMES for script that _EXECUTE_ 'aws cloudformation' commands for CFN-Stacks</p>
     *  @param _cmd see {@link Enums.GenEnum}
     *  @param _cmdLA a NotNull instance
     *  @param _myEnv a NotNull instance
     *  @return a NotNull String representing JUST the file-name ONLY.  !!ATTENTION!! Prepend it with a Folder-path, to exactly place the file in the right location in the file-system.
     *  @throws Exception for unimplemented commands (code-safety checks)
     */
    public static final String genStackScriptFileName( final Enums.GenEnum _cmd, final CmdLineArgs _cmdLA, final Environment _myEnv ) throws Exception {
        return genStackCFNFileName(_cmd, _cmdLA, _myEnv, ".sh");
    }


    /* Note: the last argument _suffix === '.yaml' or '.sh' ;  But nothing to stop you from passing in anything else. */
    private static final String genStackCFNFileName( final Enums.GenEnum _cmd, final CmdLineArgs _cmdLA, final Environment _myEnv, final String _suffix ) throws Exception
    {   final String HDR = CLASSNAME + ": getStackCFNFileName("+ _cmd +"_cmdLA,_myEnv,"+ _suffix +"): ";

        switch ( _cmd ) {
            case VPC:       return _myEnv.getCfnJobTYPEString() + UserInput.getItemNumberSuffix(_cmdLA.itemNumber) +_suffix; // ".yaml";
            case SUBNET:    return _myEnv.getCfnJobTYPEString() +"-"+ _cmdLA.scope + UserInput.getItemNumberSuffix(_cmdLA.itemNumber) +_suffix; // ".yaml";
            case SG:        return _myEnv.getCfnJobTYPEString() +"-"+ _cmdLA.scope + UserInput.getItemNumberSuffix(_cmdLA.itemNumber) +_suffix; // ".yaml";
                            // we're re-purposing '_cmdLA.scope' for passing/storing the SG-PORT# (ssh/https/..) as provided by user on commandline.
            case EC2PLAIN:
                            final Properties globalProps = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
                            return _myEnv.getCfnJobTYPEString() +"-"+ globalProps.getProperty( Environment.MYEC2INSTANCENAME ) +_suffix; // ".yaml";
            case FULLSTACK:
                            return null;    // <<------- <<--------
            case VPNCLIENT:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmd;
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
