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

import org.ASUX.yaml.YAML_Libraries;
import org.ASUX.yaml.MemoryAndContext;
import org.ASUX.yaml.CmdLineArgsBasic;
import org.ASUX.yaml.CmdLineArgsBatchCmd;

import org.ASUX.YAML.NodeImpl.NodeTools;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.regex.*;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.DumperOptions; // https://bitbucket.org/asomov/snakeyaml/src/default/src/main/java/org/yaml/snakeyaml/DumperOptions.java

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <p>
 * This org.ASUX.yaml GitHub.com project and the
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com projects.
 * </p>
 * <p>
 * This class is the "wrapper-processor" for the various "YAML-commands" (which
 * traverse a YAML file to do what you want).
 * </p>
 * <p>
 * The 4 YAML-COMMANDS are: <b>read/query, list, delete</b> and <b>replace</b>.
 * </p>
 * <p>
 * See full details of how to use these commands - in this GitHub project's wiki
 * - or - in
 * <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a>
 * GitHub.com project and its wiki.
 * </p>
 *
 * <p>
 * Example: <code>java org.ASUX.yaml.Cmd --vpc-gen my1st -o /tmp/output2.yaml  --no-quote --yamllibrary NodeImpl </code><br>
 * Example: Just running: <b><code>java org.ASUX.yaml.Cmd</code></b> will show all command-line options supported.
 * </p>
 */
public class CmdInvoker extends org.ASUX.YAML.NodeImpl.CmdInvoker {

    private static final long serialVersionUID = 512L;

    public static final String CLASSNAME = CmdInvoker.class.getName();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================
    /**
     *  The constructor exclusively for use by  main() classes anywhere.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @throws Exception if YAML-implementation is Not properly initialized or YAML-implementation cannot be "loaded"
     */
    public CmdInvoker( final boolean _verbose ) throws Exception {
        super( _verbose, /* showStats= */false );
    }

    /**
     *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within org.ASUX.YAML.NodeImp.BatchYamlProcessor which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _showStats Whether you want a final summary onto console / System.out
     *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within org.ASUX.YAML.CollectionImpl.BatchYamlProcessor which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     */
    public CmdInvoker( final boolean _verbose, final boolean _showStats, final MemoryAndContext _memoryAndContext ) {
        super(_verbose, _showStats, _memoryAndContext );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  See {@link org.ASUX.yaml.CmdInvoker}, as this is the implementation of this abstract method.
     *  @param _cmdLA Everything passed as commandline arguments to the Java program {@link org.ASUX.yaml.CmdLineArgsCommon}
     *  @param _unused currently this is Not used.
     *  @return either a String or org.yaml.snakeyaml.nodes.Node
     *  @throws FileNotFoundException if the filenames within _cmdLA do NOT exist
     *  @throws IOException if the filenames within _cmdLA give any sort of read/write troubles
     *  @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
     */
    @Override
    public Object processCommand ( org.ASUX.yaml.CmdLineArgsCommon _cmdLA, java.lang.Object _unused )
                throws FileNotFoundException, IOException, Exception
    {
        final CmdLineArgs cmdLA = (CmdLineArgs) _cmdLA;
        final String HDR = CLASSNAME + ": processCommand("+ cmdLA.getCmdName() +",_inputData: ";// NOTE !!!!!! _cmdLA/CmdLineArgsCommon .. does NOT have 'cmdType' instance-variable

        final NodeTools nodetools = (NodeTools) super.getYAMLImplementation();
        assertTrue( nodetools != null );
        NodeTools.updateDumperOptions( nodetools.getDumperOptions(), _cmdLA.getQuoteType() ); // Important <<---------- <<---------- <<-----------

        //---------------------------
        final Environment env = new Environment( this.verbose, this.memoryAndContext.getAllPropsRef() );
        final BootCheckAndConfig boot = new BootCheckAndConfig( this.verbose, env );
        boot.check( cmdLA ); // cmdLA.getCmdName(), cmdLA.getJobSetName(), cmdLA.getItemNumber() );
        if ( cmdLA.cmdName == Enums.GenEnum.FULLSTACK )
            boot.checkForFullStack( cmdLA );
        boot.configure( cmdLA ); // cmdLA.getCmdName(), cmdLA.getJobSetName(), cmdLA.getItemNumber() );
        // This boot.configure() will invoke the following:-
        // myEnvVPC.setHomeFolders( .. .. .. );
        // myEnvVPC.setFundamentalGlobalProps( .. .. );
        // myEnvVPC.setFundamentalPrefixes( .. .. );

        CmdProcessor processor = new CmdProcessor( this );
        if ( cmdLA.verbose ) new org.ASUX.common.Debug(cmdLA.verbose).printAllProps( HDR +" FULL DUMP of this.propsSetRef = ", this.memoryAndContext.getAllPropsRef() );

        //-------------------------------------
        // 1st generate the YAML.
        processor.genYAML( cmdLA, boot.myEnv.getCfnJobTYPEString(), boot.myEnv );

        // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
        processor.genCFNShellScript( cmdLA, boot.myEnv, null /* no Subnet-stack-depedency */ );

        //-------------------------------------
        // super.class (org.ASUX.yaml.CmdInvoker) requires that this method return something.
        // Cmd.java for this project does Not care about the return-value of this method (unlike Cmd.java for other ASUX.org projects like org.ASUX.yaml)
        return null;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>This method needs to supplement org.ASUX.YAML.CmdInvoker.deepClone() as this subclass (org.ASUX.YAML.NodeImpl.CmdInvoker) has it's own transient instance-fields/variables.</p>
     *  <p>Such Transients are made Transients for only ONE-SINGLE REASON - they are NOT serializable).</p>
     *  <p>!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!</p>
     *  <p>So, after a deepClone() of CmdInvoker.java .. you'll need to call: </p>
     *  <p> <code> clone.dumperopt = origObj.dumperopt; </code> <br>
     *  @param origObj the non-null original to clone
     *  @return a properly cloned and re-initiated clone of the original (that works around instance-variables that are NOT serializable)
     *  @throws Exception when org.ASUX.common.Utils.deepClone clones the core of this class-instance 
     */
    public static CmdInvoker deepClone( final CmdInvoker origObj ) throws Exception {
        // final CmdInvoker newCmdInvk = org.ASUX.common.Utils.deepClone( origObj );
        final CmdInvoker newCmdinvoker = (CmdInvoker) org.ASUX.YAML.NodeImpl.CmdInvoker.deepClone( origObj );
        return newCmdinvoker;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

}
