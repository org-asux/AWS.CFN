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
public final class CmdProcessorExisting
{
    public static final String CLASSNAME = CmdProcessorExisting.class.getName();

    public boolean verbose;
    protected CmdProcessor cmdProcessor;
    protected CmdInvoker cmdinvoker;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public CmdProcessorExisting( final CmdProcessor _cmdProcessor, final CmdInvoker _cmdinvoker ) {
        this.verbose = _cmdProcessor.verbose;
        this.cmdProcessor = _cmdProcessor;
        this.cmdinvoker = _cmdinvoker;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  if '_VPCID' === 'existing', then .. Code will search for VPCs in the following sequence :-
     *  (1) Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above
     *  (2) Tag/Name: VPC-by-ASUX.org-${ASUX::JobSetName}
     *  (3) Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.
     *  (4) There is only one NON-Default-VPC (that this AWSProfile has access to, in this region)_
     *  (5) There is only one VPC that is a DEFAULT-VPC
     *  @param _regionStr NotNull string for the AWSRegion (Not the AWSLocation)
     *  @param _VPCID either 'existing' or any ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#AWSID_REGEXP_SUFFIX}.  This method checks against that rule.
     *  @param _MyOrgName NotNull string like 'example.org' or 'MySubsidiary'
     *  @param _MyEnvironment NotNull string lile Production, UAT, development, Dev, .. ..
     *  @param _MyDomainName a NotNull string like 'subdomain.example.com'
     *  @param _offline 'true' === this entire class and all it's methods will use "cached" output (a.k.a. files under {ASUXCFNHOME}/configu/inputs folder), instead of invoking AWS SDK calls.
     *  @return a NotNull String (guaranteed), else exception is thrown
     *  @throws Exception is _VPCID argument is neither  vpc-[0-9a-f]+   or   === 'existing' .. .. or, if No VPC could be found
     */
    public String getVPCID( final String _regionStr, final String _VPCID,
                            final String _MyOrgName, final String _MyEnvironment, final String _MyDomainName,
                            final boolean _offline )
                            throws Exception
    {   final String HDR = CLASSNAME + ": getVPCID(): ";
        assertTrue( _VPCID != null );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _offline );

        //-------------------
        // (0) Whether user provided an ACTUAL VPC-ID
        if (  awssdk.isValidAWSID( _VPCID, "vpc" ) ) {
            if ( this.verbose ) System.out.println( HDR +"Assuming that "+ _VPCID +" is a valid AWS-ID. So, no need to lookup anything within the region" );
            return _VPCID;
        } else if (  !  "existing".equals(_VPCID) ) {
            throw new Exception( "VPC ID "+ _VPCID + " is invalid.  Its neither ==='existing' nor it is like vpc-"+ org.ASUX.AWSSDK.AWSSDK.AWSID_REGEXP_SUFFIX );
        }
        // if we are here, the _VPCID === 'existing'
        // So.. let's go find a suitable VPC for the lazy user.

        //-------------------

        final ArrayList< LinkedHashMap<String,Object> > vpcs = awssdk.getVPCs( _regionStr, false /* _onlyNonDefaultVPC */);

        // (1) Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above
        if ( _MyOrgName != null && _MyDomainName != null ) {
            for( LinkedHashMap<String,Object> vpc: vpcs ) {
                if ( _MyOrgName.equals(vpc.get(EnvironmentParameters.MYORGNAME)) &&  _MyDomainName.equals(vpc.get(EnvironmentParameters.MYDOMAINNAME)) ) {
                    final String s = (String) vpc.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
                    if ( this.verbose ) System.out.println( HDR +"Found a VPC that matched the OrgName and DomainName provided: '"+ s +"' "+ vpc );
                    return s;
                }
            } // for
        } // else fall-thru.. ..
        //-------------------
        // (2) Tag/Name: VPC-by-ASUX.org-${ASUX::JobSetName}
        for( LinkedHashMap<String,Object> vpc: vpcs ) {
            final String tag_name = (String) vpc.get("Name");
            if ( tag_name != null && tag_name.startsWith("vpc") ) {
                final String s = (String) vpc.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
                if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ vpc );
                return s;
            }
        } // for
        // if none found, fall-thru.. ..
        //-------------------
        // (3) Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.

            // NOTE:  (3) is NOT IMPLEMENTED FOR NOW

        //-------------------
        // (4) There is only one NON-Default-VPC (that this AWSProfile has access to, in this region)
        int nonDefaultVPCIndex = -1;
        boolean bDefaultVPCFound = false;
        int ix = 0;
        for( LinkedHashMap<String,Object> vpc: vpcs ) {
            final boolean isDefaultVPC = (Boolean) vpc.get( org.ASUX.AWSSDK.AWSSDK.VPC_ISDEFAULT );
            bDefaultVPCFound = isDefaultVPC;
            if ( isDefaultVPC )
                nonDefaultVPCIndex = ix;
            ix ++;
        } // for
        if ( vpcs.size() <= 2 && bDefaultVPCFound && nonDefaultVPCIndex >= 0 ) {
            final LinkedHashMap<String,Object> nonDefaultVPC = vpcs.get( nonDefaultVPCIndex );
            final String s = (String) nonDefaultVPC.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
            if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ nonDefaultVPC );
            return s;
        }
        // if none found, fall-thru.. ..

        //-------------------
        // (5) There is only one VPC that is a DEFAULT-VPC
        if ( vpcs.size() <= 1 && bDefaultVPCFound ) {
            final LinkedHashMap<String,Object> defaultVPC = vpcs.get(0);
            final String s = (String) defaultVPC.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
            if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ defaultVPC );
            return s;
        }

        throw new Exception( "No VPCs found for _regionStr="+ _regionStr +" _VPCID="+ _VPCID +" =_MyOrgName"+ _MyOrgName +" _MyEnvironment="+ _MyEnvironment +" _MyDomainName="+ _MyDomainName );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================


    /**
     *  <p>Runs the command to generate CFN-Template YAML via: //${ORGASUXHOME}/asux.js yaml batch @${AWSCFNHOME}/bin/AWSCFN-${CFNContext}-Create.ASUX-batch.txt -i /dev/null -o ${CFNfile}</p>
     *  <p>The shell script to use that CFN-Template YAML:-  "aws cloudformation create-stack --stack-name ${MyVPCStackPrefix}-VPC  --region ${AWSRegion} --profile \${AWSprofile} --parameters ParameterKey=MyVPCStackPrefix,ParameterValue=${MyVPCStackPrefix} --template-body file://${CFNfile} " </p>
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @param _envParams a NotNull object (created by {@link BootCheckAndConfig#configure})
     *  @throws IOException if any errors creating output files for CFN-template YAML or for the script to run that CFN-YAML
     *  @throws Exception if any errors with inputs or while running batch-command to generate CFN templates
     */
    public void genCFNShellScript( final CmdLineArgs _cmdLA, final EnvironmentParameters _envParams ) throws IOException, Exception
    {   final String HDR = CLASSNAME + ": genVPCCFNShellScript(): ";

        final Properties globalProps = _envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );
        final YAMLTools yamltools = new YAMLTools( this.verbose, /* showStats */ false, this.cmdinvoker.dumperopt );

        //-------------------------------------
        // read the single YAML-configuration-file.. that's describing the entire-stack / fullstack
        Node output = null;
        final Node inputNode = yamltools.readYamlFile( "fullStackJob_Filename" );
        if ( this.verbose ) System.out.println( HDR +" file contents= '" + NodeTools.Node2YAMLString( inputNode ) + ".yaml'");

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

        // final String MyOrgName      = (existingVPCID == null) ? yamltools.readStringFromYAML( inputNode, "AWS,MyOrgName" )     : getVPCTag( existingVPCID, "MyOrgName", _cmdLA, _envParams );
        // final String MyEnvironment  = (existingVPCID == null) ? yamltools.readStringFromYAML( inputNode, "AWS,MyEnvironment" ) : getVPCTag( existingVPCID, "MyEnvironment", _cmdLA, _envParams );
        // final String MyDomainName   = (existingVPCID == null) ? yamltools.readStringFromYAML( inputNode, "AWS,MyDomainName" )  : getVPCTag( existingVPCID, "MyDomainName", _cmdLA, _envParams );
        // if (this.verbose) System.out.println( HDR + "MyOrgName=" + MyOrgName + " MyEnvironment=" + MyEnvironment + " AWSRegion=" + AWSRegion + " MyDomainName=" + MyDomainName +"." );

        final String VPCIDwMacros   = Macros.evalThoroughly( this.verbose, "${ASUX::VPCID}", _envParams.getAllPropsRef() ); // set by BootCheckAndConfig!  === _envParams.MyVPCStackPrefix + "-VPCID"
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
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
