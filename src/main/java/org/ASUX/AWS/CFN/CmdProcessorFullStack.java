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
import org.ASUX.common.Inet;
import org.ASUX.common.Tuple;
import org.ASUX.yaml.YAML_Libraries;

import org.ASUX.YAML.NodeImpl.ReadYamlEntry;
import org.ASUX.AWSSDK.AWSSDK;
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
import java.util.Date;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Files;

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
     *  @param _myEnv a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @return a String (containing {ASUX::_} macros) that should be used to executed using BATCH-YAML-Processor within {@link CmdProcessor#genCFNShellScript}
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public String genAllCFNs( final CmdLineArgs _cmdLA, final Environment _myEnv ) throws IOException, Exception
    {   final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";

        final Properties globalProps = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );
        final NodeTools nodetools = (NodeTools) this.cmdinvoker.getYAMLImplementation();
        final YAMLTools yamltools = new YAMLTools( this.verbose, /* showStats */ false, nodetools );
        final CmdProcessorExisting existingInfrastructure = new CmdProcessorExisting( this.cmdinvoker );

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

        // final java.io.InputStream is1 = new java.io.FileInputStream( fullStackJob_Filename );
        // final java.io.Reader filereader = new java.io.InputStreamReader(is1);
        // final GenericYAMLScanner yamlscanner = new GenericYAMLScanner( _cmdLA.verbose );
        // yamlscanner.setYamlLibrary( YAML_Libraries.NodeImpl_Library );
        // final Node inputNode = yamlscanner.load( filereader );
        final Node inputNode = yamltools.readYamlFile( fullStackJob_Filename );
        if ( this.verbose ) System.out.println( HDR +" file contents= '" + NodeTools.Node2YAMLString( inputNode ) + ".yaml'");

        //-------------------------------------
        // new org.ASUX.common.Debug(this.verbose).printAllProps( "!!!!!!!!!!!!!!!!!!!!!!!!!!", _myEnv.getAllPropsRef() );

        //========================================================================
        final String AWSRegionAsEnteredByUser = yamltools.readStringFromYAML( inputNode, "AWS,AWSRegion" );
        assertTrue( AWSRegionAsEnteredByUser != null );
        // final String InitialCapitalStr = Character.toUpperCase( AWSRegionAsEnteredByUser.charAt(0) ) + AWSRegionAsEnteredByUser.substring(1).toLowerCase();
        // // Even after 'fixing' the case ---> Title-case, perhaps.. .. end-user put in a AWSLocation (example: Tokyo) instead of AWSRegion (example: ap-northeast-1) ??
        // final String macroStr = "${ASUX::AWS-"+InitialCapitalStr+"}";
        // final String AWSRegionLookupStr   = Macros.evalThoroughly( this.verbose, macroStr, _myEnv.getAllPropsRef() );
        // final boolean bMacroEvalFailed =  macroStr.equals( AWSRegionLookupStr ); // if the Macros.evalThoroughly() actually worked..
        // final String AWSRegion = ( bMacroEvalFailed ) ? AWSRegionAsEnteredByUser : AWSRegionLookupStr; // if the Macros.evalThoroughly() actually worked..
        // if (this.verbose) System.out.println( HDR +"macroStr="+ macroStr +"AWSRegionLookupStr="+ AWSRegionLookupStr +"bMacroEvalFailed="+ bMacroEvalFailed +"AWSRegion="+ AWSRegion );
        // if (  !   awssdk.matchesAWSRegionPattern( AWSRegion ) ) {
        //     final String msg = "Invalid AWS-Region: "+ AWSRegion +" provided inside the YAML File: "+ fullStackJob_Filename;
        //     System.err.println( HDR +"\n\nERROR!!!!!! "+ msg +"\n" );
        //     throw new Exception( msg  );
        // }
        // final String AWSLocation = AWSRegionLookupStr; // we've now Confirmed that this contains the Location (like Ohio, Virginia, Oregon, Syndey, Seoul, ..)
        final Tuple<String,String> tuple = awssdk.getRegionAndLocation( AWSRegionAsEnteredByUser );
        if (  tuple == null ) {
            final String msg = "Invalid AWS-Region: "+ AWSRegionAsEnteredByUser +" provided inside the YAML File: "+ fullStackJob_Filename;
            System.err.println( HDR +"\n\nERROR!!!!!! "+ msg +"\n" );
            throw new Exception( msg  );
        }
        final String AWSRegion   = tuple.key;
        final String AWSLocation = tuple.val;
        globalProps.setProperty( "AWSRegion", AWSRegion );
        // The above are IMPORTANT lines.  Until we read the YAML from 'fullStackJob_Filename', we had NO clue what the 'AWSRegion' is.
        if ( this.verbose ) System.out.println( HDR +"AWSRegion="+ AWSRegion +" AWSLocation="+ AWSLocation );

        //================================== READ more from JobConfig YAML file ======================================
        // invoking org.ASUX.YAML.NodeImpl.CmdInvoker() is too generic.. especially, when I am clear as daylight that I want to invoke --read YAML-command.
        // final org.ASUX.YAML.NodeImpl.CmdInvoker nodeImplCmdInvoker = org.ASUX.YAML.NodeImpl.CmdInvoker(
        //             this.verbose, false,  _cmdInvoker.getMemoryAndContext(), (DumperOptions)_cmdInvoker.getLibraryOptionsObject() );
        // final Object outputAsIs = nodeImplCmdInvoker.processCommand( cmdlineargs, inputNode );
// above 3 lines  -versus-  below 2 lines
        final ReadYamlEntry readcmd = yamltools.getReadcmd(); // new ReadYamlEntry( _cmdLA.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );

        final String existingVPCID_asEnteredByUser  = yamltools.readStringFromYAML( inputNode, "AWS,VPC,VPCID" );
        // the above value can be either the word 'existing' .. or, something like 'vpc-0123456789';  We need to handle both variations (so, note the existingInfrastructure.getVPCID() call below)
        final String myOrgName_asEnteredByUser      = yamltools.readStringFromYAML( inputNode, "AWS,MyOrgName" );
        final String myEnvironment_asEnteredByUser  = yamltools.readStringFromYAML( inputNode, "AWS,MyEnvironment" );
        final String myDomainName_asEnteredByUser   = yamltools.readStringFromYAML( inputNode, "AWS,MyDomainName" );

        final String existingVPCID = existingInfrastructure.getVPCID( AWSRegion, existingVPCID_asEnteredByUser, myOrgName_asEnteredByUser, myEnvironment_asEnteredByUser, myDomainName_asEnteredByUser, _cmdLA.isOffline() );
        // Sanity-Checks the VPCID provided by user.. or, translates the word 'existing' into an actual usable VPCID.

        //------------------------
        // If user did NOT provide key inputs like MyOrgName and MyDomainName.. then see if user provided a VPC created by ASUX.org tools.
        final String MyOrgName      = (existingVPCID == null) ? myOrgName_asEnteredByUser     : getVPCTag( existingVPCID, "MyOrgName", _cmdLA, AWSRegion );
        if ( myOrgName_asEnteredByUser != null &&  !  myOrgName_asEnteredByUser.equals( MyOrgName ) )
            System.err.println( "Hey! The value of '"+ myOrgName_asEnteredByUser +"' for MyOrgName does _NOT_ match the corresponding-Tag for the VPC ID # "+ existingVPCID );

        final String MyEnvironment  = (existingVPCID == null) ? myEnvironment_asEnteredByUser : getVPCTag( existingVPCID, "MyEnvironment", _cmdLA, AWSRegion );
        if ( myEnvironment_asEnteredByUser != null &&  !  myEnvironment_asEnteredByUser.equals( MyEnvironment ) )
            System.err.println( "Hey! The value of '"+ myEnvironment_asEnteredByUser +"' for MyEnvironment does _NOT_ match the corresponding-Tag for the VPC ID # "+ existingVPCID );

        final String MyDomainName   = (existingVPCID == null) ? myDomainName_asEnteredByUser  : getVPCTag( existingVPCID, "MyDomainName", _cmdLA, AWSRegion );
        if ( myDomainName_asEnteredByUser != null &&  !  myDomainName_asEnteredByUser.equals( MyDomainName ) )
            System.err.println( "Hey! The value of '"+ myDomainName_asEnteredByUser +"' for MyDomainName does _NOT_ match the corresponding-Tag for the VPC ID # "+ existingVPCID );

        assertTrue( MyOrgName != null );
        assertTrue( MyEnvironment != null );
        assertTrue( MyDomainName != null );
        globalProps.setProperty( "MyOrgName", MyOrgName );
        globalProps.setProperty( "MyEnvironment", MyEnvironment );
        globalProps.setProperty( Environment.MYDOMAINNAME, MyDomainName );

        // !!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!
        // It is _VERY_ important that these 4 above GLOBAL properties be set - BEFORE creating 'boot' object, and invoking boot.configure()
        if (this.verbose) System.out.println( HDR + "MyOrgName=" + MyOrgName + " MyEnvironment=" + MyEnvironment + " AWSRegion=" + AWSRegion + " MyDomainName=" + MyDomainName +"." );

        //------------------------
        // A good chance that.. we can re-use an existing IGW.  So, let's check with existing Infrastructure in the region.
        final String IGWID = existingInfrastructure.getIGWID( AWSRegion, existingVPCID, _cmdLA.isOffline() );
        globalProps.setProperty( "MyIGW", ( IGWID == null ) ? "" : IGWID );                 // default, unless - inside the IF-below - we detect an existing IGW that we can re-use.
        globalProps.setProperty( "IGWExistingOrNew", ( IGWID == null ) ? "new" : "existing" );
        // So.. after the above code, if IGW ID is null, then .. we'll create a new IGW (within the @${ASUX::AWSCFNHOME}/bin/AWSCFN-fullstack-vpc-create.txt).

        //========================================================================
        //----------------------- Initialize a few things ------------------------
        //========================================================================
        final BootCheckAndConfig boot = new BootCheckAndConfig( this.verbose, Environment.deepClone( _myEnv ) );
        boot.myEnv.enhancedUserInput = new UserInputEnhanced( new UserInput( this.verbose, AWSRegion, AWSLocation ) );
        boot.myEnv.enhancedUserInput.setCmd( _cmdLA.cmdName, UserInputEnhanced.getCFNJobTypeAsString( _cmdLA.cmdName ) );
        // So, any existing value of _myEnv.enhancedUserInput is invalid.
        // redo - cuz we set Macro-sensitive values for 'MyOrgName' and 'MyEnvironment' and 'MyDomainName' above..
        boot.configure( _cmdLA );   // this will set appropriate instance-variables in myEnvVPC, myEnvSubnet, .. myEnvEC2
        assertTrue( boot.myEnv.getStackSet() != null );
            // it's ok if boot.myEnv.getStack() ==== null

        final Path path = FileSystems.getDefault().getPath( _myEnv.get_cwd(), _cmdLA.jobSetName );
        // final File outputFldr = new File( _cmdLA.jobSetName );
        final File newOutputFldr = path.toFile();
        newOutputFldr.mkdir(); // create a folder in '.' called '{JobSetName}'
        boot.myEnv.enhancedUserInput.setOutputFolderPath( newOutputFldr.getAbsolutePath() );

        //========================================================================
        //-------------------------- Prep for Recursion --------------------------
        //========================================================================

        //--------------- VPC -------------------
        if ( existingVPCID == null ) {
            // final Environment myEnvVPC = Environment.deepClone( boot.myEnv );
            // myEnvVPC.bInRecursionByFullStack = true;
            // myEnvVPC.enhancedUserInput.setCmd( Enums.GenEnum.VPC, UserInputEnhanced.getCFNJobTypeAsString( Enums.GenEnum.VPC ) );
            // final CmdLineArgs claVPC     = CmdLineArgs.deepCloneWithChanges( _cmdLA, myEnvVPC.enhancedUserInput.getCmd(), null, null );
            // boot.myEnv = myEnvVPC;
            // boot.configure( claVPC );   // this will set appropriate instance-variables in myEnvVPC
            // // myEnvVPC.setHomeFolders( .. .. .. );             // <-- above boot.configure() will invoke this
            // // myEnvVPC.setFundamentalGlobalProps( .. .. );     // <-- above boot.configure() will invoke this
            // // myEnvVPC.setFundamentalPrefixes( .. .. );        // <-- above boot.configure() will invoke this
            // final Stack stackVPC = new Stack( this.verbose, AWSRegion, AWSLocation );
            // boot.myEnv.setStack( stackVPC );
            // boot.myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( boot.myEnv.enhancedUserInput.getCmd(), _cmdLA, boot.myEnv ) );
            // boot.myEnv.getStackSet().add( boot.myEnv.getStack() ); // add the above new Stack object/instance
            final CmdLineArgs claVPC = CmdLineArgs.deepCloneWithChanges( _cmdLA, Enums.GenEnum.VPC, null, null );
            final Stack stackVPC = new Stack( this.verbose, AWSRegion, AWSLocation );
            this.reconfigureBoot( Enums.GenEnum.VPC, claVPC, stackVPC, boot.myEnv, boot);  // FYI: boot.myEnv gets replaced with a clone

            final String VPCNameWithMacros   = Macros.evalThoroughly( this.verbose, "${ASUX::VPCID}", boot.myEnv.getAllPropsRef() ); // set by BootCheckAndConfig  === boot.myEnv.enhancedUserInput.... + "-VPCID"
            // !!!! ATTENTION !!!! I'm totally going to PREVENT end-user from specifying MyVPCName
            final String VPCName = Macros.evalThoroughly( this.verbose, VPCNameWithMacros, boot.myEnv.getAllPropsRef() );
            // !!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!            Don't ask.  I'm forced to run Macros.evalThoroughly _TWICE_ in succession, to get it to eval completely.
            globalProps.setProperty( "MyVPCName", VPCName.replaceAll("-", "") ); // Why?  CreateStack format error: Resource name org_ASUX_Playground_Tokyo_VPCID is non alphanumeric
            if ( this.verbose ) System.out.println( HDR +"VPCName(${ASUX::VPCID})="+ VPCName );

            final String Rt53HZID = awssdk.getRt53HostedZoneId( AWSRegion, MyDomainName, false /* _needPublicHostedZone */ );
            if ( Rt53HZID == null ) {
                // since it's a new VPC + since Rt53 PRIVATE-HostedZone does _NOT_ exist.. .. let's create a NEW  Rt53PrivateZone + associate it with the VPC
                globalProps.setProperty( "Rt53VPCAssocGenScript", "AWSCFN-Rt53-VPCAssociation.txt" ); // This will cause 'bin/AWSCFN-fullstack-vpc-Create.ASUX-batch.txt' to include 'bin/AWSCFN-Rt53-VPCAssociation.txt'
            } else {
                System.err.println( "\n\t\t!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!\nCloudFormation does _Not_ allow associating a new VPC to an __EXISTING__ Rt53-HostedDomain("+ MyDomainName +" = "+ Rt53HZID +").   So.. do it yourself manually!!\n" );
            }

            // 1st generate the YAML.
            this.cmdProcessor.genYAML( claVPC, boot.myEnv.getCfnJobTYPEString(), boot.myEnv );          // Note: the 2nd argument automtically has "fullstack-" prefix in it.
            // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
            this.cmdProcessor.genCFNShellScript( claVPC, boot.myEnv );
        }

        //----------------- SG --------------------
        // let's determine if SG-ID is specified by user.
        readcmd.searchYamlForPattern( inputNode, "AWS,VPC,SG", "," );
        final SequenceNode SGs = readcmd.getOutput(); // we know readcmd always returns a LIST (because we could have one or more matches for ANY arbitratry YAML-Path-Expression.)
        final java.util.List<Node> SGseqs = SGs.getValue();
        int ix = 0;
        for ( Node SG: SGseqs ) {// loop over EACH SecurityGroup that is listed by user
            if ( this.verbose ) System.out.println( HDR +" SecurityGroup YAML-tree =\n" + NodeTools.Node2YAMLString( SG ) +"\n" );

            if ( SG instanceof SequenceNode ) {
                final SequenceNode innerSeqN = (SequenceNode) SG;
                final java.util.List<Node> innerSGseqs = innerSeqN.getValue();
                SG = innerSGseqs.get(0);
                if ( this.verbose ) System.out.println( HDR +" SecurityGroup YAML-tree =\n" + NodeTools.Node2YAMLString( SG ) +"\n" );
            } // readCmd() can do that.. it will return an array, whose elements _CAN_ be single-element-ARRAYS.  You can ONLY know for sure, based on the context/YAML you're expecting.

            final String existingSecurityGroupID_asEnteredByUser = yamltools.readStringFromYAML( SG, "SG-ID" );
            // the above value can be either the word 'existing' .. or, something like 'sg-0123456789';  We need to handle both variations (so, note the existingInfrastructure.getSGID() call in the next line)
            final String SGPortType_asEnteredByUser = yamltools.readStringFromYAML( SG, "SG-type" );
            if ( SGPortType_asEnteredByUser == null )
                throw new Exception( "'SG-type' is _REQUIRED_ inside __EACH__ 'SG' entry within the Job-definition YAML-file.  It is missing in:\n"+ NodeTools.Node2YAMLString( SGs ) );

            String existingSecurityGroup = null;
            if ( existingVPCID != null ) {
                existingSecurityGroup = existingInfrastructure.getSGID(
                                            AWSRegion, existingVPCID, existingSecurityGroupID_asEnteredByUser, SGPortType_asEnteredByUser,
                                            MyOrgName, MyEnvironment, MyDomainName, _cmdLA.isOffline() );
                globalProps.setProperty( "ExistingSSHSecurityGroup", existingSecurityGroup ); // Why?  We've a YAML Entry in 'EC2-ResourceProperties-ExistingSubnet.yaml' as:- SubnetId: My${ASUX::ExistingSubnetID}
            } // if
// !!!!!!!!!!!!!!!!!! If the user specified MULTIPLE SGs .. this code (especially 'EC2-ResourceProperties-ExistingSubnet.yaml') is NOT ready to assign multiple SGs to the EC2 instance.

            //------------
            if ( existingSecurityGroup == null ) { // So, new SG needs to be created
                // final Environment myEnvSG = Environment.deepClone( boot.myEnv );
                // myEnvSG.bInRecursionByFullStack = true;
                // myEnvSG.enhancedUserInput.setCmd( Enums.GenEnum.SG, UserInputEnhanced.getCFNJobTypeAsString( Enums.GenEnum.SG ) );
                // final CmdLineArgs claSG   = CmdLineArgs.deepCloneWithChanges( _cmdLA, myEnvSG.enhancedUserInput.getCmd(), SGPortType_asEnteredByUser+"-"+ix, null );
                // boot.myEnv = myEnvSG;
                // boot.configure( claSG ); // this will set appropriate instance-variables in myEnvSG
                // final Stack stackSG = new Stack( this.verbose, AWSRegion, AWSLocation );
                // boot.myEnv.setStack( stackSG );
                // boot.myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( boot.myEnv.enhancedUserInput.getCmd(), _cmdLA, boot.myEnv ) );
                // boot.myEnv.getStackSet().add( boot.myEnv.getStack() ); // add the above new Stack object/instance
                final CmdLineArgs claSG = CmdLineArgs.deepCloneWithChanges( _cmdLA, Enums.GenEnum.SG, "-"+ix, SGPortType_asEnteredByUser );
                                                                            // The last argument is 'PublicOrPrivate'.. .. but ..
                                                                            //  we're re-purposing '_cmdLA.PublicOrPrivate' for passing/storing
                                                                            // the SG-PORT# (ssh/https/..) as provided by user on commandline.
                final Stack stackSG = new Stack( this.verbose, AWSRegion, AWSLocation );
                this.reconfigureBoot( Enums.GenEnum.SG, claSG, stackSG, boot.myEnv, boot);  // FYI: boot.myEnv gets replaced with a clone

                // 1st generate the YAML.
                this.cmdProcessor.genYAML( claSG, boot.myEnv.getCfnJobTYPEString(), boot.myEnv );           // Note: the 2nd argument automtically has "fullstack-" prefix in it.
                // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
                this.cmdProcessor.genCFNShellScript( claSG, boot.myEnv );

                boot.myEnv.getStackSet().popDependencyHeirarchy(); // 'pop' the last StackSet Elem. repreenting the SUBNET created @ top of this FOR-LOOP.
                boot.myEnv.setStack( null );
            } // if existingSecurityGroup == null

            ix ++;
        } // for-loop

        //--------------- subnets -------------------
        final ArrayList<String> AZs = awssdk.getAZs( AWSRegion );
        final int numOfAZs = AZs.size();

        readcmd.searchYamlForPattern( inputNode, "AWS,VPC,subnet", "," );
        final SequenceNode subnetSeqN = readcmd.getOutput();
        // we know readcmd always returns a LIST (because we could have one or more matches for ANY arbitratry YAML-Path-Expression.)
        final java.util.List<Node> subnetseqs = subnetSeqN.getValue();
        if ( subnetseqs.size() < 1 )
            throw new Exception( "Under 'subnet', a child-element labelled(LHS) 'SERVERS' must be provided" );

        ix = 0;
        for ( Node subnet: subnetseqs ) {// loop over EACH subnet
            if ( this.verbose ) System.out.println( HDR +" subnet YAML-tree =\n" + NodeTools.Node2YAMLString( subnet ) +"\n" );

            final String PublicOrPrivate = boot.myEnv.enhancedUserInput.getPublicOrPrivate( yamltools, subnet );
            globalProps.setProperty( "PublicOrPrivate", PublicOrPrivate );  // Override what's set in BootCheckAndConfig

            final String existingSubnetID_asEnteredByUser  = yamltools.readStringFromYAML( subnet, "SubnetID" );
            // the above value can be either the word 'existing' .. or, something like 'subnet-0123456789';  We need to handle both variations (so, note the existingInfrastructure.getSubnetID() call in the next line)
            final String existingSubnetID = existingInfrastructure.getSubnetID(
                                    AWSRegion, existingVPCID, existingSubnetID_asEnteredByUser, PublicOrPrivate,
                                    MyOrgName, MyEnvironment, MyDomainName, _cmdLA.isOffline() );

            //---------------------------------
            if ( existingSubnetID == null ) {
                final String VPCCIDRBlock = globalProps.getProperty( Environment.VPCCIDRBLOCK ); // "172.31.0.0/20"
                final String CIDRBLOCK_Byte3_DeltaString = globalProps.getProperty( Inet.CIDRBLOCK_BYTE3_DELTA ); // example: 16
                final int CIDRBLOCK_Byte3_Delta = Integer.parseInt( CIDRBLOCK_Byte3_DeltaString );
                int subix = 1;
                final Inet inet = new Inet( this.verbose );
                for ( String subnetMask: inet.genSubnetRangeWithMasks( VPCCIDRBlock, numOfAZs, CIDRBLOCK_Byte3_Delta) ) {
                    globalProps.setProperty( "CidrBlockAZ" + subix, subnetMask );    // globalProps.setProperty( "CidrBlockAZ1", "172.31.0.0/20" )
                    subix ++;
                }

                //--------------- New Subnet -------------------
                // final Environment myEnvSubnet = Environment.deepClone( boot.myEnv );
                // myEnvSubnet.bInRecursionByFullStack = true;
                // myEnvSubnet.enhancedUserInput.setCmd( Enums.GenEnum.SUBNET, UserInputEnhanced.getCFNJobTypeAsString( Enums.GenEnum.SUBNET ) );
                // final CmdLineArgs claSubnet  = CmdLineArgs.deepCloneWithChanges( _cmdLA, myEnvSubnet.enhancedUserInput.getCmd(), ""+ix, PublicOrPrivate );
                // boot.myEnv = myEnvSubnet;
                // boot.configure( claSubnet );     // this will set appropriate instance-variables in myEnvSubnet
                // final Stack stackSubnet = new Stack( this.verbose, AWSRegion, AWSLocation );
                // boot.myEnv.setStack( stackSubnet );
                // boot.myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( boot.myEnv.enhancedUserInput.getCmd(), _cmdLA, boot.myEnv ) );
                // boot.myEnv.getStackSet().add( boot.myEnv.getStack() ); // add the above new Stack object/instance
                final CmdLineArgs claSubnet = CmdLineArgs.deepCloneWithChanges( _cmdLA, Enums.GenEnum.SUBNET, ""+ix, PublicOrPrivate );
                final Stack stackSubnet = new Stack( this.verbose, AWSRegion, AWSLocation );
                this.reconfigureBoot( Enums.GenEnum.SUBNET, claSubnet, stackSubnet, boot.myEnv, boot);  // FYI: boot.myEnv gets replaced with a clone

                // 1st generate the YAML.
                this.cmdProcessor.genYAML( claSubnet, boot.myEnv.getCfnJobTYPEString(), boot.myEnv );          // Note: the 2nd argument automtically has "fullstack-" prefix in it.
                // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
                this.cmdProcessor.genCFNShellScript( claSubnet, boot.myEnv );

            } // End-IF-block (IF subnetID Not provided by user within FullStackJob.yaml file)

            //--------------- Server(s) -------------------
            if ( existingSubnetID == null ) {
                genServerCFN( fullStackJob_Filename, AWSRegion, AWSLocation, subnet, PublicOrPrivate, boot, yamltools, _cmdLA, boot.myEnv );
                boot.myEnv.getStackSet().popDependencyHeirarchy(); // 'pop' the last StackSet Elem. repreenting the SUBNET created @ top of this FOR-LOOP.
                boot.myEnv.setStack( null );
            } else {
                // existing-subnet found. So, in the above previous-block-of-code, did _NOT_ gen-CFN for a new Subnet.
                globalProps.setProperty( "ExistingSubnetID", existingSubnetID ); // Why?  We've a YAML Entry in ec2-cfn YAML as:- SubnetId: My${ASUX::ExistingSubnetID}
                final Environment myEnv_ServersInExistingSubnet = Environment.deepClone( boot.myEnv );
                myEnv_ServersInExistingSubnet.enhancedUserInput.setExisting( existingVPCID, existingSubnetID );
                // above setExisting() is important.  This set flags, that change the Batch-Script to 'AWSCFN-fullstack-ec2plainExistingSubnet-Create.ASUX-batch.txt'
                genServerCFN( fullStackJob_Filename, AWSRegion, AWSLocation, subnet, "Public" /* assume publicly accessible server */, boot, yamltools, _cmdLA, myEnv_ServersInExistingSubnet );
            }

            ix ++;
        } // for each SUBNET


        //-------------------------------------
        //-------------------------------------
        this.createStackSetCFNTemplate( _cmdLA, boot.myEnv );
        // create the super-CFN-template + shell-script for all the stacks created.

        //-------------------------------------
        return null; // This is ok, ONLY for 'fullstack' command.
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  Internal-use only.
     * @param _newCmd usual NotNull
     * @param _claNew usual NotNull
     * @param _newstack
     * @param _myCurrentEnv
     * @param _boot
     * @throws Exception
     */
    private void reconfigureBoot( final Enums.GenEnum _newCmd,
        final CmdLineArgs _claNew, final Stack _newstack,
        final Environment _myCurrentEnv, final BootCheckAndConfig _boot )
            throws Exception
    {   final String HDR = CLASSNAME + ": reconfigureBoot(...): ";

        final Environment myEnvNew = Environment.deepClone( _myCurrentEnv );
        myEnvNew.bInRecursionByFullStack = true;
        myEnvNew.enhancedUserInput.setCmd( _newCmd, UserInputEnhanced.getCFNJobTypeAsString( _newCmd ) );

        _boot.myEnv = myEnvNew;

        _boot.myEnv.setStack( _newstack );
        _boot.myEnv.getStackSet().add( _newstack );

        _boot.configure( _claNew ); // this will set appropriate instance-variables in _boot.myEnv
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>generate CFN-YAML snippets for each and every subnet within the subnet specified by the user.</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _fullStackJob_Filename a NotNull String - file name of the full-stack-job yaml file.
     *  @param _AWSRegion pass in valid AWS region names like 'us-east-2', 'us-west-1', 'ap-northeast-1' ..
     *  @param _AWSLocation pass in valid AWS-Locations like 'virginia', 'Ohio', 'Tokyo', 'Seoul', 'Sydney' .. (case-insensitive is NOT-valid)
     *  @param _subnet a NotNull SnakeYaml Node representing the entire YAML-tree rooted at _ONE_ single subnet-LHS/Key.
     *  @param _PublicOrPrivate whether a public or private subnet EC2 instance (String value is case-sensitive.  Exact allowed values are: 'Public' 'Private')
     *  @param _boot a NotNull instance created within {@link #genAllCFNs(CmdLineArgs, Environment)}
     *  @param _yamltools a NotNull instance (created within {@link #genAllCFNs(CmdLineArgs, Environment)})
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _myEnv a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genServerCFN( final String _fullStackJob_Filename, final String _AWSRegion, final String _AWSLocation,
                                    final Node _subnet, final String _PublicOrPrivate,
                                    final BootCheckAndConfig _boot, final YAMLTools _yamltools,
                                    final CmdLineArgs _cmdLA, final Environment _myEnv ) throws IOException, Exception
    {   final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";
        final Properties globalProps = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
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
                        parseServerInfo( (MappingNode) valNode, "packages", _yamltools, _myEnv );  // ????????????????????? Need to parametrize "packages"
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
                        parseServerInfo( (MappingNode) seqItem, "packages", _yamltools, _myEnv );  // ????????????????????? Need to parametrize "packages"
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
            globalProps.setProperty( Environment.MYEC2INSTANCENAME, ec2instanceName );

            //-------------------------------------
            // final Environment myEnvEC2 = Environment.deepClone( _myEnv );
            // myEnvEC2.bInRecursionByFullStack = true;
            // myEnvEC2.enhancedUserInput.setCmd( Enums.GenEnum.EC2PLAIN, UserInputEnhanced.getCFNJobTypeAsString( Enums.GenEnum.EC2PLAIN ) );
            // final CmdLineArgs claEC2     = CmdLineArgs.deepCloneWithChanges( _cmdLA, myEnvEC2.enhancedUserInput.getCmd(), null, _PublicOrPrivate );
            // _boot.myEnv = myEnvEC2;
            // _boot.configure( claEC2 ); // this will set appropriate instance-variables in myEnvEC2
            // final Stack stackEC2 = new Stack( this.verbose, _AWSRegion, _AWSLocation );
            // _boot.myEnv.setStack( stackEC2 );
            // _boot.myEnv.getStack().setCFNTemplateFileName( InputOutput.genStackCFNFileName( _boot.myEnv.enhancedUserInput.getCmd(), _cmdLA, _boot.myEnv ) );
            // _boot.myEnv.getStackSet().add( _boot.myEnv.getStack() ); // add the above new Stack object/instance
            final CmdLineArgs claEC2 = CmdLineArgs.deepCloneWithChanges( _cmdLA, Enums.GenEnum.EC2PLAIN, null, _PublicOrPrivate );
            final Stack stackEC2 = new Stack( this.verbose, _AWSRegion, _AWSLocation );
            this.reconfigureBoot( Enums.GenEnum.EC2PLAIN, claEC2, stackEC2, _myEnv, _boot);  // FYI: boot.myEnv gets replaced with a clone

            // 1st generate the YAML.
            this.cmdProcessor.genYAML( claEC2, _boot.myEnv.getCfnJobTYPEString(), _boot.myEnv );               // Note: the 2nd argument automtically has "fullstack-" prefix in it.
            // 2nd generate the .SHELL script to invoke AWS CLI for Cloudformatoin, with the above generated YAML
            this.cmdProcessor.genCFNShellScript( claEC2, _boot.myEnv );

            _boot.myEnv.getStackSet().popDependencyHeirarchy(); // 'pop' the last StackSet Elem. representing the EC2 instance immediately above..
            _boot.myEnv.setStack( null );
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
     *  @param _myEnv a NotNull object (created by {@link BootCheckAndConfig#exec})
     *  @throws Exception logic inside method will throw if the right YAML-structure is not provided, to read simple KV-pairs.
     */
    private void parseServerInfo( final MappingNode _mapNode, final String _cfnInitContext, final YAMLTools _yamltools, final Environment _myEnv )
                                throws Exception
    {   final String HDR = CLASSNAME + ": parseServerInfo(<mapNode>,"+ _cfnInitContext +"): ";

        if ( this.verbose ) System.out.println( HDR +" input is YAML-tree =\n" + NodeTools.Node2YAMLString( _mapNode ) +"\n" );

        final Properties globalProps = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );

        //-----------------
        final String EC2InstanceType      = _yamltools.readStringFromYAML( _mapNode, "EC2InstanceType" );
        globalProps.setProperty( Environment.EC2INSTANCETYPE, EC2InstanceType );  // <<----------- <<-------------

        //-----------------
        final Node IAMRoles = _yamltools.readNodeFromYAML( _mapNode, Environment.EC2IAMROLES );

        if ( IAMRoles instanceof ScalarNode ) {
            final ScalarNode scalar = (ScalarNode) IAMRoles;
            globalProps.setProperty( Environment.EC2IAMROLES, scalar.getValue() );
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
                globalProps.setProperty( Environment.EC2IAMROLES, strBuf.toString() );  // <<----------- <<-------------
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
        // final Node parent   = NodeTools.getNewSingleMap( "Packages", "", nodetools.getDumperOptions() );

        final NodeTools nodetools = (NodeTools) this.cmdinvoker.getYAMLImplementation();

        final java.util.List<NodeTuple> tuples = new LinkedList<NodeTuple>();
        if ( yum != null ) { // && yum.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "yum", null, null, nodetools.getDumperOptions().getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, yum );
            tuples.add( tuple );
        }
        if ( rpm != null ) { // && rpm.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "rpm", null, null, nodetools.getDumperOptions().getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, rpm );
            tuples.add( tuple );
        }
        if ( configCustomCommands != null ) { // && configCustomCommands.getValue().size() > 0 ) {
            final ScalarNode keyN = new ScalarNode( Tag.STR, "configCustomCommands", null, null, nodetools.getDumperOptions().getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
            final NodeTuple tuple = new NodeTuple( keyN, configCustomCommands );
            tuples.add( tuple );
        }

        // This will be the YAML-CONTENTS __UNDER__  'Metadata' / 'AWS::CloudFormation::Init' / 'Standup' / 'Packages'
        final MappingNode parentMapN = new MappingNode ( Tag.MAP, false, tuples, null, null, nodetools.getDumperOptions().getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK

        //-----------------
        // now.. create the topmost '_cfnInitContext: ' YAML-entry (for 'AWS::CloudFormation::Init')
        final java.util.List<NodeTuple> tuples2 = new LinkedList<NodeTuple>();
        final ScalarNode keyN2 = new ScalarNode( Tag.STR, _cfnInitContext, null, null, nodetools.getDumperOptions().getDefaultScalarStyle() ); // DumperOptions.ScalarStyle.PLAIN
        final NodeTuple tuple2 = new NodeTuple( keyN2, parentMapN );
        tuples2.add( tuple2 );
        final MappingNode superParentMapN = new MappingNode ( Tag.MAP, false, tuples2, null, null, nodetools.getDumperOptions().getDefaultFlowStyle() ); // DumperOptions.FlowStyle.BLOCK

        //-----------------
        this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( Environment.CFNINIT_PACKAGES, superParentMapN );   // <<----------- <<-------------
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( Environment.CFNINIT_PACKAGES +".yum", yum );
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( Environment.CFNINIT_PACKAGES +".rpm", rpm );
        // this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( Environment.CFNINIT_PACKAGES +".configCustomCommands", configCustomCommands );

        //-----------------
        if ( this.verbose ) System.out.println( HDR +"_mapNode =\n"+ NodeTools.Node2YAMLString( _mapNode ) );
        Node services = _yamltools.readNodeFromYAML( _mapNode, "Services" );
        if ( services == null ) {
            // That is, "Services" is missing within the Full-stack YAML-file
            services = _yamltools.readUserDefaultsYamlFile( "EC2-Services" ); // This is VERY likely to throw an EXCEPTION.  If so, it indicates a _LOT_ of missing info!!!
        }
        if ( this.verbose ) System.out.println( HDR +"YAML for CFNINIT_PACKAGES/Services =\n"+ NodeTools.Node2YAMLString( services ) );
        this.cmdinvoker.getMemoryAndContext().saveDataIntoMemory( Environment.CFNINIT_SERVICES, services );   // <<----------- <<-------------

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
     *  @param _AWSRegion pass in valid AWS region names like 'us-east-2', 'us-west-1', 'ap-northeast-1' ..
     *  @return a Null-string, or, the value of an existing tag
     *  @throws Exception on any invalid VPC details provided, or any missing cached-files (for --ofline mode), etc...
     */
    public String getVPCTag( final String _existingVPCID, final String _tagKey, final CmdLineArgs _cmdLA, final String _AWSRegion ) throws Exception
    {   final String HDR = CLASSNAME + ": getVPCTag("+ _existingVPCID +","+ _tagKey +",<cmdLA>,<myEnv>): ";

        if ( this.previouslyFoundExistingVPCID.equals( _existingVPCID) && (this.previouslyFoundMap.size() > 0)  )  {
            // do nothing.
            if (this.verbose) System.out.println( HDR + "Speeding VPC-details lookup using CACHED-information for _existingVPCID='" + _existingVPCID +"'\n"+ this.previouslyFoundMap );
        } else {
            final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );
            final ArrayList< LinkedHashMap<String,Object> > arrOfMaps = awssdk.getVPCs( _AWSRegion, /* _onlyNonDefaultVPC */ false );
            for ( LinkedHashMap<String,Object> map: arrOfMaps ) {
                final String anExistingVPCID = (String) map.get( AWSSDK.VPC_ID );
                if (this.verbose) System.out.println( HDR + "Going to search Existing VPC with ID='" + anExistingVPCID +"'\n"+ map );
                if ( _existingVPCID.equals( anExistingVPCID ) ) {
                    if (this.verbose) System.out.println( HDR + "Found my VPC ID='" + anExistingVPCID +"'" );
                    this.previouslyFoundExistingVPCID = anExistingVPCID;
                    this.previouslyFoundMap = map;
                    break;
                } // inner if
            } // for
        } // if-else

        final Object o = this.previouslyFoundMap.get( _tagKey );
        if (this.verbose) System.out.println( HDR + "Lookup of ["+ _tagKey +"] Tag's value ='" + o +"'" );
        return (String) o;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>This should be invoked as the final step, after repeatedly invoking {@link CmdProcessor#genYAML(CmdLineArgs, String, Environment)} and {@link CmdProcessor#genCFNShellScript(CmdLineArgs, Environment)}.</p>
     *  <p>This method will generate the Stack-Set YAML, so that all the various components are run as a single set of Nested Stacks (very convenient, rather than run each one-after-another, waiting for each to complete)</p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _myEnv a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws Exception any errors while interacting with AWS-S3 or in writing to local file-system
     */
    public void createStackSetCFNTemplate( final CmdLineArgs _cmdLA, final Environment _myEnv ) throws Exception
    {   final String HDR = CLASSNAME + ": createStackSetCFNTemplate(): ";
        if ( _cmdLA.s3bucketname == null || "".equals( _cmdLA.s3bucketname )  ) {
            System.out.println( "Not generating StackSET." );
            return;
        }

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline()  );

        final Tuple<String,String> tuple = awssdk.parseS3Bucketname( _cmdLA.s3bucketname ); // splits "bucketname@eu-west-1" into 'bucketname' & 'eu-west-1'
        final String properBucketName = tuple.key; // if _cmdLA.s3bucketname was null, this will be null too.
        final String correctBucketRegionID = ( tuple.val == null || "".equals(tuple.val) )   ?   _myEnv.enhancedUserInput.getAWSRegion() : tuple.val;
        if (  !   awssdk.matchesAWSRegionPattern( correctBucketRegionID ) ) {
            System.err.println( "\n\nERROR!!!!!! Invalid AWS Region: "+ tuple.val );
            return;
        }
        if ( awssdk.doesBucketExist( properBucketName ) ) {
            final boolean haveAccess = awssdk.isValidS3Bucket( correctBucketRegionID, properBucketName )
                                    && awssdk.haveS3BucketAccess( correctBucketRegionID, properBucketName, AWSSDK.S3Permissions.READWRITE ); 
            if (  ! haveAccess  ) {
                System.err.println( "\n\nERROR!!!!!! You do _NOT_ have access to the Bucket that you provided on command line: "+ _cmdLA.s3bucketname );
                // awssdk.listBuckets( correctBucketRegionID ); // show the buckets that the user has in the region?
                return;
            }
        } else {
            System.err.println( "\n\nERROR!!!!!! Invalid Bucketname provided on command line: "+ _cmdLA.s3bucketname );
            return;
        }
        final String s3BucketHTTPSURL = "https://"+ properBucketName +".s3."+ correctBucketRegionID +".amazonaws.com";

        final StringBuffer bufferYAML = new StringBuffer();
        final StringBuffer bufferShellScript = new StringBuffer();
        bufferYAML.append( "AWSTemplateFormatVersion: '2010-09-09'\n" );
        bufferYAML.append( "Description: This CloudFormation StackSet deploys multiple AWS-specific CloudFormation-templates - as created using ASUX.org tools for Jobset '" );
        bufferYAML.append( _cmdLA.jobSetName ).append( "' on " ).append( new Date() ).append( " within Working-folder '" ).append( Environment.get_cwd() ).append("'\n");
        // Parameters:
        //		AWSprofile:
        //	 		Type: String
        //			Description: Your AWS Profile under ~/.aws/config that refers to the CLI KeyPair

        if ( this.verbose ) System.out.println( HDR + " # of Stacks Created ="+ _myEnv.getStackSet().getAllStacksCreated().size() );
        bufferYAML.append( "\nResources:\n\n" );
        // String dependsOn = null;
        int ix = -1;
        for ( Stack stackCmd:   _myEnv.getStackSet().getAllStacksCreated()   ) {

            ix ++; // starts counting from zero
            CmdProcessor.evalMacros( this.verbose, stackCmd, _myEnv ); // just to be extra-safe.. .. rerun-the macro-processor on all variables in Environment.java & userInputEnhanced.java

            final String s3ObjectURL = "s3://"+ properBucketName +"/"+ stackCmd.getStackName();
            final String s3ObjectHTTPSURL = s3BucketHTTPSURL +"/"+ stackCmd.getStackName();
            // if (  !  _cmdLA.isOffline()  ) {
            // !!!!!!!!!!! DO NOT UNCOMMENT THESE LINES !!!!!!!!!!!
            // Unless you make arrangements to enhance awssdk.S3put() to take on '--acl' options via API.
            //     if ( this.verbose ) System.out.println( HDR + "About to upload "+ stackCmd.getCFNTemplateFileName() +" as S3-object at s3://"+ stackCmd.getStackName() +"/..." );
            //     awssdk.S3put( correctBucketRegionID, _cmdLA.s3bucketname,  stackCmd.getStackName() /* _S3ObjectName */,   stackCmd.getCFNTemplateFileName() /* _filepathString */ );
            //     if ( this.verbose ) System.out.println( HDR + "Completed upload to "+ s3ObjectURL );
            // }
            final Stack dependsOnObj = _myEnv.getStackSet().getDependencyFor( ix );
            final String dependsOn = (dependsOnObj == null) ? null : Macros.evalThoroughly( this.verbose,    dependsOnObj.getStackId(),    _myEnv.getAllPropsRef() );
            final Tuple<String,String> tuple22 = stackCmd.getCFNYAMLString( s3ObjectHTTPSURL, dependsOn );
            // update: dependsOn = tuple22.key;
            bufferYAML.append( tuple22.val ).append("\n");
            bufferShellScript.append( "aws s3 cp --profile ${AWSprofile} --acl public-read  " ) // make the S3 object automatically publicly readable.
                            .append( _myEnv.enhancedUserInput.getOutputFolderPath() ).append( "/" ).append( stackCmd.getCFNTemplateFileName() ).append("   ").append( s3ObjectURL )
                            .append( " --region " ).append( correctBucketRegionID ).append( "\n" );
        }

        // Outputs:
        //   StackRef:
        //     Value: !Ref myStack
        //   OutputFromNestedStack:
        //     Value: !GetAtt myStack.Outputs.BucketName

        final String yamlfile = _myEnv.enhancedUserInput.getOutputFolderPath() +"/stackset.yaml";
        org.ASUX.common.IOUtils.write2File( yamlfile, bufferYAML.toString() );
        org.ASUX.common.IOUtils.setFilePerms( this.verbose, yamlfile, true, true, false, true ); // readable, writeable, executable, ownerOnly
        System.out.println( yamlfile );

        // final String MyVPCStackPrefix = Macros.evalThoroughly( this.verbose, _myEnv.enhancedUserInput.getMyStackNamePrefix(), _myEnv.getAllPropsRef() );
        bufferShellScript.append( "aws cloudformation create-stack --profile ${AWSprofile} --stack-name " ).append( _myEnv.enhancedUserInput.getMyStackNamePrefix() )
                        .append( " --region " ).append( _myEnv.enhancedUserInput.getAWSRegion() )
                        .append( " --template-body file://")
                        .append( yamlfile ).append( "\n" );

        final String scriptfile = _myEnv.enhancedUserInput.getOutputFolderPath() +"/stackset.sh";
        org.ASUX.common.IOUtils.write2File( scriptfile, bufferShellScript.toString() );
        org.ASUX.common.IOUtils.setFilePerms( this.verbose, scriptfile, true, true, true, true ); // rwx------ file-permissions
        System.out.println( scriptfile );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
