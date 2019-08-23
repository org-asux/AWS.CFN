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
import org.ASUX.yaml.YAMLImplementation;

import org.ASUX.YAML.NodeImpl.ReadYamlEntry;
import org.ASUX.YAML.NodeImpl.NodeTools;
import org.ASUX.YAML.NodeImpl.GenericYAMLScanner;
import org.ASUX.YAML.NodeImpl.GenericYAMLWriter;
import org.ASUX.YAML.NodeImpl.InputsOutputs;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.regex.*;
import java.util.Properties;

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

import org.junit.Test;
import static org.junit.Assert.*;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * This enum class is a bit extensive, only because the ENNUMERATED VALUEs are strings.
 * For variations - see https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
 */
public final class YAMLTools
{
    public static final String CLASSNAME = YAMLTools.class.getName();

    //-----------------------------
	public boolean verbose;
	private final ReadYamlEntry readcmd;
	private final YAMLImplementation yamlImpl;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public YAMLTools( final boolean _verbose, final boolean _showStats, final YAMLImplementation _yamlImpl ) {
		this.verbose = _verbose;
		this.yamlImpl = _yamlImpl;

        final NodeTools nodetools = (NodeTools) this.yamlImpl;
        this.readcmd = new ReadYamlEntry( _verbose, _showStats, nodetools.getDumperOptions() );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	public final ReadYamlEntry getReadcmd()		{ return this.readcmd; }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public Node readYamlFile( final String _filename ) throws java.io.FileNotFoundException, java.io.IOException, Exception
    {   final String HDR = CLASSNAME + ": readYamlFile("+ _filename +"): ";
        if ( this.verbose ) System.out.println( HDR +" Reading YAML from file: '"+ _filename +"'" );
        final InputStream is1                = new FileInputStream( _filename );
        final InputStreamReader filereader   = new InputStreamReader(is1);
        final GenericYAMLScanner yamlscanner = new GenericYAMLScanner( this.verbose );
        yamlscanner.setYAMLLibrary( YAML_Libraries.NodeImpl_Library );
        if ( this.verbose ) System.out.println( HDR +" Reading YAML from file: '"+ _filename +"'" );
        final Node node = yamlscanner.load( filereader );
        if ( this.verbose ) System.out.println( NodeTools.Node2YAMLString( node ) );
        return node;
    }

    /**
     *  <p>Given a 'filenamePrefix' === 'Services', This method will check for the following 3 files - in that order.  And return the contents of the 1st file found.</p>
     * <ol><li>~/.aws/Services-Alone.yaml</li><li>Services-Department.yaml</li><li>Services-Enterprise.yaml</li></ol>
     *  @param _filename NotNull String. Does NOT matter, whether you provide a file-name, with OR without the '.yaml' file-name-extension, it will be automatically addressed.
     *  @return a NotNull Node (Any failure will lead to Exceptions being thrown)
     *  @throws FileNotFoundException if the file whose name is derived as described above, do Not exist
     *  @throws IOException if any error reading the contents of the file
     *  @throws Exception any trouble with reading the YAML contents
     */
    public Node readUserDefaultsYamlFile( final String _filename )
                throws FileNotFoundException, IOException, Exception
    {
        final String HDR = CLASSNAME + ": readUserDefaultsYamlFile("+ _filename +"): ";
        final String filenamePrefix = _filename.endsWith(".yaml") ? _filename.replaceAll(".yaml$","") : _filename;
        final String fnAlone        = Environment.USERCONFIGHOME_CFN +"/"+ filenamePrefix +"-"+ Environment.ALONE       +".yaml";
        final String fnDept         = Environment.USERCONFIGHOME_CFN +"/"+ filenamePrefix +"-"+ Environment.DEPT        +".yaml";
        final String fnEnterprise   = Environment.USERCONFIGHOME_CFN +"/"+ filenamePrefix +"-"+ Environment.ENTERPRISE  +".yaml";
        String nameOfFileThatExists;
        InputStream is1;
        try {
            nameOfFileThatExists = fnAlone;
            if ( this.verbose ) System.out.println( HDR +" About to open file: '" + nameOfFileThatExists +"'" );
            // is1 = new FileInputStream( nameOfFileThatExists );
            return this.readYamlFile( nameOfFileThatExists );
        } catch (FileNotFoundException fnfe) {
            try {
                nameOfFileThatExists = fnDept;
                if ( this.verbose ) System.out.println( HDR +" About to open file: '" + nameOfFileThatExists +"'" );
                // is1 = new FileInputStream( nameOfFileThatExists );
                return this.readYamlFile( nameOfFileThatExists );
            } catch (FileNotFoundException fnfe2) {
                nameOfFileThatExists = fnEnterprise;
                if ( this.verbose ) System.out.println( HDR +" About to open file: '" + nameOfFileThatExists +"'" );
                // is1 = new FileInputStream( nameOfFileThatExists );
                return this.readYamlFile( nameOfFileThatExists );
            }
        }
        // final InputStreamReader filereader   = new InputStreamReader( is1 );
        // final GenericYAMLScanner yamlscanner = new GenericYAMLScanner( this.verbose );
        // yamlscanner.setYamlLibrary( YAML_Libraries.NodeImpl_Library );
        // if ( this.verbose ) System.out.println( HDR +" Reading YAML from file: '"+ nameOfFileThatExists +"'" );
        // final Node node = yamlscanner.load( filereader );
        // if ( this.verbose ) System.out.println( NodeTools.Node2YAMLString( node ) );
        // return node;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

	// *  @param _readcmd a NotNull instance

    /**
     *  <p>Common code refactored into a utility private method.  Given the YAML from the 'Jobfile.yaml', read the various YAML-entries like 'AWS,MyOrgName', 'AWS,MyEnvironment', ..</p>
     *  <p>Warning! if you are expecting a simple string, and you either make a mistake with "_YAMLPath" or .. the user enters much more than a simple string @ '_YAMLPath' .. you've got a problem!</p>
     *  @param _inputNode NotNull Node reference
     *  @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String
     *  @return a Nullable String 
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, or the '_YAMLPath' does not point to a simple String .. (Also, potentially, a org.junit.Assert.AssertionError (Throwable) is thrown, as determined within org.ASUX.YAML.NodeImpl.NodeTools.getScalarContent)
     */
    public String readStringFromYAML( final Node _inputNode, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readStringFromYAML(<Node>, "+ _YAMLPath +"): ";
        this.readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final Node output = this.readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );

        final NodeTools nodetools = (NodeTools) this.yamlImpl;
        final String s = nodetools.getScalarContent( output );
        return s;
    }

    //=================================================================================
    /**
     *  Common code refactored into a utility private method.  Given the YAML from the 'Jobfile.yaml', read the various YAML-entries like 'AWS,VPC,subnet,SERVERS,?MyEC2InstanceName?",yum', 'AWS,VPC,subnet,SERVERS,?MyEC2InstanceName?,configCustomCommands'..
     *  @param _inputNode NotNull Node object
     *  @param _YAMLPath NotNull String representing a COMMA-Delimited YAML-Path-String
     *  @return a possibly-Null Node (or else a runtime-assertion-exception is thrown, as determined within org.ASUX.YAML.NodeImpl.NodeTools.getScalarContent()
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    public Node readNodeFromYAML( final Node _inputNode, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readNodeFromYAML(<Node>, "+ _YAMLPath +"): ";
        this.readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final SequenceNode output = this.readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );
        // return output;
        final java.util.List<Node> seqs = output.getValue();
        if ( seqs.size() <= 0 )
            return null;
        else
            return seqs.get(0);
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
