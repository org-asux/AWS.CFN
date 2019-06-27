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

import java.util.LinkedHashMap;
import java.util.Properties;

import java.io.File;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * 
 */
public final class BootstrapAndChecks {

    public static final String CLASSNAME = BootstrapAndChecks.class.getName();

    public static final String ORGASUXHOME = "ORGASUXHOME";
    public static final String AWSHOME = "AWSHOME";
    public static final String AWSCFNHOME = "AWSCFNHOME";

    public static final String PROPERTIES_FOR_CFN = "cfn.properties"; // one of the many Properties objects within this.allPropsRef (see go())
    public static final String PROPERTIES_FOR_JOB = "job.properties"; // one of the many Properties objects within this.allPropsRef (see go())

    public static final String AWSREGIONSLOCATIONS = "config/AWSRegionsLocations.properties";
    public static final String JOB_DEFAULTS = "/config/defaults/job-DEFAULTS.properties"; // under AWSCFNHOME
    public static final String JOBSET_MASTER = "jobset-Master.properties"; // under '.' folder

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    public String orgasuxhome = "UNDEFINED";
    public String awshome = "UNDEFINED";
    public String awscfnhome = "UNDEFINED";

    public boolean verbose;
    final LinkedHashMap<String, Properties> allPropsRef;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _allProps a (NotNull) reference provided by CmdInvoker().memoryAndContext.getAllPropsRef().. or other source
     */
    public BootstrapAndChecks( final boolean _verbose, final LinkedHashMap<String, Properties> _allProps  ) {
        this.verbose = _verbose;
        this.allPropsRef = _allProps;
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    // * @param _jobTYPE a string representing the context (example: vpn, sg-ssh, ec2plain).. which is then used to load the appropriate Config-file for User's SPECs.
    /**
     *  Checks for critical environment variables and for critical cmdline parameters like AWSRegion. Then loads all Propertyfiles for the job.
     *  @param _cmdName  a value of type {@link Enums.GenEnum} - it should come from {@link CmdLineArgs#getCmdName()}
     *  @param _jobSetName a NotNull value that describes the 'job' and should represent a __SUBFOLDER__ within the current-working folder.
     *  @param _itemNumber a NotNull value that describes the 'clone-ID' of the _jobSetName (if you repeatedly create CFN based on _jobSetName)
     *  @throws Exception on any missing variables or parameters
     */
    public void exec( final Enums.GenEnum _cmdName, final String _jobSetName, final String _itemNumber ) throws Exception
    {   final String HDR = CLASSNAME + ": go(_v," + _cmdName + ",_allProps): ";

        final Properties sysprops       = this.allPropsRef.get( org.ASUX.common.OSScriptFileScanner.SYSTEM_ENV );
        this.orgasuxhome  = sysprops.getProperty("ORGASUXHOME");
        this.awshome      = sysprops.getProperty("AWSHOME");
        this.awscfnhome   = sysprops.getProperty("AWSCFNHOME");

        if (orgasuxhome == null) {
            throw new Exception("ERROR! " + ORGASUXHOME + " is NOT defined.");
        }
        if (awshome == null) {
            throw new Exception("ERROR! " + AWSHOME + " is NOT defined.");
        }
        if (awscfnhome == null) {
            throw new Exception("ERROR! " + AWSCFNHOME + " is NOT defined.");
        }
        if (this.verbose) System.out.println(HDR + "ORGASUXHOME=" + orgasuxhome + " AWSHOME=" + awshome + " AWSCFNHOME=" + awscfnhome  + " jobSetName=" + _jobSetName);

        // --------------------
        String cfnJobTYPE = getCFNJobType( _cmdName );
        if (this.verbose) System.out.println( HDR + "cfnJobTYPE=" + cfnJobTYPE );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        fileCheck( orgasuxhome, "/asux.js" );
        // final File asux_js = new File( orgasuxhome +"/asux.js" );
        // if ( ! asux_js.exists() ) {
        //      final String es = "Please ensure the correct value of 'ORGASUXHOME' as thatfolder is missing the file/script 'asux.js', which is currently set to "+orgasuxhome;
        //      System.err.println( es );
        //      System.err.println( "This command will fail until you correct the current value of ORGASUXHOME );
        //      System.exit(5);
        //      throw new Exception( es );
        // }

        fileCheck( awscfnhome, AWSREGIONSLOCATIONS );
        // Don't need checks for 'AWSprofile' (a.k.a. ~/.aws/.profile) as this check is done within org.ASUX.AWS-SDK project's code, prior to interacting with AWS-SDK.

        // --------------------
        fileCheck( awscfnhome, JOB_DEFAULTS );
        fileCheck( _jobSetName, JOBSET_MASTER );
        fileCheck( _jobSetName, "jobset-" + cfnJobTYPE + ".properties" );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        final Properties globalProps = this.allPropsRef.get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        final Properties AWSRegionLocations = org.ASUX.common.Utils.parseProperties( "@"+ awscfnhome  +"/"+ AWSREGIONSLOCATIONS );
        this.allPropsRef.put( "AWSRegionLocations", AWSRegionLocations );
        // We need this specific '{AWSCFNHOME}/config/AWSRegionsLocations.properties' because we need to CONVERT a AWSRegion into an AWSLocation

        // --------------------
        // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ awscfnhome  +"/"+ AWSREGIONSLOCATIONS ) );
        globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ awscfnhome  +"/"+ JOB_DEFAULTS ) );
        globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _jobSetName +"/"+ JOBSET_MASTER ) );
        globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _jobSetName +"/jobset-" + cfnJobTYPE + ".properties" ) );
        if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

        // --------------------
        globalProps.setProperty( "cfnJobTYPE", cfnJobTYPE );
        globalProps.setProperty( "JobSetName", _jobSetName );
        globalProps.setProperty( "ItemNumber", _itemNumber );
        if (this.verbose) System.out.println( HDR + "JobSetName=" + _jobSetName + " ItemNumber=" + _itemNumber );

        final String AWSLocation = Macros.evalThoroughly( this.verbose, "AWS-${ASUX::AWSRegion}", this.allPropsRef );
        final String MyVPCStackPrefix = Macros.evalThoroughly( this.verbose, "${ASUX::MyOrgName}-${ASUX::MyEnvironment}-${ASUX::AWSLocation}", this.allPropsRef );
        globalProps.setProperty( "AWSLocation", AWSLocation );
        globalProps.setProperty( "MyVPCStackPrefix", MyVPCStackPrefix );

        final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::MyVPCStackPrefix}--${ASUX::JobSetName}${ASUX::ItemNumber}", this.allPropsRef );
        globalProps.setProperty( "MyStackNamePrefix", MyStackNamePrefix );
        if (this.verbose) System.out.println( HDR + "MyStackNamePrefix=" + MyStackNamePrefix );

        final String VPCID = MyVPCStackPrefix + "-VPCID"; // Macros.evalThoroughly( this.verbose, "${ASUX::MyVPCStackPrefix}-VPCID", this.allPropsRef  );
        final String DefaultAZ = MyVPCStackPrefix + "-AZ-ID"; // Macros.evalThoroughly( this.verbose, "${ASUX::MyVPCStackPrefix}-AZ-ID", this.allPropsRef );
        globalProps.setProperty( "VPCID", VPCID ) ;
        globalProps.setProperty( "DefaultAZ", DefaultAZ );
        if (this.verbose) System.out.println( HDR + "VPCID=" + VPCID + " DefaultAZ=" + DefaultAZ );

        final String DefaultPublicSubnet1 = MyStackNamePrefix + "-Subnet-1-ID";// Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-Subnet-1-ID", this.allPropsRef  );
        final String MySSHSecurityGroup = MyStackNamePrefix + "-SG-SSH"; // Macros.evalThoroughly( this.verbose, "${ASUX::MyStackNamePrefix}-SG-SSH", this.allPropsRef  );
        // final String MyIamInstanceProfiles = Macros.evalThoroughly( this.verbose, "${ASUX::?????????????????}-"+HDR, this.allPropsRef );
        final String MySSHKeyName = Macros.evalThoroughly( this.verbose, "${ASUX::AWSLocation}-${ASUX::MyOrgName}-${ASUX::MyEnvironment}-LinuxSSH.pem", this.allPropsRef );
        globalProps.setProperty( "DefaultPublicSubnet1", DefaultPublicSubnet1 );
        globalProps.setProperty( "MySSHSecurityGroup", MySSHSecurityGroup );
        globalProps.setProperty( "MySSHKeyName", MySSHKeyName );
        if (this.verbose) System.out.println( HDR + "DefaultPublicSubnet1=" + DefaultPublicSubnet1 + " MySSHSecurityGroup=" + MySSHSecurityGroup + " MySSHKeyName=" + MySSHKeyName );

        if (this.verbose) System.out.println( HDR + "globalProps: Total # of entries = " + globalProps.size() + "." );

    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  A simple tool to map the cmd - into a String variable, that is used to load the cmd-specific YAML-BATCH scripts.
     *  @param _cmdName  a value of type {@link Enums.GenEnum} - it should come from {@link CmdLineArgs#getCmdName()}
     *  @return a string that is always NOT NULL
     *  @throws Exception on any invalid input or for Incomplete-code scenarios
     */
    public String getCFNJobType( final Enums.GenEnum _cmdName ) throws Exception
    {   final String HDR = CLASSNAME + ": getJobType("+ _cmdName +"): ";
        switch (_cmdName) {
            case VPC: // cfnJobTYPE="vpc"; break;
            case SUBNET: // cfnJobTYPE="subnets"; break;
            case SGSSH: // cfnJobTYPE="sg-ssh"; break;
            case SGEFS: // cfnJobTYPE="sg-efs"; break;
            case EC2PLAIN: // cfnJobTYPE="ec2plain"; break;
            case VPNCLIENT: // cfnJobTYPE="vpnclient"; break;
            case UNDEFINED: // cfnJobTYPE="vpc"; break;
                String cfnJobTYPE = _cmdName.toString();
                cfnJobTYPE = cfnJobTYPE.replaceAll("-gen$", "").toLowerCase();
                assertTrue( cfnJobTYPE != null );
                return cfnJobTYPE;
                // break;
            default:
                final String es = HDR + "Internal Error: INCOMPLETE CODE.  Switch(_cmdName) for _cmdName=" + _cmdName.toString();
                System.err.println(es);
                throw new Exception(es);
        } // switch
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * Checks whether a file under a specified folder-path exists or not.  If NOT, that is a problem, and appropriate error messages are put on STDERR and program stops running.
     * @param _RootFldr NotNull - where to check for the existence of _filename
     * @param _filename NotNull - can be a simple filename or a __RELATIVE__ path (under _RootFldr)
     * @throws Exception if the file and/or folder do Not exist.
     */
    private static void fileCheck( final String _RootFldr, final String _filename ) throws Exception {
        final File fileObj = new File ( _RootFldr +"/"+ _filename );
        if ( ! fileObj.exists() || ! fileObj.canRead() ) {
            final String es = "ERROR! File "+ _filename +" missing/unreadable, under the folder-tree @ "+ _RootFldr;
            System.err.println( es );
            System.err.println( "This command will fail until you correct this problem." );
            System.exit(5);
            throw new Exception( es );
        }
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
