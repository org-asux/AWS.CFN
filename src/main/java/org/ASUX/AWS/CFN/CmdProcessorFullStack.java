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
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @return a String (containing {ASUX::_} macros) that should be used to executed using BATCH-YAML-Processor within {@link CmdProcessor#genCFNShellScript}
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public String genCFNShellScript( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );
        final YAMLTools yamltools = new YAMLTools( this.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );

        final File outputFldr = new File( _cmdLA.jobSetName );
        outputFldr.mkdir(); // create a folder in '.' called '{JobSetName}'
        _envParams.outputFolderPath = outputFldr.getAbsolutePath();

        //-------------------------------------
        // read AWS,MyOrgName --delimiter ,              (put it into GLOBAL.PROPERTIES)
        // read AWS,MyEnvironment --delimiter ,          (put it into GLOBAL.PROPERTIES)
        // read AWS,AWSRegion --delimiter ,              (put it into GLOBAL.PROPERTIES)
        // read AWS,VPC,MyVPCName --delimiter ,           else: a DEFAULT value based on {MyOrgName}-{MyEnvironment}-{AWSLocation}-{user.name} ..  (put them into GLOBAL.PROPERTIES)
        // read AWS,VPC,subnet  --delimiter ,           to get _ANY_ KV-Pairs for subnet (put them into GLOBAL.PROPERTIES)
        // read AWS,VPC,subnet,SERVERS --delimiter ,     iterate over how many ever such elements exist
        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName> --delimiter ,
        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,EC2InstanceType --delimiter ,
        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,IAM-roles --delimiter ,
        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,yum --delimiter ,
        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,rpm --delimiter ,
        // read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName>,configCustomCommands --delimiter ,

        //-------------------------------------
        // Now recursively call _ALL_ the logic of this entire java-package, to generate the various VPC, subnet, SG and EC2 YAML-files and associated shell scripts
        //-------------------------------------
        // _cmdLA.verbose       <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
        // _cmdLA.quoteType     <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
        // _cmdLA.jobSetName    <-- SAME VALUE FOR ALL CMDs (as provided by user on commandline)
        // _cmdLA.PublicOrPrivate <-- SAME VALUE FOR ALL CMDs (as other commands will IGNORE this)

        //-------------------------------------
        // read the single YAML-configuration-file.. that's describing the entire-stack / fullstack
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

        //-------------------------------------
        // new org.ASUX.common.Debug(this.verbose).printAllProps( "!!!!!!!!!!!!!!!!!!!!!!!!!!", _envParams.getAllPropsRef() );

        //========================================================================
        final String AWSRegionAsIs = yamltools.readStringFromYAML( inputNode, "AWS,AWSRegion" );
        final String InitialCapitalStr = Character.toUpperCase( AWSRegionAsIs.charAt(0) ) + AWSRegionAsIs.substring(1).toLowerCase();
        // Even after 'fixing' the case ---> Title-case, perhaps.. .. end-user put in a AWSLocation (example: Tokyo) instead of AWSRegion (example: ap-northeast-1) ??
        final String macroStr = "${ASUX::AWS-"+InitialCapitalStr+"}";
        final String AWSRegionLookupStr   = Macros.evalThoroughly( this.verbose, macroStr, _envParams.getAllPropsRef() );
        final boolean bMacroEvalFailed =  macroStr.equals( AWSRegionLookupStr ); // if the Macros.evalThoroughly() actually worked..
        final String AWSRegion = ( bMacroEvalFailed ) ? AWSRegionAsIs : AWSRegionLookupStr; // if the Macros.evalThoroughly() actually worked..
        if (this.verbose) System.out.println( HDR +"macroStr="+ macroStr +"AWSRegionLookupStr="+ AWSRegionLookupStr +"bMacroEvalFailed="+ bMacroEvalFailed +"AWSRegion="+ AWSRegion );

        globalProps.setProperty( "AWSRegion", AWSRegion );
        if ( this.verbose ) System.out.println( HDR +"AWSRegion="+ AWSRegion );
        final BootCheckAndConfig boot = new BootCheckAndConfig( this.verbose, this.cmdinvoker.getMemoryAndContext().getAllPropsRef() );
        boot.envParams = _envParams;
        boot.configure( _cmdLA );   // this will set appropriate instance-variables in envParamsEC2

        //========================================================================
        // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --read YAML-command.
        // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
        //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
        // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below 2 lines
        final ReadYamlEntry readcmd = yamltools.getReadcmd(); // new ReadYamlEntry( _cmdLA.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );
        final String existingVPCID  = yamltools.readStringFromYAML( inputNode, "AWS,VPC,VPCID" );

        final String MyOrgName      = (existingVPCID == null) ? yamltools.readStringFromYAML( inputNode, "AWS,MyOrgName" )     : getVPCTag( existingVPCID, "MyOrgName", _cmdLA, _envParams );
        final String MyEnvironment  = (existingVPCID == null) ? yamltools.readStringFromYAML( inputNode, "AWS,MyEnvironment" ) : getVPCTag( existingVPCID, "MyEnvironment", _cmdLA, _envParams );
        final String MyDomainName   = (existingVPCID == null) ? yamltools.readStringFromYAML( inputNode, "AWS,MyDomainName" )  : getVPCTag( existingVPCID, "MyDomainName", _cmdLA, _envParams );
        globalProps.setProperty( "MyOrgName", MyOrgName );
        globalProps.setProperty( "MyEnvironment", MyEnvironment );
        globalProps.setProperty( EnvironmentParameters.MYDOMAINNAME, MyDomainName );
        // !!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!
        // It is _VERY_ important that these 4 above GLOBAL properties be set - BEFORE creating 'boot' object, and invoking boot.configure()
        if (this.verbose) System.out.println( HDR + "MyOrgName=" + MyOrgName + " MyEnvironment=" + MyEnvironment + " AWSRegion=" + AWSRegion + " MyDomainName=" + MyDomainName +"." );

        final String VPCIDwMacros   = Macros.evalThoroughly( this.verbose, "${ASUX::VPCID}", _envParams.getAllPropsRef() ); // set by BootCheckAndConfig!  === _envParams.MyVPCStackPrefix + "-VPCID"
        // !!!! ATTENTION !!!! I'm totally going to PREVENT end-user from specifying MyVPCName
        final String VPCID = Macros.evalThoroughly( this.verbose, VPCIDwMacros, _envParams.getAllPropsRef() );
        // !!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!            Don't ask.  I'm forced to run Macros.evalThoroughly _TWICE_ in succession, to get it to eval completely.
        globalProps.setProperty( "MyVPCName", VPCID.replaceAll("-", "") ); // Why?  CreateStack format error: Resource name org_ASUX_Playground_Tokyo_VPCID is non alphanumeric
        if ( this.verbose ) System.out.println( HDR +"VPCID="+ VPCID );

        // redo
        boot.envParams = _envParams;
        boot.configure( _cmdLA );   // this will set appropriate instance-variables in envParamsEC2

        //========================================================================
        //-------------------------- Prep for Recursion --------------------------
        //========================================================================

        //--------------- VPC -------------------
        if ( existingVPCID == null ) {
            final EnvironmentParameters envParamsVPC = EnvironmentParameters.deepClone( _envParams );
            envParamsVPC.bInRecursionByFullStack = true;
            envParamsVPC.setCmd( Enums.GenEnum.VPC );
            final CmdLineArgs claVPC     = CmdLineArgs.deepCloneWithChanges( _cmdLA, envParamsVPC.getCmdEnum(), null, null );
            boot.envParams = envParamsVPC;
            boot.configure( claVPC );   // this will set appropriate instance-variables in envParamsEC2
            // This boot.configure() will invoke the following:-
            // envParamsVPC.setHomeFolders( .. .. .. );
            // envParamsVPC.setFundamentalGlobalProps( .. .. );
            // envParamsVPC.setFundamentalPrefixes( .. .. );

            // 1st generate the YAML.
            this.cmdProcessor.genYAML( claVPC, envParamsVPC.getCfnJobTYPEString(), envParamsVPC );          // Note: the 2nd argument automtically has "fullstack-" prefix in it.
            // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
            this.cmdProcessor.genCFNShellScript( claVPC, envParamsVPC );
        }

        //----------------- SG --------------------
        if ( existingVPCID == null ) {
// ASSUMPTION: SINCE VPC-ID is specified, (too early to tell if SUBNET-ID is ALSO specified), .. so, SG __BETTER__ be specified.
// ??????? For each SG in jobSetName.yaml ...
            final EnvironmentParameters envParamsSG = EnvironmentParameters.deepClone( _envParams );
            envParamsSG.bInRecursionByFullStack = true;
            envParamsSG.setCmd( Enums.GenEnum.SGSSH );
            final CmdLineArgs claSGSSH   = CmdLineArgs.deepCloneWithChanges( _cmdLA, envParamsSG.getCmdEnum(), null, null );
            boot.envParams = envParamsSG;
            boot.configure( claSGSSH ); // this will set appropriate instance-variables in envParamsEC2
            // 1st generate the YAML.
            this.cmdProcessor.genYAML( claSGSSH, envParamsSG.getCfnJobTYPEString(), envParamsSG );           // Note: the 2nd argument automtically has "fullstack-" prefix in it.
            // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
            this.cmdProcessor.genCFNShellScript( claSGSSH, envParamsSG );
        } else {
            final String existingSSHSecurityGroup = yamltools.readStringFromYAML( inputNode, "AWS,VPC,SG-ssh,SG-ssh-ID" );
            globalProps.setProperty( "ExistingSSHSecurityGroup", existingSSHSecurityGroup ); // Why?  We've a YAML Entry in ec2-cfn YAML as:- SubnetId: My${ASUX::ExistingSubnetID}
        }

        //--------------- subnets -------------------
        final ArrayList<String> AZs = awssdk.getAZs( AWSRegion );
        final int numOfAZs = AZs.size();

        readcmd.searchYamlForPattern( inputNode, "AWS,VPC,subnet", "," );
        final SequenceNode subnetSeqN = readcmd.getOutput();
        // we know readcmd always returns a LIST (because we could have one or more matches for ANY arbitratry YAML-Path-Expression.)
        final java.util.List<Node> subnetseqs = subnetSeqN.getValue();
        if ( subnetseqs.size() < 1 )
            throw new Exception( "Under 'subnet', a child-element labelled(LHS) 'SERVERS' must be provided" );
        for ( Node subnet: subnetseqs ) {// loop over EACH subnet

            final String existingSubnetID  = yamltools.readStringFromYAML( subnet, "SubnetID" );
            if ( this.verbose ) System.out.println( HDR +" subnet YAML-tree =\n" + NodeTools.Node2YAMLString( subnet ) +"\n" );

            //---------------------------------
            String PublicOrPrivate = "public"; // by default - in case of existing Subnet ID
            if ( existingSubnetID == null ) {
                String strPublicSubnet    = "no";
                String strPrivateSubnet   = "no";
                try {
                    strPublicSubnet    = yamltools.readStringFromYAML( subnet, "public" );
                } catch( java.lang.AssertionError ae ) { /* do Nothing */ }
                try {
                    strPrivateSubnet   = yamltools.readStringFromYAML( subnet, "private" );
                } catch( java.lang.AssertionError ae ) { /* do Nothing */ }
                final boolean isPublicSubnet    = ( strPublicSubnet != null && strPublicSubnet.toLowerCase().equals("yes") );
                final boolean isPrivateSubnet   = ( strPrivateSubnet != null && strPrivateSubnet.toLowerCase().equals("yes") );

                //-------------------------------------
                if ( isPublicSubnet && ! isPrivateSubnet )
                    PublicOrPrivate = "Public"; // unless I am 100% sure, I'm _NOT_ making the subnet _PUBLIC_.
                else
                    PublicOrPrivate = "Private";
                globalProps.setProperty( "PublicOrPrivate", PublicOrPrivate );  // Override what's set in BootCheckAndConfig
        
                final String VPCCIDRBlock = globalProps.getProperty( EnvironmentParameters.VPCCIDRBLOCK ); // "172.31.0.0/20"
                final String CIDRBLOCK_Byte3_DeltaString = globalProps.getProperty( EnvironmentParameters.CIDRBLOCK_BYTE3_DELTA ); // example: 16
                final int CIDRBLOCK_Byte3_Delta = Integer.parseInt( CIDRBLOCK_Byte3_DeltaString );
                // globalProps.setProperty( "CidrBlockAZ1", "172.31.0.0/20" );
                // globalProps.setProperty( "CidrBlockAZ2", "172.31.16.0/20" );
                // globalProps.setProperty( "CidrBlockAZ3", "172.31.32.0/20" );
                // globalProps.setProperty( "CidrBlockAZ4", "172.31.48.0/20" );
                int subix = 1;
                final Inet inet = new Inet( this.verbose );
                for ( String subnetMask: inet.genSubnetRangeWithMasks( VPCCIDRBlock, numOfAZs, CIDRBLOCK_Byte3_Delta) ) {
                    globalProps.setProperty( "CidrBlockAZ" + subix, subnetMask );
                    subix ++;
                }

                //--------------- New Subnet -------------------
                final EnvironmentParameters envParamsSubnet = EnvironmentParameters.deepClone( _envParams );
                envParamsSubnet.bInRecursionByFullStack = true;
                envParamsSubnet.setCmd( Enums.GenEnum.SUBNET );
                final CmdLineArgs claSubnet  = CmdLineArgs.deepCloneWithChanges( _cmdLA, envParamsSubnet.getCmdEnum(), null, PublicOrPrivate );
                boot.envParams = envParamsSubnet;
                boot.configure( claSubnet );     // this will set appropriate instance-variables in envParamsEC2
                // 1st generate the YAML.
                this.cmdProcessor.genYAML( claSubnet, envParamsSubnet.getCfnJobTYPEString(), envParamsSubnet );          // Note: the 2nd argument automtically has "fullstack-" prefix in it.
                // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
                this.cmdProcessor.genCFNShellScript( claSubnet, envParamsSubnet );

            } // End-IF-block (IF subnetID Not provided by user within FullStackJob.yaml file)

            //--------------- Server(s) -------------------
            if ( existingSubnetID == null )
                genServerCFN( fullStackJob_Filename, subnet, PublicOrPrivate, boot, yamltools, _cmdLA, _envParams );
            else {
                globalProps.setProperty( "ExistingSubnetID", existingSubnetID ); // Why?  We've a YAML Entry in ec2-cfn YAML as:- SubnetId: My${ASUX::ExistingSubnetID}
                final EnvironmentParameters envParamsServersInExistingSubnet = EnvironmentParameters.deepClone( _envParams );
                envParamsServersInExistingSubnet.setExisting( existingVPCID, existingSubnetID );
                // above setExisting() is important.  This set flags, that change the Batch-Script to 'AWSCFN-fullstack-ec2plainExistingSubnet-Create.ASUX-batch.txt'
                genServerCFN( fullStackJob_Filename, subnet, "Public" /* assume publicly accessible server */, boot, yamltools, _cmdLA, envParamsServersInExistingSubnet );
            }

        } // for each SUBNET

        //-------------------------------------
        return null; // This is ok, ONLY for 'fullstack' command.
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>generate CFN-YAML snippets for each and every subnet within the subnet specified by the user.</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _fullStackJob_Filename a NotNull String - file name of the full-stack-job yaml file.
     *  @param _subnet a NotNull SnakeYaml Node representing the entire YAML-tree rooted at _ONE_ single subnet-LHS/Key.
     *  @param _PublicOrPrivate whether a public or private subnet EC2 instance
     *  @param _boot a NotNull instance created within {@link #genCFNShellScript(CmdLineArgs, EnvironmentParameters)}
     *  @param _yamltools a NotNull instance (created within {@link #genCFNShellScript(CmdLineArgs, EnvironmentParameters)})
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genServerCFN( final String _fullStackJob_Filename, final Node _subnet, final String _PublicOrPrivate,
                                    final BootCheckAndConfig _boot, final YAMLTools _yamltools,
                                    final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {
        final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";
        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final ReadYamlEntry readcmd = _yamltools.getReadcmd(); // new ReadYamlEntry( _cmdLA.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );

        //--------------- SERVERS -------------------
        readcmd.searchYamlForPattern( _subnet, "SERVERS", "," );
        final SequenceNode serversSeqN = readcmd.getOutput();
        // we know readcmd always returns a LIST (because we could have one or more matches for ANY arbitratry YAML-Path-Expression.)
        final java.util.List<Node> srvrseqs = serversSeqN.getValue();
        if ( srvrseqs.size() < 1 )
            throw new Exception( "Under 'subnet', a child-element labelled(LHS) 'SERVERS' must be provided" );
        for ( Node serverNode: srvrseqs ) { // loop for each EC2 / server

            // final Node serverNode = srvrseqs.get(0);
            if ( this.verbose ) System.out.println( HDR +" SERVERS(plural) YAML-tree =\n" + NodeTools.Node2YAMLString( serverNode ) +"\n" );

            //--------------- EC2 -------------------
            String ec2instanceName = null;
            if ( serverNode instanceof MappingNode ) {
                //       SERVERS:
                //          OrgASUXplayEC2plain: ### This is the name of the 1st SERVER.
                //              ..

                final MappingNode mapNode = (MappingNode) serverNode;
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
                        parseServerInfo( (MappingNode) valNode, "packages", _yamltools, _envParams );  // ????????????????????? Need to parametrize "packages"
                    } else {
                        if ( this.verbose ) System.out.println( HDR +" (server["+ ec2instanceName +"] instanceof MappingNode) is Not a mapping within:\n" + NodeTools.Node2YAMLString( mapNode ) + "\n");
                        throw new Exception( "SERVER(#"+ix+")="+ ec2instanceName +" is an Invalid Node of type: "+ valNode.getNodeId() );
                    }
                    ix ++;
                }

            } else if ( serverNode instanceof SequenceNode ) {
                //       SERVERS:
                //        -   ### Sequence of UNNAMED server
                //            ..  ..
                final SequenceNode seqNode = (SequenceNode) serverNode;
                final java.util.List<Node> seqs = seqNode.getValue();
                assertTrue( seqs.size() >= 1 );
                int ix = 0;
                for( Node seqItem: seqs ) {
                    if ( seqItem instanceof MappingNode ) {
                        parseServerInfo( (MappingNode) seqItem, "packages", _yamltools, _envParams );  // ????????????????????? Need to parametrize "packages"
                    } else {
                        if ( this.verbose ) System.out.println( HDR +" (server["+ ix +"] instanceof SequenceNode) failed with:\n" + NodeTools.Node2YAMLString( seqItem ) + "\n");
                        throw new Exception( "Invalid Node of type: "+ seqItem.getNodeId() );
                    }
                    ec2instanceName = Integer.toString( ix + 1 );
                    ix ++;
                }
            } else { // !  (serverNode instanceof MappingNode)   &&   !  (serverNode instanceof SequenceNode) )
                throw new Exception( "the content of "+ _fullStackJob_Filename +" at the YAML-path: 'AWS,VPC,subnet,SERVERS' is neither MappingNode nor SequenceNode" );
            }

            assertTrue( ec2instanceName != null );
            globalProps.setProperty( EnvironmentParameters.MYEC2INSTANCENAME, ec2instanceName );

            //-------------------------------------
            final EnvironmentParameters envParamsEC2 = EnvironmentParameters.deepClone( _envParams );
            envParamsEC2.bInRecursionByFullStack = true;
            envParamsEC2.setCmd( Enums.GenEnum.EC2PLAIN );
            final CmdLineArgs claEC2     = CmdLineArgs.deepCloneWithChanges( _cmdLA, envParamsEC2.getCmdEnum(), null, _PublicOrPrivate );
            _boot.envParams = envParamsEC2;
            _boot.configure( claEC2 ); // this will set appropriate instance-variables in envParamsEC2
            // 1st generate the YAML.
            this.cmdProcessor.genYAML( claEC2, envParamsEC2.getCfnJobTYPEString(), envParamsEC2 );               // Note: the 2nd argument automtically has "fullstack-" prefix in it.
            // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
            this.cmdProcessor.genCFNShellScript( claEC2, envParamsEC2 );

        } // for each EC2 / server
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Extract YAML-sub-trees from the 1st argument, and save them into "MemoryAndContext" for the 'AWSCFN-ec2plain-Create.ASUX-batch.txt' YamlBatchScript to use.</p>
     *  <p>Make sure the 'labels' for what's put in memory matches what is 'recalled' within the 'AWSCFN-ec2plain-Create.ASUX-batch.txt' YamlBatchScript</p>
     *  @param _mapNode should be the YAML-sub-tree determined by: read AWS,VPC,subnet,SERVERS,<MyEC2InstanceName> --delimiter ,
     *  @param _cfnInitContext typically, it's one of the AWS cfn-init ConfigSets (StandupOnly, StandUpInstallAndRun, ..)
     *  @param _yamltools a NotNull instance
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#exec})
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    private void parseServerInfo( final MappingNode _mapNode, final String _cfnInitContext, final YAMLTools _yamltools, final EnvironmentParameters _envParams )
                                throws Exception
    {   final String HDR = CLASSNAME + ": parseServerInfo(<mapNode>,"+ _cfnInitContext +"): ";

        if ( this.verbose ) System.out.println( HDR +" input is YAML-tree =\n" + NodeTools.Node2YAMLString( _mapNode ) +"\n" );

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );

        //-----------------
        final String EC2InstanceType      = _yamltools.readStringFromYAML( _mapNode, "EC2InstanceType" );
        globalProps.setProperty( EnvironmentParameters.EC2INSTANCETYPE, EC2InstanceType );  // <<----------- <<-------------

        //-----------------
        final Node IAMRoles      = _yamltools.readNodeFromYAML( _mapNode, EnvironmentParameters.EC2IAMROLES );

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
        final Node yum      = _yamltools.readNodeFromYAML( _mapNode, "yum" );
        final Node rpm      = _yamltools.readNodeFromYAML( _mapNode, "rpm" );
        final Node configCustomCommands = _yamltools.readNodeFromYAML( _mapNode, "configCustomCommands" );
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

        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( EnvironmentParameters.CFNINIT_SERVICES, null );   // <<----------- <<-------------

    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // These 2 instance-variables are SOLELY for the use by getVPCTag().. to cache AWS API responses, so that we do Not have contact AWS for every call.
    private String previouslyFoundExistingVPCID = "None-Found";
    private LinkedHashMap<String,Object> previouslyFoundMap = new LinkedHashMap<String,Object>();

    /**
     *  Given the ID of an existing VPC, see if it has specific tags (which would exist if the VPC was created by ASUX.org AWS.CFN project tools)
     *  @param _existingVPCID a NotNull string representing the VPC-ID of an existing AWS VPC
     *  @param _tagKey a NotNull string representing the key-of-a-tag, whose 'value' (if exists) will be returned
     *  @param _cmdLA to pass-on flags like '--offline' '--yamllibrary' etc..
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @return a Null-string, or, the value of an existing tag
     *  @throws Exception on any invalid VPC details provided, or any missing cached-files (for --ofline mode), etc...
     */
    public String getVPCTag( final String _existingVPCID, final String _tagKey, final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws Exception
    {   final String HDR = CLASSNAME + ": getVPCTag("+ _existingVPCID +","+ _tagKey +",<cmdLA>,<envParams>): ";

        if ( this.previouslyFoundExistingVPCID.equals( _existingVPCID) && (this.previouslyFoundMap.size() > 0)  )  {
            // do nothing.
        } else {
            final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );
            final ArrayList< LinkedHashMap<String,Object> > arrOfMaps = awssdk.getVPCs( _envParams.getAWSRegion(), /* _onlyNonDefaultVPC */ false );
            for ( LinkedHashMap<String,Object> map: arrOfMaps ) {
                final String anExistingVPCID = (String) map.get( "ID" );
                if (this.verbose) System.out.println( HDR + "Going to search Existing VPC with ID='" + anExistingVPCID +"' //\t"+ map );
                if ( _existingVPCID.equals( anExistingVPCID ) ) {
                    this.previouslyFoundExistingVPCID = anExistingVPCID;
                    this.previouslyFoundMap = map;
                    break;
                } // inner if
            } // for
        } // eif-else
        final Object o = this.previouslyFoundMap.get( _tagKey );
        if (this.verbose) System.out.println( HDR + "Lookup of Tag's value ='" + o +"'" );
        return (String) o;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
