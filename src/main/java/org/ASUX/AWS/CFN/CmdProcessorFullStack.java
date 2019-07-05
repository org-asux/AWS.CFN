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
public final class CmdProcessorFullStack
{
    public static final String CLASSNAME = CmdProcessorFullStack.class.getName();

    public boolean verbose;
    protected CmdProcessor cmdProcessor;
    protected CmdInvoker cmdinvoker;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public CmdProcessorFullStack( final CmdProcessor _cmdProcessor, final CmdInvoker _cmdinvoker ) {
        this.verbose = _cmdProcessor.verbose;
        this.cmdProcessor = _cmdProcessor;
        this.cmdinvoker = _cmdinvoker;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _envParams a NotNull object (created by {@link BootstrapAndChecks#exec})
     *  @return a String (containing {ASUX::_} macros) that should be used to executed using BATCH-YAML-Processor within {@link CmdProcessor#genCFNShellScript}
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public String genCFNShellScript( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );

        String preStr = null;

        //-------------------------------------
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
        // read the single configuration file describing the entire stack
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

        //--------------- VPC -------------------
        // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --read YAML-command.
        // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
        //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
        // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below method
        final ReadYamlEntry readcmd = new ReadYamlEntry( _cmdLA.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );
        final String MyOrgName      = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,MyOrgName" );
        final String MyEnvironment  = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,MyEnvironment" );
        final String AWSRegion      = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,AWSRegion" );
        // final String VPCName        = readStringFromFullStackJobConfig( inputNode, readcmd, "AWS,VPC,VPCName" );  I'm totally going to PREVENT end-user from specifying VPCName
        final String VPCName        = Macros.evalThoroughly( this.verbose, "${ASUX::VPCID}", _envParams.getAllPropsRef() ); // set by BootstrapAndChecks!  === _envParams.MyVPCStackPrefix + "-VPCID"

        //--------------- subnets -------------------
        readcmd.searchYamlForPattern( inputNode, "AWS,VPC,subnet", "," );
        final SequenceNode subnetSeqN = readcmd.getOutput();
        // we know readcmd always returns a LIST (because we could have one or more matches for ANY arbitratry YAML-Path-Expression.)
        final java.util.List<Node> subnetseqs = subnetSeqN.getValue();
        if ( subnetseqs.size() < 1 )
            throw new Exception( "Under 'subnet', a child-element labelled(LHS) 'SERVERS' must be provided" );
        final Node subnet = subnetseqs.get(0);
// ????????????? We should loop over the subnetseqs
        if ( this.verbose ) System.out.println( HDR +" subnet YAML-tree =\n" + NodeTools.Node2YAMLString( subnet ) +"\n" );

        String strPublicSubnet    = "no";
        String strPrivateSubnet   = "no";
        try {
            strPublicSubnet    = readStringFromFullStackJobConfig( subnet, readcmd, "public" );
        } catch( java.lang.AssertionError ae ) { /* do Nothing */ }
        try {
            strPrivateSubnet   = readStringFromFullStackJobConfig( subnet, readcmd, "private" );
        } catch( java.lang.AssertionError ae ) { /* do Nothing */ }
        final boolean isPublicSubnet    = ( strPublicSubnet != null && strPublicSubnet.toLowerCase().equals("yes") );
        final boolean isPrivateSubnet   = ( strPrivateSubnet != null && strPrivateSubnet.toLowerCase().equals("yes") );

        //--------------- SERVERS -------------------
        readcmd.searchYamlForPattern( subnet, "SERVERS", "," );
        final SequenceNode serversSeqN = readcmd.getOutput();
        // we know readcmd always returns a LIST (because we could have one or more matches for ANY arbitratry YAML-Path-Expression.)
        final java.util.List<Node> srvrseqs = serversSeqN.getValue();
        if ( srvrseqs.size() < 1 )
            throw new Exception( "Under 'subnet', a child-element labelled(LHS) 'SERVERS' must be provided" );
// ????????????? We should loop over the srvrseqs
        final Node servers = srvrseqs.get(0);
        if ( this.verbose ) System.out.println( HDR +" SERVERS(plural) YAML-tree =\n" + NodeTools.Node2YAMLString( servers ) +"\n" );

        //--------------- EC2 -------------------
        String ec2instanceName = null;
        if ( servers instanceof MappingNode ) {
            //       SERVERS:
            //          OrgASUXplayEC2plain: ### This is the name of the 1st SERVER.
            //              ..

            final MappingNode mapNode = (MappingNode) servers;
            final java.util.List<NodeTuple> tuples = mapNode.getValue();
            assertTrue( tuples.size() > 0 );
            int ix = 0;
            for( NodeTuple kv: tuples ) {
                final Node keyNode = kv.getKeyNode();
                assertTrue( keyNode instanceof ScalarNode );
                // @SuppressWarnings("unchecked")
                final ScalarNode scalarKey = (ScalarNode) keyNode;
                ec2instanceName = scalarKey.getValue();
                final Node valNode = kv.getValueNode();
                if ( this.verbose ) System.out.println( HDR +" SERVER(#"+ix+") YAML-tree =\n" + NodeTools.Node2YAMLString( valNode ) +"\n" );
                if ( valNode instanceof MappingNode ) {
                    parseServerInfo( (MappingNode) valNode, readcmd, _envParams, "packages" );  // ????????????????????? Need to parametrize "packages"
                } else {
                    if ( this.verbose ) System.out.println( HDR +" (servers["+ ec2instanceName +"] instanceof MappingNode) is Not a mapping within:\n" + NodeTools.Node2YAMLString( mapNode ) + "\n");
                    throw new Exception( "SERVER(#"+ix+")="+ ec2instanceName +" is an Invalid Node of type: "+ valNode.getNodeId() );
                }
                ix ++;
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
                    parseServerInfo( (MappingNode) seqItem, readcmd, _envParams, "packages" );  // ????????????????????? Need to parametrize "packages"
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
        // Now recursively call _ALL_ the logic of this entire java-package, to generate the various VPC, subnet, SG and EC2 YAML-files and associated shell scripts
        //-------------------------------------
        // _cmdLA.verbose       <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
        // _cmdLA.quoteType     <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
        // _cmdLA.jobSetName    <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
        // _cmdLA.publicOrPrivateSubnet <-- SAME VALUE FOR ALL CMDs (as other commands will IGNORE this)

        //-------------------------------------
        Enums.GenEnum cmd;
        final BootstrapAndChecks boot = new BootstrapAndChecks( this.verbose, this.cmdinvoker.getMemoryAndContext().getAllPropsRef() );


        cmd = Enums.GenEnum.VPC;
        final EnvironmentParameters envParamsVPC = EnvironmentParameters.deepClone( _envParams );
        envParamsVPC.cfnJobTYPEString = BootstrapAndChecks.getCFNJobTypeAsString( cmd );
        final CmdLineArgs claVPC     = CmdLineArgs.deepCloneWithChanges( _cmdLA, cmd, null, null );
        boot.configure( claVPC.getCmdName(), claVPC.getJobSetName(), claVPC.getItemNumber() );
        // 1st generate the YAML.
        this.cmdProcessor.genYAML( claVPC, envParamsVPC.cfnJobTYPEString, envParamsVPC );
        // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
        this.cmdProcessor.genCFNShellScript( claVPC, envParamsVPC );

        //-------------------------------------
        String publicOrPrivateSubnet;
        if ( isPublicSubnet && ! isPrivateSubnet )
            publicOrPrivateSubnet = "public"; // unless I am 100% sure, I'm not making the subnet public.
        else
            publicOrPrivateSubnet = "private";

        globalProps.setProperty( "CidrBlockAZ1", "172.31.0.0/20" );
        globalProps.setProperty( "CidrBlockAZ2", "172.31.16.0/20" );
        globalProps.setProperty( "CidrBlockAZ3", "172.31.32.0/20" );
        globalProps.setProperty( "CidrBlockAZ4", "172.31.48.0/20" );

// ??????? For each subnet in jobSetName.yaml ...
        cmd = Enums.GenEnum.SUBNET;
        final EnvironmentParameters envParamsSubnet = EnvironmentParameters.deepClone( _envParams );
        envParamsSubnet.cfnJobTYPEString = BootstrapAndChecks.getCFNJobTypeAsString( cmd );
        final CmdLineArgs claSubnet  = CmdLineArgs.deepCloneWithChanges( _cmdLA, cmd, null, publicOrPrivateSubnet );
        boot.configure( claSubnet.getCmdName(), claSubnet.getJobSetName(), claSubnet.getItemNumber() );
        // 1st generate the YAML.
        this.cmdProcessor.genYAML( claSubnet, envParamsSubnet.cfnJobTYPEString, envParamsSubnet );
        // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
        this.cmdProcessor.genCFNShellScript( claSubnet, envParamsSubnet );

        //-------------------------------------
// ??????? For each SG in jobSetName.yaml ...
        cmd = Enums.GenEnum.SGSSH;
        final EnvironmentParameters envParamsSG = EnvironmentParameters.deepClone( _envParams );
        envParamsSG.cfnJobTYPEString = BootstrapAndChecks.getCFNJobTypeAsString( cmd );
        final CmdLineArgs claSGSSH   = CmdLineArgs.deepCloneWithChanges( _cmdLA, cmd, null, null );
        boot.configure( claSGSSH.getCmdName(), claSGSSH.getJobSetName(), claSGSSH.getItemNumber() );
        // 1st generate the YAML.
        this.cmdProcessor.genYAML( claSGSSH, envParamsSG.cfnJobTYPEString, envParamsSG );
        // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
        this.cmdProcessor.genCFNShellScript( claSGSSH, envParamsSG );

        //-------------------------------------
// ??????? For each SERVER in jobSetName.yaml ...
        cmd = Enums.GenEnum.EC2PLAIN;
        final EnvironmentParameters envParamsEC2 = EnvironmentParameters.deepClone( _envParams );
        envParamsEC2.cfnJobTYPEString = BootstrapAndChecks.getCFNJobTypeAsString( cmd );
        final CmdLineArgs claEC2     = CmdLineArgs.deepCloneWithChanges( _cmdLA, cmd, null, null );
        boot.configure( claEC2.getCmdName(), claEC2.getJobSetName(), claEC2.getItemNumber() );
        // 1st generate the YAML.
        this.cmdProcessor.genYAML( claEC2, envParamsEC2.cfnJobTYPEString, envParamsEC2 );
        // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
        this.cmdProcessor.genCFNShellScript( claEC2, envParamsEC2 );

        //-------------------------------------
        return preStr;
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
        if ( this.verbose ) System.out.println( HDR +" input is YAML-tree =\n" + NodeTools.Node2YAMLString( _mapNode ) +"\n" );

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );

        //-----------------
        final String EC2InstanceType      = readStringFromFullStackJobConfig( _mapNode, _readcmd, "EC2InstanceType" );
        globalProps.setProperty( EnvironmentParameters.EC2INSTANCETYPE, EC2InstanceType );  // <<----------- <<-------------

        //-----------------
        final Node IAMRoles      = readNodeFromFullStackJobConfig( _mapNode, _readcmd, EnvironmentParameters.EC2IAMROLES );

        if ( IAMRoles instanceof ScalarNode ) {
            final ScalarNode scalar = (ScalarNode) IAMRoles;
            globalProps.setProperty( EnvironmentParameters.EC2IAMROLES, scalar.getValue() );
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
                globalProps.setProperty( EnvironmentParameters.EC2IAMROLES, strBuf.toString() );  // <<----------- <<-------------
            } else {
                if ( this.verbose ) System.out.println( HDR +" IAMRoles failed with:\n" + NodeTools.Node2YAMLString( IAMRoles ) + "\n");
                throw new Exception( "Invalid Node of type: "+ seqs.get(0).getNodeId() );
            }
        } else {
            if ( this.verbose ) System.out.println( HDR +" IAMRoles failed with:\n" + NodeTools.Node2YAMLString( IAMRoles ) + "\n");
            throw new Exception( "Invalid Node of type: "+ IAMRoles.getNodeId() );
        }

        //-----------------
        final Node yum      = readNodeFromFullStackJobConfig( _mapNode, _readcmd, "yum" );
        final Node rpm      = readNodeFromFullStackJobConfig( _mapNode, _readcmd, "rpm" );
        final Node configCustomCommands = readNodeFromFullStackJobConfig( _mapNode, _readcmd, "configCustomCommands" );
        // final Node parent   = NodeTools.getNewSingleMap( "Packages", "", this.cmdinvoker.dumperopt );
        final java.util.List<NodeTuple> tuples = new LinkedList<NodeTuple>();
        if ( yum != null ) { // && yum.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "yum", null, null, this.cmdinvoker.dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, yum );
            tuples.add( tuple );
        }
        if ( rpm != null ) { // && rpm.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "rpm", null, null, this.cmdinvoker.dumperopt.getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, rpm );
            tuples.add( tuple );
        }
        if ( configCustomCommands != null ) { // && configCustomCommands.getValue().size() > 0 ) {
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
        this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( EnvironmentParameters.CFNINIT_PACKAGES, superParentMapN );   // <<----------- <<-------------
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( EnvironmentParameters.CFNINIT_PACKAGES +".yum", yum );
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( EnvironmentParameters.CFNINIT_PACKAGES +".rpm", rpm );
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( EnvironmentParameters.CFNINIT_PACKAGES +".configCustomCommands", configCustomCommands );

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
     *  @return a possibly-Null Node (or else a runtime-assertion-exception is thrown, as determined within {@link #getOnlyContent}
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    private Node readNodeFromFullStackJobConfig( final Node _inputNode, final ReadYamlEntry _readcmd, final String _YAMLPath ) throws Exception {
        final String HDR = CLASSNAME + ": readNodeFromFullStackJobConfig(<Node>, "+ _YAMLPath +"): ";
        _readcmd.searchYamlForPattern( _inputNode, _YAMLPath, "," );
        final SequenceNode output = _readcmd.getOutput();
        if ( this.verbose ) System.out.println( HDR +" output =\n" + NodeTools.Node2YAMLString( output ) +"\n" );
        final java.util.List<Node> seqs = output.getValue();
        if ( seqs.size() <= 0 )
            return null;
        else
            return seqs.get(0);
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

};
