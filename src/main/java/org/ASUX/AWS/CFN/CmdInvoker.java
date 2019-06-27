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

import org.ASUX.YAML.NodeImpl.BatchCmdProcessor;
import org.ASUX.YAML.NodeImpl.NodeTools;
import org.ASUX.YAML.NodeImpl.GenericYAMLWriter;
import org.ASUX.YAML.NodeImpl.InputsOutputs;

import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.regex.*;
import java.util.LinkedHashMap;
import java.util.Properties;

// https://yaml.org/spec/1.2/spec.html#id2762107
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.Node;
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
     */
    public CmdInvoker( final boolean _verbose ) {
        super( _verbose, /* showStats= */false );
    }

    /**
     *  Variation of constructor that allows you to pass-in memory from another previously existing instance of this class.  Useful within {@link org.ASUX.yaml.BatchCmdProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     *  @param _memoryAndContext pass in memory from another previously existing instance of this class.  Useful within {@link org.ASUX.yaml.BatchCmdProcessor} which creates new instances of this class, whenever it encounters a YAML or AWS command within the Batch-file.
     * @param _dopt a non-null reference to org.yaml.snakeyaml.DumperOptions instance.  CmdInvoker can provide this reference.
     */
    public CmdInvoker( final boolean _verbose, final MemoryAndContext _memoryAndContext, final DumperOptions _dopt ) {
        super(_verbose, /* showStats= */false, _memoryAndContext, _dopt );
        init();
    }

    // @Override
    // protected void init() {
    // }

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
        final String HDR = CLASSNAME + ": processCommand("+ _cmdLA.toString() +"): ";
        final CmdLineArgs cmdLA = (CmdLineArgs) _cmdLA;

        this.setYamlLibrary( cmdLA.YAMLLibrary );
        if (cmdLA.verbose) System.out.println( HDR +" set YAML-Library to [" + cmdLA.YAMLLibrary + " and [" + this.getYamlLibrary() + "]" );

        //---------------------------
        if ( this.dumperopt == null ) // this won't be null, if this object was created within BatchCmdProcessor.java
            this.dumperopt = GenericYAMLWriter.defaultConfigurationForSnakeYamlWriter();

        switch( cmdLA.quoteType ) {
            case DOUBLE_QUOTED: dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.DOUBLE_QUOTED );  break;
            case SINGLE_QUOTED: dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.SINGLE_QUOTED );  break;
            case LITERAL:       dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.LITERAL );        break;
            case FOLDED:        dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
            case PLAIN:         dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.PLAIN );          break;
            default:            dumperopt.setDefaultScalarStyle( org.yaml.snakeyaml.DumperOptions.ScalarStyle.FOLDED );         break;
        }

        //---------------------------
        final BootstrapAndChecks boot = new BootstrapAndChecks( this.verbose, this.memoryAndContext.getAllPropsRef() );
        boot.exec( cmdLA.getCmdName(), cmdLA.getJobSetName() );
        CmdProcessor processor = new CmdProcessor( this );
        if ( cmdLA.verbose ) new org.ASUX.common.Debug(cmdLA.verbose).printAllProps( HDR +" FULL DUMP of this.propsSetRef = ", this.memoryAndContext.getAllPropsRef() );

        //---------------------------
        final String cfnJobType = boot.getCFNJobType( cmdLA.getCmdName() );
        String[] batchcmdargs = null;

        switch ( cmdLA.getCmdName() ) {
        case VPC:
            batchcmdargs =  processor.genVPCCmdLine( cmdLA, boot );
            break;

        case SUBNET:
        case SGSSH:
        case SGEFS:
        case VPNCLIENT:
        case UNDEFINED:

        case EC2PLAIN:
            // if (claMacro.verbose) System.out.println( HDR +" loading Props file [" + claMacro.propertiesFilePath + "]");
            // assertTrue( claMacro.propertiesFilePath != null );

            // switch ( cmdLA.cmdType ) {
            //     case MACRO:     assertTrue( false ); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
            //                     // macroStrPr = new MacroStringProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'dumperopt'
            //                     break;
            //     case MACROYAML: macroYamlPr = new MacroYamlProcessor( claMacro.verbose, claMacro.showStats ); // does NOT use 'dumperopt'
            //                     break;
            //     default: assertTrue( false ); // should not be here.
            // }

            // Properties properties = null;
            // if ( "!AllProperties".equals( claMacro.propertiesFilePath ) ) {
            //     // do Nothing.   properties will remain set to 'null'
            // } else {
            //     final Object content = this.getDataFromReference( claMacro.propertiesFilePath );
            //     if (content instanceof Properties) {
            //         properties = (Properties) content;
            //     }else {
            //         throw new Exception( claMacro.propertiesFilePath +" is Not a java properties file, with the extension '.properties' .. or, it's contents (of type'"+ content.getClass().getName() +"')are Not compatible with java.util.Properties" );
            //     }
            // }

            // if (claMacro.verbose) System.out.println( HDR +" about to start MACRO command using: [Props file [" + claMacro.propertiesFilePath + "]");
            // Node outpData = null;
            // switch ( cmdLA.cmdType ) {
            //     case MACRO:     assertTrue( false ); // we can't get here with '_input' ..  _WITHOUT_ it being a _VALID_ YAML content.   So, so might as well as use 'MacroYamlProcessor'
            //                     // outpData = macroStrPr.searchNReplace( raw-java.lang.String-from-where??, properties, this.memoryAndContext.getAllPropsRef() );
            //                     break;
            //     case MACROYAML: outpData = macroYamlPr.recursiveSearch( _inputNode, properties, this.memoryAndContext.getAllPropsRef() );
            //                     break;
            //     default: assertTrue( false ); // should not be here.
            // }

        default:
            final String es = HDR +" Unimplemented command: " + cmdLA.toString();
            System.err.println( es );
            throw new Exception( es );
        }

        //-------------------------------------
        final CmdLineArgsBatchCmd claBatch = new CmdLineArgsBatchCmd( batchcmdargs, org.ASUX.yaml.Enums.CmdEnum.BATCH, CmdLineArgsBasic.BATCHCMD[0], CmdLineArgsBasic.BATCHCMD[1], CmdLineArgsBasic.BATCHCMD[2], 1, "BatchFileName" );  // Note: there's a trick in the parameter-string.. as setArgName() assumes a single 'word' and puts a '<' & '>' around that single-word.
        claBatch.parse( batchcmdargs );
        if (this.verbose) System.out.println( HDR +" about to start BATCH command [" + claBatch + "]");

        claBatch.verbose = this.verbose;
        claBatch.quoteType = cmdLA.quoteType;
        claBatch.YAMLLibrary = cmdLA.YAMLLibrary;

        //-------------------------------------
        // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --batch command.
        // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
        //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
        // final Object outputAsIs = this.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below 3 lines
        final BatchCmdProcessor batcher = new BatchCmdProcessor( claBatch.verbose, claBatch.showStats, claBatch.quoteType, this.dumperopt );
        batcher.setMemoryAndContext( this.getMemoryAndContext() );
        if ( cmdLA.verbose ) new org.ASUX.common.Debug(cmdLA.verbose).printAllProps( HDR +" FULL DUMP of this.propsSetRef = ", this.getMemoryAndContext().getAllPropsRef() );

        final Node emptyInput = NodeTools.getEmptyYAML( this.dumperopt );
        final Node outpData2 = batcher.go( claBatch.batchFilePath, emptyInput );
        if ( this.verbose ) System.out.println( HDR +" outpData2 =" + outpData2 +"\n\n");

        final String outpfile   = "/tmp/"+ cfnJobType +".yaml";
        InputsOutputs.saveDataIntoReference( "@"+ outpfile, outpData2, null, this.getYamlWriter(), this.dumperopt, claBatch.verbose );

        //-------------------------------------
        switch ( cmdLA.getCmdName() ) {
            case VPC:
                processor.genVPCCFNShellScript( cmdLA, boot );
                break;
    
            case SUBNET:
            case SGSSH:
            case SGEFS:
            case VPNCLIENT:
            case UNDEFINED:
            case EC2PLAIN:

            default:
            final String es = HDR +" Unimplemented command: " + cmdLA.toString();
            System.err.println( es );
            throw new Exception( es );
        }

        //-------------------------------------
        return outpData2;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // /**
    //  * know which YAML-parsing/emitting library was chosen by user.  Ideally used within a Batch-Yaml script / BatchCmdProcessor.java
    //  * @return the YAML-library in use. See {@link YAML_Libraries} for legal values to this parameter
    //  */
    // @Override
    // public YAML_Libraries getYamlLibrary() {
    //     // !!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!
    //     // The body should be an EXACT __COPY__ of  org.ASUX.YAML.NodeImpl.CmdInvoker's method.
    //     // We can't invoke that other method, and have it set instance-variables here
    //     final YAML_Libraries sclib = this.YAMLScanner.getYamlLibrary();
    //     assertTrue( sclib == this.YAMLWriter.getYamlLibrary() );
    //     return sclib;
    // }

    // /**
    //  * Allows you to set the YAML-parsing/emitting library of choice.  Ideally used within a Batch-Yaml script.
    //  * @param _l the YAML-library to use going forward. See {@link YAML_Libraries} for legal values to this parameter
    //  */
    // @Override
    // public void setYamlLibrary( final YAML_Libraries _l ) {
    //     // !!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!
    //     // The body should be an EXACT __COPY__ of  org.ASUX.YAML.NodeImpl.CmdInvoker's method.
    //     // We can't invoke that other method, and have it set instance-variables here
    //     if ( this.YAMLScanner == null || this.YAMLWriter == null )
    //         this.init();
    //     this.YAMLScanner.setYamlLibrary(_l);
    //     this.YAMLWriter.setYamlLibrary(_l);
    // }


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

    // /**
    //  *  <p>Example: For SnakeYAML-library based subclass of this, this should return DumperOptions.class</p>
    //  *  <p>This is to be used primarily within org.ASUX.yaml.BatchCmdProcessor#onAnyCmd().</p>
    //  *  @return name of class of the object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions)
    //  */
    // public Class<?> getLibraryOptionsClass() {
    //     final String HDR = CLASSNAME + ": getLibraryOptionsClass(__CLASS__): ";
    //     throw new RuntimeException( "Unimplemented method!"+ HDR );
    // }

    // /**
    //  *  <p>Example: For SnakeYAML-library based subclass of this, this should return the reference to the instance of the class DumperOption</p>
    //  *  <p>This is to be used primarily within org.ASUX.yaml.BatchCmdProcessor#onAnyCmd().</p>
    //  * @return instance/object that subclasses of {@link CmdInvoker} use, to configure YAML-Output (example: SnakeYAML uses DumperOptions objects)
    //  */
    // public Object getLibraryOptionsObject() {
    //     final String HDR = CLASSNAME + ": getLibraryOptionsObject(__OBJECT__): ";
    //     throw new RuntimeException( "Unimplemented method!"+ HDR );
    // }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // /**
    //  * This is a simpler facade/interface to {@link InputsOutputs#getDataFromReference}, for use by {@link org.ASUX.yaml.BatchCmdProcessor}
    //  * @param _src a javalang.String value - either inline YAML/JSON, or a filename (must be prefixed with '@'), or a reference to a property within a Batch-file execution (must be prefixed with a '!')
    //  * @return an object (either any of Node, SequenceNode, MapNode, ScalarNode ..)
    //  * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
    //  * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
    //  * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
    //  */
    // public Object getDataFromReference( final String _src )
    //                             throws FileNotFoundException, IOException, Exception
    // {   final String HDR = CLASSNAME + ": getDataFromReference("+ _src +"): ";
    //     // return InputsOutputs.getDataFromReference( _src, this.memoryAndContext, ???? this.getYamlScanner() ???, this.dumperopt, this.verbose );
    //     throw new Exception( "Unimplemented method!"+ HDR );
    // }

    // //==============================================================================
    // //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // //==============================================================================

    // /**
    //  * This is a simpler facade/interface to {@link InputsOutputs#saveDataIntoReference}, for use by {@link org.ASUX.yaml.BatchCmdProcessor}
    //  * @param _dest a javalang.String value - either a filename (must be prefixed with '@'), or a reference to a (new) property-variable within a Batch-file execution (must be prefixed with a '!')
    //  * @param _input the object to be saved using the reference provided in _dest paramater
    //  * @throws FileNotFoundException if the filenames within _cmdLineArgs do NOT exist
    //  * @throws IOException if the filenames within _cmdLineArgs give any sort of read/write troubles
    //  * @throws Exception by ReplaceYamlCmd method and this nethod (in case of unknown command)
    //  */
    // public void saveDataIntoReference( final String _dest, final Object _input )
    //                         throws FileNotFoundException, IOException, Exception
    // {   final String HDR = CLASSNAME + ": saveDataIntoReference("+ _dest +", _input): ";
    //     // InputsOutputs.saveDataIntoReference( _dest, _input, this.memoryAndContext, this.getYamlWriter(), this.dumperopt, this.verbose );
    //     throw new Exception( "Unimplemented method!"+ HDR );
    // }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

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

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

}
