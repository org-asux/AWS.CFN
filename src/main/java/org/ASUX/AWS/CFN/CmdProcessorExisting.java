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
import org.ASUX.common.Triple;
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

import java.util.regex.*;

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
    protected CmdInvoker cmdinvoker;
    // protected CmdProcessor cmdProcessor;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // public CmdProcessorExisting( final CmdProcessor _cmdProcessor, final CmdInvoker _cmdinvoker )
    public CmdProcessorExisting( final CmdInvoker _cmdinvoker ) {
        this.verbose = _cmdinvoker.verbose;
        this.cmdinvoker = _cmdinvoker;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>if '_VPCID' === 'existing', then .. Code will search for VPCs in the following sequence :-</p>
     *  <ul>
     *  <li>(1) Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above</li>
     *  <li>(2) Tag/Name: VPC-by-ASUX.org-${ASUX::JobSetName}</li>
     *  <li>(3) Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.</li>
     *  <li>(4) There is only one NON-Default-VPC (that this AWSProfile has access to, in this region)</li>
     *  <li>(5) There is only one VPC that is a DEFAULT-VPC</li>
     *  </ul>
     *  @param _regionStr NotNull string for the AWSRegion (Not the AWSLocation)
     *  @param _VPCID either 'existing' or any ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#REGEXP_AWSID_SUFFIX}.  This method checks against that rule.
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
    {   final String HDR = CLASSNAME + ": getVPCID("+ _regionStr +","+ _VPCID +","+ _MyOrgName +","+ _MyEnvironment +","+ _MyDomainName +","+ _offline +": ";
        if ( _VPCID == null ) {
            if ( this.verbose ) System.out.println( HDR +" VPC ID entered by user is '"+ _VPCID +"'" );
            return null;
        }
        
        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _offline );

        //-------------------
        // (0) Whether user provided an ACTUAL VPC-ID
        if (  awssdk.matchesAWSIDPattern( _VPCID, "vpc" ) ) {
            if ( this.verbose ) System.out.println( HDR +"Assuming that "+ _VPCID +" is a valid AWS-ID. So, no need to lookup anything within the region" );
            return _VPCID;
        } else if (  !  "existing".equals(_VPCID) ) {
            throw new Exception( "VPC ID "+ _VPCID + " is invalid.  Its neither ==='existing' nor it is like vpc-"+ org.ASUX.AWSSDK.AWSSDK.REGEXP_AWSID_SUFFIX );
        }
        // if we are here, the _VPCID === 'existing'
        // So.. let's go find a suitable VPC for the lazy user.
        
        if ( "existing".equals(_VPCID) && _offline )
            throw new Exception( "User-Error: With --offline cmd-line flag, _CANNOT_ provide 'existing' as the VPC ID, within the Job-definition YAML file." );

        //-------------------
        final ArrayList< LinkedHashMap<String,Object> > vpcs = awssdk.getVPCs( _regionStr, false /* _onlyNonDefaultVPC */);

        // (1) Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above
        if ( _MyOrgName != null && _MyDomainName != null ) {
            for( LinkedHashMap<String,Object> vpc: vpcs ) {
                if ( this.verbose ) System.out.println( HDR +"checking out VPC: "+ vpc );
                if ( _MyOrgName.equals(vpc.get(EnvironmentParameters.MYORGNAME)) &&  _MyDomainName.equals(vpc.get(EnvironmentParameters.MYDOMAINNAME)) ) {
                    final String s = (String) vpc.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
                    if ( this.verbose ) System.out.println( HDR +"Found a VPC that matched the OrgName and DomainName provided: '"+ s +"' "+ vpc );
                    System.out.print( "Using 'existing' VPC '"+ s +"'\t" );
                    return s;
                }
            } // for
        } // else fall-thru.. ..
        //-------------------
        // (2) Tag/Name: VPC-by-ASUX.org-${ASUX::JobSetName}
        for( LinkedHashMap<String,Object> vpc: vpcs ) {
            final String tag_name = (String) vpc.get("Name");
            if ( tag_name != null && tag_name.startsWith("VPC-by-") ) {
                final String s = (String) vpc.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
                if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ vpc );
                System.out.print( "Using 'existing' VPC '"+ s +"'\t" );
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
        if ( this.verbose ) System.out.println( HDR +"nonDefaultVPCIndex="+ nonDefaultVPCIndex +" bDefaultVPCFound="+ bDefaultVPCFound );

        if ( vpcs.size() <= 2 && bDefaultVPCFound && nonDefaultVPCIndex >= 0 ) { // One Default-VPC and One Non-Default-VPC
            final LinkedHashMap<String,Object> nonDefaultVPC = vpcs.get( nonDefaultVPCIndex );
            final String s = (String) nonDefaultVPC.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
            if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ nonDefaultVPC );
            System.out.print( "Using 'existing' VPC '"+ s +"'\t" );
            return s;
        }
        if ( vpcs.size() <= 1 && nonDefaultVPCIndex >= 0 ) { // The only VPC is a NON-Default VPC.
            final LinkedHashMap<String,Object> nonDefaultVPC = vpcs.get( nonDefaultVPCIndex );
            final String s = (String) nonDefaultVPC.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
            if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ nonDefaultVPC );
            System.out.print( "Using 'existing' VPC '"+ s +"'\t" );
            return s;
        }
        // if none found, fall-thru.. ..

        //-------------------
        // (5) There is only one VPC that is a DEFAULT-VPC
        if ( vpcs.size() <= 1 && bDefaultVPCFound ) {
            final LinkedHashMap<String,Object> defaultVPC = vpcs.get(0);
            final String s = (String) defaultVPC.get( org.ASUX.AWSSDK.AWSSDK.VPC_ID );
            if ( this.verbose ) System.out.println( HDR +"Found a VPC whose Tag_Name indicates ASUX.org tools were used to create it: '"+ s +"' "+ defaultVPC );
            System.out.print( "Using 'existing' VPC '"+ s +"'\t" );
            return s;
        }

        throw new Exception( "No VPCs found for _regionStr="+ _regionStr +" _VPCID="+ _VPCID +" =_MyOrgName"+ _MyOrgName +" _MyEnvironment="+ _MyEnvironment +" _MyDomainName="+ _MyDomainName );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>if '_subnetID' === 'existing', then .. Code will search for Subnets ONLY if ALL the following are met :-</p>
     *  <ul>
     *  <li>MUST Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above<br>
     *      + Tag/PublicOrPrivate must exist, and must have value 'Public' or 'Private'</li>
     *  <li>Tag/Name: Subnet-(Public|Private)1-org-ASUX-${ASUX::MyEnvironment}-${ASUX::AWSLocation}-${ASUX::JobSetName}<br>
     *      + Tag/PublicOrPrivate must exist, and must have value 'Public' or 'Private'</li>
     *  <li>Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.<br>
     *      + Tag/PublicOrPrivate must exist, and must have value 'Public' or 'Private'</li>
     *  <li>There is only one (Public|Private) subnet (that this AWSProfile has access to, in this region)</li>
     *  </ul>
     *  @param _regionStr NotNull string for the AWSRegion (Not the AWSLocation)
     *  @param _VPCID either 'existing' or any ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#REGEXP_AWSID_SUFFIX}.  This method checks against that rule.
     *  @param _subnetID either 'existing' or any ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#REGEXP_AWSID_SUFFIX}.  This method checks against that rule.
     *  @param _PublicOrPrivate whether a public or private subnet EC2 instance (String value is case-sensitive.  Exact allowed values are: 'Public' 'Private')
     *  @param _MyOrgName NotNull string like 'example.org' or 'MySubsidiary'
     *  @param _MyEnvironment NotNull string lile Production, UAT, development, Dev, .. ..
     *  @param _MyDomainName a NotNull string like 'subdomain.example.com'
     *  @param _offline 'true' === this entire class and all it's methods will use "cached" output (a.k.a. files under {ASUXCFNHOME}/configu/inputs folder), instead of invoking AWS SDK calls.
     *  @return a NotNull String (guaranteed), else exception is thrown
     *  @throws Exception is _subnetID argument is neither  vpc-[0-9a-f]+   or   === 'existing' .. .. or, if No VPC could be found
     */
    public String getSubnetID( final String _regionStr, final String _VPCID, final String _subnetID, final String _PublicOrPrivate,
                            final String _MyOrgName, final String _MyEnvironment, final String _MyDomainName,
                            final boolean _offline )
                            throws Exception
    {   final String HDR = CLASSNAME + ": getSubnetID("+ _regionStr +","+ _VPCID +","+ _subnetID +","+ _PublicOrPrivate +","+ _MyOrgName +","+ _MyEnvironment +","+ _MyDomainName +","+ _offline +": ";
        if ( _VPCID == null ) {
            if ( this.verbose ) System.out.println( HDR +" _VPCID provided to this method is '"+ _VPCID +"'" );
            return null;
        }
        // assertTrue( _subnetID != null );
        assertTrue( _PublicOrPrivate != null );
        if ( _subnetID == null ) {
            if ( this.verbose ) System.out.println( HDR +" Subnet ID entered by user is '"+ _subnetID +"'" );
            return null;
        }

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _offline );

        //-------------------
        // (0) Whether user provided an ACTUAL VPC-ID
        if (  awssdk.matchesAWSIDPattern( _subnetID, "subnet" ) ) {
            if ( this.verbose ) System.out.println( HDR +"Assuming that "+ _subnetID +" is a valid Subnet-ID. So, no need to lookup anything within the region" );
            return _subnetID;
        } else if (  !  "existing".equals(_subnetID) ) {
            throw new Exception( "Subnet-ID "+ _subnetID + " is invalid.  Its neither ==='existing' nor it is like vpc-"+ org.ASUX.AWSSDK.AWSSDK.REGEXP_AWSID_SUFFIX );
        }
        // if we are here, the _subnetID === 'existing'
        // So.. let's go find a suitable VPC for the lazy user.

        if ( "existing".equals(_subnetID) && _offline )
            throw new Exception( "User-Error: With --offline cmd-line flag, _CANNOT_ provide 'existing' as the Subnet ID, within the Job-definition YAML file." );

        //-------------------
        final ArrayList< LinkedHashMap<String,Object> > subnets = awssdk.getSubnets( _regionStr, _VPCID, _PublicOrPrivate );

        // (1) Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above
        if ( _MyOrgName != null && _MyDomainName != null ) {
            for( LinkedHashMap<String,Object> subnet: subnets ) {
                if ( this.verbose ) System.out.println( HDR +"Checking.. Subnet: "+ subnet );
                if ( _MyOrgName.equals(subnet.get(EnvironmentParameters.MYORGNAME)) &&  _MyDomainName.equals(subnet.get(EnvironmentParameters.MYDOMAINNAME)) ) {
                    final String id = (String) subnet.get( org.ASUX.AWSSDK.AWSSDK.SUBNET_ID );
                    if ( this.verbose ) System.out.println( HDR +"Found a Subnet that matched the OrgName and DomainName provided.  ID is '"+ id +"' "+ subnet );
                    if ( _PublicOrPrivate.equals( (String) subnet.get( org.ASUX.AWSSDK.AWSSDK.KV_PUBLICorPRIVATE ) ) ) {
                        if ( this.verbose ) System.out.println( HDR +"Found a Subnet! For PublicOrPrivate="+ _PublicOrPrivate +". ID is '"+ id +"'." );
                        System.out.print( "Using 'existing' Subnet '"+ id +"'\t" );
                        return id;
                    }
                }
            } // for
        } // else if NONE found, fall-thru.. ..
        //-------------------
        // (2) Tag/Name: VPC-by-ASUX.org-${ASUX::JobSetName}
        for( LinkedHashMap<String,Object> subnet: subnets ) {
            final String tag_name = (String) subnet.get("Name");
            if ( this.verbose ) System.out.println( HDR +"Checking.. Subnet: "+ tag_name +"  "+ subnet );
            try {
                final Pattern pattern = Pattern.compile( "^Subnet-(Public|Private)[0-9]+-org-ASUX-"+ _MyEnvironment +"-.+" );
                final Matcher matcher = pattern.matcher( tag_name );
                if ( matcher.find() ) {
                    if ( this.verbose ) System.out.println( HDR +"I found the text "+ matcher.group() +" starting at index "+  matcher.start() +" and ending at index "+ matcher.end() );
                    final String id = (String) subnet.get( org.ASUX.AWSSDK.AWSSDK.SUBNET_ID );
                    if ( this.verbose ) System.out.println( HDR +"Found a Subnet that has Tags that ASUX.org tools created.  ID is '"+ id +"'   "+ subnet );
                    if ( _PublicOrPrivate.equals( (String) subnet.get( org.ASUX.AWSSDK.AWSSDK.KV_PUBLICorPRIVATE ) ) ) {
                        if ( this.verbose ) System.out.println( HDR +"Found a Subnet! For PublicOrPrivate="+ _PublicOrPrivate +". ID is '"+ id +"'.");
                        System.out.print( "Using 'existing' Subnet '"+ id +"'\t" );
                        return id;
                    }
                }
            }catch(PatternSyntaxException e){
                e.printStackTrace( System.err );
                throw new Exception("Serious internal error - PatternSyntaxException within "+ HDR );
            }
        } // for
        // else if NONE found, fall-thru.. ..
        //-------------------
        // (3) Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.

            // NOTE:  (3) is NOT IMPLEMENTED FOR NOW

        //-------------------
        // (4) There is only one NON-Default-VPC (that this AWSProfile has access to, in this region)
        if ( subnets.size() == 1 ) {
            final LinkedHashMap<String,Object> onlySubnet = subnets.get( 0 );
            final String id = (String) onlySubnet.get( org.ASUX.AWSSDK.AWSSDK.SUBNET_ID );
            if ( this.verbose ) System.out.println( HDR +"Found the ONLY existing Subnet (unknown whether Public or Private): '"+ id +"' "+ onlySubnet );
            System.out.print( "Using 'existing' Subnet '"+ id +"'\t" );
            return id;
        }

        throw new Exception( "No Subnets found for _regionStr="+ _regionStr +" _VPCID="+ _VPCID +" _subnetID="+ _subnetID +" _PublicOrPrivate="+ _PublicOrPrivate +" =_MyOrgName"+ _MyOrgName +" _MyEnvironment="+ _MyEnvironment +" _MyDomainName="+ _MyDomainName +" _offline="+ _offline );
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     *  <p>if '_subnetID' === 'existing', then .. Code will search for Subnets ONLY if ALL the following are met :-</p>
     *  <ul>
     *  <li>Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above<br>
     *      + matches the port-type specified</li>
     *  <li>Tag/Name: SG-SSH-${ASUX::MyOrgName}-${ASUX::MyEnvironment}-${ASUX::AWSRegion}<br>
     *      + matches the port-type specified</li>
     *  <li>Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.<br>
     *      + matches the port-type specified</li>
     *  <li>The very 1st existing SG<br>
     *      + matches the port-type specified</li>
     *  </ul>
     *  @param _regionStr NotNull string for the AWSRegion (Not the AWSLocation)
     *  @param _VPCID either 'existing' or any ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#REGEXP_AWSID_SUFFIX}.  This method checks against that rule.
     *  @param _SGID either 'existing' or any ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#REGEXP_AWSID_SUFFIX}.  This method checks against that rule.
     *  @param _portOfInterest whether "ssh", "rdp", .. (String value is case-sensitive)
     *  @param _MyOrgName NotNull string like 'example.org' or 'MySubsidiary'
     *  @param _MyEnvironment NotNull string lile Production, UAT, development, Dev, .. ..
     *  @param _MyDomainName a NotNull string like 'subdomain.example.com'
     *  @param _offline 'true' === this entire class and all it's methods will use "cached" output (a.k.a. files under {ASUXCFNHOME}/configu/inputs folder), instead of invoking AWS SDK calls.
     *  @return a NotNull String (guaranteed), else exception is thrown
     *  @throws Exception is _SGID argument is neither  vpc-[0-9a-f]+   or   === 'existing' .. .. or, if No VPC could be found
     */
    public String getSGID( final String _regionStr, final String _VPCID,
                            final String _SGID, final String _portOfInterest,
                            final String _MyOrgName, final String _MyEnvironment, final String _MyDomainName,
                            final boolean _offline )
                            throws Exception
    {   final String HDR = CLASSNAME + ": getSGID("+ _regionStr +","+ _VPCID +","+ _SGID +","+ _portOfInterest +","+ _MyOrgName +","+ _MyEnvironment +","+ _MyDomainName +","+ _offline +": ";
        assertTrue( _VPCID != null );
        assertTrue( _portOfInterest != null );
        // assertTrue( _SGID != null );
        if ( _SGID == null ) {
            if ( this.verbose ) System.out.println( HDR +" SecurityGroup ID entered by user is '"+ _SGID +"'" );
            return null;
        }

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _offline );

        //-------------------
        // (0) Whether user provided an ACTUAL VPC-ID
        if (  _SGID != null  &&  awssdk.matchesAWSIDPattern( _SGID, "subnet" ) ) {
            if ( this.verbose ) System.out.println( HDR +"Assuming that "+ _SGID +" is a valid SG-ID. So, no need to lookup anything within the region" );
            return _SGID;
        } else if (  !  "existing".equals(_SGID) ) {
            throw new Exception( "SG-ID "+ _SGID + " is invalid.  Its neither ==='existing' nor it is like vpc-"+ org.ASUX.AWSSDK.AWSSDK.REGEXP_AWSID_SUFFIX );
        }
        // if we are here, the _SGID === 'existing'
        // So.. let's go find a suitable VPC for the lazy user.

        if ( "existing".equals(_SGID) && _offline )
            throw new Exception( "User-Error: With --offline cmd-line flag, _CANNOT_ provide 'existing' as the Security-Group ID, within the Job-definition YAML file." );

        //-------------------
        final ArrayList< LinkedHashMap<String,Object> > SGs = awssdk.getSGs( _regionStr, _VPCID, _portOfInterest );

        // (1) Match Tag/Name for MyOrgName/MyDomainName .. _IF_ provided above
        if ( _MyOrgName != null && _MyDomainName != null ) {
            for( LinkedHashMap<String,Object> sg: SGs ) {
                if ( this.verbose ) System.out.println( HDR +"Checking.. security-group: "+ sg );
                if ( _MyOrgName.equals(sg.get(EnvironmentParameters.MYORGNAME)) &&  _MyDomainName.equals(sg.get(EnvironmentParameters.MYDOMAINNAME)) ) {
                    final String id = (String) sg.get( org.ASUX.AWSSDK.AWSSDK.SG_ID );
                    if ( this.verbose ) System.out.println( HDR +"Found a SG that matched the OrgName and DomainName provided.  ID is '"+ id +"' "+ sg );
                    if ( this.verbose ) System.out.println( HDR +"Found a Security-Group - Specifically for Port of Interest ="+ _portOfInterest +". ID is '"+ id +"'." );
                    System.out.print( "Using 'existing' Security-Group '"+ id +"'\t" );
                    return id;
                }
            } // for
        } // else if NONE found, fall-thru.. ..
        //-------------------
        // (2) Tag/Name: VPC-by-ASUX.org-${ASUX::JobSetName}
        for( LinkedHashMap<String,Object> sg: SGs ) {
            final String tag_name = (String) sg.get("Name");
            if ( this.verbose ) System.out.println( HDR +"Checking.. SecurityGroup: "+ tag_name +"  "+ sg );
            try {
                final Pattern pattern = Pattern.compile( "^SG-SSH-"+ _MyOrgName +"-"+ _MyEnvironment +"-"+ _regionStr ); // SG-SSH-${ASUX::MyOrgName}-${ASUX::MyEnvironment}-${ASUX::AWSRegion}
                final Matcher matcher = pattern.matcher( tag_name );
                if ( matcher.find() ) {
                    if ( this.verbose ) System.out.println( HDR +"I found the text "+ matcher.group() +" starting at index "+  matcher.start() +" and ending at index "+ matcher.end() );
                    final String id = (String) sg.get( org.ASUX.AWSSDK.AWSSDK.SG_ID );
                    if ( this.verbose ) System.out.println( HDR +"Found a SG that has Tags that ASUX.org tools created.  ID is '"+ id +"'   "+ sg );
                    if ( this.verbose ) System.out.println( HDR +"Found a Security-Group! For Port of Interest ="+ _portOfInterest +". ID is '"+ id +"'.");
                    System.out.print( "Using 'existing' Security-Group '"+ id +"'\t" );
                    return id;
                }
            }catch(PatternSyntaxException e){
                e.printStackTrace( System.err );
                throw new Exception("Serious internal error - PatternSyntaxException within "+ HDR );
            }
        } // for
        // else if NONE found, fall-thru.. ..
        //-------------------
        // (3) Tag/CreatedBy: matches that of the AWSProfile used for SDK calls.

            // NOTE:  (3) is NOT IMPLEMENTED FOR NOW

        //-------------------
        // (4) There is only one NON-Default-VPC (that this AWSProfile has access to, in this region)
        if ( SGs.size() >= 1 ) { // !!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!! Unlike getSubnetID() and getVPCID(), this _IF_ condition uses >=
            final LinkedHashMap<String,Object> anySG = SGs.get( 0 );
            final String id = (String) anySG.get( org.ASUX.AWSSDK.AWSSDK.SG_ID );
            if ( this.verbose ) System.out.println( HDR +"Found the 1st existing Security-Group: '"+ id +"' "+ anySG );
            System.out.print( "Using 'existing' Security-Group '"+ id +"'\t" );
            return id;
        }

        throw new Exception( "No Security-Group found for _regionStr="+ _regionStr +" _VPCID="+ _VPCID +" _SGID="+ _SGID +" _portOfInterest="+ _portOfInterest +" =_MyOrgName"+ _MyOrgName +" _MyEnvironment="+ _MyEnvironment +" _MyDomainName="+ _MyDomainName +" _offline="+ _offline );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Lookup the InternetGateways in the region, determine which are Not already associated with a VPC (or, the IGW is already associated with the _existingVPCID).</p>
     *  @param _regionStr NotNull string for the AWSRegion (Not the AWSLocation)
     *  @param _existingVPCID a NotNull VPC ID in AWS (whether VPC, subnet, SG, EC2...) === prefix('vpc-', 'subnet-', ..) + a hexadecimal suffix {@link org.ASUX.AWSSDK.AWSSDK#REGEXP_AWSID_SUFFIX}.  This method checks against that rule.
     *  @param _offline 'true' === this entire class and all it's methods will use "cached" output (a.k.a. files under {ASUXCFNHOME}/configu/inputs folder), instead of invoking AWS SDK calls.
     *  @return a Null-able String, unless exception is thrown
     *  @throws Exception is _SGID argument is neither  vpc-[0-9a-f]+   or   === 'existing' .. .. or, if No VPC could be found
     */
    public String getIGWID( final String _regionStr, final String _existingVPCID, final boolean _offline ) throws Exception
    {   final String HDR = CLASSNAME + ": getIGWID("+ _regionStr +","+ _existingVPCID  +","+ _offline +"): ";
        if ( _offline )
            return null; // assume No IGW exists...

        final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _offline );

        String IGWID = null;   // default, unless - inside the IF-below - we detect an existing IGW that we can re-use.
        final ArrayList< Tuple<String,String> > existingIGWIDs  = awssdk.getIGWs( _regionStr, false /* _bUnassociated */ );
        final ArrayList< Tuple<String,String> > existingUnassociatedIGWIDs  = awssdk.getIGWs( _regionStr, true /* _bUnassociated */ );

        if ( _existingVPCID == null ) {
            if ( existingUnassociatedIGWIDs.size() > 0 ) {
                IGWID = existingUnassociatedIGWIDs.get(0).key;
                if (this.verbose) System.out.println( HDR + "Will associate an existing IGW ith ID# " + IGWID +" for this _NEW_ VPC." );
            }
        } else {
            IGWID = awssdk.getIGWForVPC( _regionStr, _existingVPCID );
            if ( IGWID != null ) {
                if (this.verbose) System.out.println( HDR + "All good! IGW ith ID# " + IGWID +" is _ALREADY_  _ASSOCIATED_ with this VPC "+ _existingVPCID +"." );
            } else {
                // looks like this _EXISTING_ VPC does _NOT_ an IGW attached to it!
                if ( existingUnassociatedIGWIDs.size() > 0 ) {
                    IGWID = existingUnassociatedIGWIDs.get(0).key; // taking the 1st available InternetGateway, to associate with this _EXISTING_ VPC is NOT a bad idea.
                    System.err.println("!!!!!! ATTENTION !!!!!!! Manually __ATTACH__ the InternetGateway with ID# " + IGWID +" to existing VPC "+ _existingVPCID +".!!!!!" );
                } else {
                    // System.err.println("!!!!!! ATTENTION !!!!!!! Manually create a __NEW__ InternetGateway & associate it __MANUALLY__ with this existing VPC "+ _existingVPCID +"!!!!!" );
                    if (this.verbose) System.out.println( HDR + "Will create a _NEW_ IGW in the CFN-template and auto-associate with the _NEW_ VPC." );
                }
            }
        }
        // So.. after the above code, if IGW ID is null, then .. we'll create a new IGW (within the @${ASUX::AWSCFNHOME}/bin/AWSCFN-fullstack-vpc-create.txt).
        return IGWID;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    // public Triple<String,String,String> getMustHaveValues( final YAMLTools yamltools, final boolean _offline )
    // {   final String HDR = CLASSNAME + ": getMustHaveValues(): ";
    //     final org.ASUX.AWSSDK.AWSSDK awssdk = org.ASUX.AWSSDK.AWSSDK.AWSCmdline( this.verbose, _cmdLA.isOffline() );
    // }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
