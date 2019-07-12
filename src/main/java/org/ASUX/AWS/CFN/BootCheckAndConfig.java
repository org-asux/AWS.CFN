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
public final class BootCheckAndConfig {

    public static final String CLASSNAME = BootCheckAndConfig.class.getName();

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    public boolean verbose;
    public EnvironmentParameters envParams;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _allProps a (NotNull) reference provided by CmdInvoker().memoryAndContext.getAllPropsRef().. or other source
     */
    public BootCheckAndConfig( final boolean _verbose, final LinkedHashMap<String, Properties> _allProps  ) {
        this.verbose = _verbose;
        this.envParams = new EnvironmentParameters( _verbose, _allProps );
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    //  final Enums.GenEnum _cmdName, final String _jobSetName, final String _itemNumber
    //  *  @param _cmdName  a value of type {@link Enums.GenEnum} - it should come from {@link CmdLineArgs#getCmdName()}
    //  *  @param _jobSetName a NotNull value that describes the 'job' and should represent a __SUBFOLDER__ within the current-working folder.
    //  *  @param _itemNumber a NotNull value that describes the 'clone-ID' of the _jobSetName (if you repeatedly create CFN based on _jobSetName)
    //  * @param _jobTYPE a string representing the context (example: vpn, sg-ssh, ec2plain).. which is then used to load the appropriate Config-file for User's SPECs.
    /**
     *  Checks for critical environment variables and for critical cmdline parameters like AWSRegion. Then loads all Propertyfiles for the job.
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @throws Exception on any missing variables or parameters
     */
    public void check( final CmdLineArgs _cmdLA ) throws Exception
    {   final String HDR = CLASSNAME + ": check(_v," + _cmdLA.cmdName + ",_allProps): ";

        final Properties sysprops     = this.envParams.getAllPropsRef().get( org.ASUX.common.OSScriptFileScanner.SYSTEM_ENV );
        final String orgasuxhome      = sysprops.getProperty("ORGASUXHOME");
        final String awshome          = sysprops.getProperty("AWSHOME");
        final String awscfnhome       = sysprops.getProperty("AWSCFNHOME");

        // --------------------
        this.envParams.setCmd( _cmdLA.cmdName );
        final String cfnJobTYPEString = getCFNJobTypeAsString( _cmdLA.cmdName );
        if (this.verbose) System.out.println( HDR + "cfnJobTYPEString=" + cfnJobTYPEString );

        // --------------------
        if (orgasuxhome == null) {
            throw new Exception("ERROR! " + EnvironmentParameters.ORGASUXHOME + " is NOT defined.");
        }
        if (awshome == null) {
            throw new Exception("ERROR! " + EnvironmentParameters.AWSHOME + " is NOT defined.");
        }
        if (awscfnhome == null) {
            throw new Exception("ERROR! " + EnvironmentParameters.AWSCFNHOME + " is NOT defined.");
        }

        this.envParams.setHomeFolders( orgasuxhome, awshome, awscfnhome );
        if (this.verbose) System.out.println(HDR + "ORGASUXHOME=" + this.envParams.get_orgasuxhome() + " AWSHOME=" + this.envParams.get_awshome() + " AWSCFNHOME=" + this.envParams.get_awscfnhome()  + " jobSetName=" + _cmdLA.jobSetName );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        fileCheck( this.envParams.get_orgasuxhome(), "/asux.js", false /* _bMissingIsOk */ );
        // final File asux_js = new File( orgasuxhome +"/asux.js" );
        // if ( ! asux_js.exists() ) {
        //      final String es = "Please ensure the correct value of 'ORGASUXHOME' as thatfolder is missing the file/script 'asux.js', which is currently set to "+orgasuxhome;
        //      System.err.println( es );
        //      System.err.println( "This command will fail until you correct the current value of ORGASUXHOME );
        //      System.exit(5);
        //      throw new Exception( es );
        // }

        fileCheck( this.envParams.get_awscfnhome(), EnvironmentParameters.AWSREGIONSLOCATIONS, false /* _bMissingIsOk */ );
        // Don't need checks for 'AWSprofile' (a.k.a. ~/.aws/.profile) as this check is done within org.ASUX.AWS-SDK project's code, prior to interacting with AWS-SDK.

        // --------------------
        switch ( _cmdLA.cmdName ) {
            case FULLSTACK:
                            // Note: We have 'fileCheck()' invocations for these 3 'exclusive-or'  Tags-props-files in this.check() method.
                            // Objective: To determine that at least one of these exists.
                            // So, let's make sure that these below fileCheck() calls .. do _NOT_ throw a FileNotFoundException.
                            // For that the last parameter _MUST_ be true
                            final boolean exists1 = fileCheck( EnvironmentParameters.USERCONFIGHOME_CFN, EnvironmentParameters.TAGS_ALONE_MASTER, true /* _bMissingIsOk */ );
                            if (this.verbose) System.out.println(HDR + "exists1="+ exists1 +" for "+ EnvironmentParameters.USERCONFIGHOME_CFN +"/"+ EnvironmentParameters.TAGS_ALONE_MASTER );
                            final boolean exists2 = fileCheck( EnvironmentParameters.USERCONFIGHOME_CFN, EnvironmentParameters.TAGS_DEPT_MASTER, true /* _bMissingIsOk */ );
                            if (this.verbose) System.out.println(HDR + "exists2="+ exists1 +" for "+ EnvironmentParameters.USERCONFIGHOME_CFN +"/"+ EnvironmentParameters.TAGS_DEPT_MASTER );
                            final boolean exists3 = fileCheck( EnvironmentParameters.USERCONFIGHOME_CFN, EnvironmentParameters.TAGS_ENTERPRISE_MASTER, true /* _bMissingIsOk */ );
                            if (this.verbose) System.out.println(HDR + "exists3="+ exists1 +" for "+ EnvironmentParameters.USERCONFIGHOME_CFN +"/"+ EnvironmentParameters.TAGS_ENTERPRISE_MASTER );
                            if (  !  ( exists1 || exists2 || exists3 ) )
                                throw new Exception( "Missing defaults file named "+ EnvironmentParameters.TAGS_ALONE_MASTER +" or "+ EnvironmentParameters.TAGS_DEPT_MASTER +" or "+ EnvironmentParameters.TAGS_ENTERPRISE_MASTER +" within folder: "+ EnvironmentParameters.USERCONFIGHOME_CFN );
                            break;
            case SUBNET:
            case EC2PLAIN:
            case VPC:
            case SGSSH:
                            fileCheck( this.envParams.get_awscfnhome(), this.envParams.getJOB_DEFAULTS(), false /* _bMissingIsOk */ );
                            fileCheck( _cmdLA.jobSetName, this.envParams.getJOBSET_MASTER(), this.envParams.bInRecursionByFullStack );
                            // fileCheck( _cmdLA.jobSetName, "jobset-" + this.envParams.cfnJobTYPEString + ".properties" ); // we can't do this for all cfnJob-TYPEs
                            break;
            case VPNCLIENT:
            case SGEFS:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmdLA.cmdName;
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    //  final Enums.GenEnum _cmdName, final String _jobSetName, final String _itemNumber
    //  *  @param _cmdName  a value of type {@link Enums.GenEnum} - it should come from {@link CmdLineArgs#getCmdName()}
    //  *  @param _jobSetName a NotNull value that describes the 'job' and should represent a __SUBFOLDER__ within the current-working folder.
    //  *  @param _itemNumber a NotNull value that describes the 'clone-ID' of the _jobSetName (if you repeatedly create CFN based on _jobSetName)
    // * @param _jobTYPE a string representing the context (example: vpn, sg-ssh, ec2plain).. which is then used to load the appropriate Config-file for User's SPECs.
    /**
     *  Checks for critical environment variables and for critical cmdline parameters like AWSRegion. Then loads all Propertyfiles for the job.
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @throws Exception on any missing variables or parameters
     */
    public void configure( final CmdLineArgs _cmdLA ) throws Exception
    {   final String HDR = CLASSNAME + ": configure(_v," + _cmdLA.cmdName + ",_allProps): ";

        final Properties sysprops       = this.envParams.getAllPropsRef().get( org.ASUX.common.OSScriptFileScanner.SYSTEM_ENV );
        // this.envParams.orgasuxhome      = sysprops.getProperty("ORGASUXHOME");
        // this.envParams.awshome          = sysprops.getProperty("AWSHOME");
        // this.envParams.awscfnhome       = sysprops.getProperty("AWSCFNHOME");
        // this.envParams.cfnJobTYPEString = getCFNJobTypeAsString( _cmdLA.cmdName );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // Now check and obtain ALL the important Property Objects.
        if( this.envParams.getAllPropsRef().get( "Tags" ) == null ) {
            this.envParams.getAllPropsRef().put( "Tags", new Properties() );
        }
        final Properties globalProps = this.envParams.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        assertTrue( globalProps != null );
        final Properties Tags        = this.envParams.getAllPropsRef().get( "Tags" );
        assertTrue( Tags != null );

        final Properties AWSRegionLocations = org.ASUX.common.Utils.parseProperties( "@"+ this.envParams.get_awscfnhome()  +"/"+ EnvironmentParameters.AWSREGIONSLOCATIONS );
        this.envParams.getAllPropsRef().put( "AWSRegionLocations", AWSRegionLocations );
        // We need this specific '{AWSCFNHOME}/config/AWSRegionsLocations.properties' because we need to CONVERT a AWSRegion into an AWSLocation

        // --------------------
        // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ awscfnhome  +"/"+ AWSREGIONSLOCATIONS ) );
        switch ( _cmdLA.cmdName ) {
            case EC2PLAIN:
            case VPC:
            case SGSSH:
            case SUBNET:
                            globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ this.envParams.get_awscfnhome()  +"/"+ this.envParams.getJOB_DEFAULTS() ) );
                            // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _cmdLA.jobSetName +"/"+ EnvironmentParameters.JOBSET_MASTER ) );
                            // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _cmdLA.jobSetName +"/jobset-" + this.envParams.cfnJobTYPEString + ".properties" ) );
                            loadPropsIntoGlobal( "@"+ _cmdLA.jobSetName +"/"+ this.envParams.getJOBSET_MASTER(),                              globalProps, this.envParams.bInRecursionByFullStack );
                            loadPropsIntoGlobal( "@"+ _cmdLA.jobSetName +"/jobset-" + this.envParams.getCfnJobTYPEString() + ".properties",   globalProps, this.envParams.bInRecursionByFullStack );
                            break;
            case FULLSTACK: // get defaults from ~/.ASUX.org/.
                            // Note: We have 'fileCheck()' invocations for these 3 Tags-props-files in this.check() method.
                            // So, we know at least one of these exists.
                            // So, let's make sure that these below loadPropsIntoGlobal() calls throw a FileNotFoundException.
                            // For that the last parameter _MUST_ be true
// Loading these property files is TOO early.
// It gets OVERWRITTEN by the config/DEFAULTS/Tags_Defaults.properties
                            // loadPropsIntoGlobal( "@"+ EnvironmentParameters.USERCONFIGHOME_CFN +"/"+ EnvironmentParameters.TAGS_ALONE_MASTER,       Tags, true /* _bMissingIsOk */ );
                            // loadPropsIntoGlobal( "@"+ EnvironmentParameters.USERCONFIGHOME_CFN +"/"+ EnvironmentParameters.TAGS_DEPT_MASTER,        Tags, true /* _bMissingIsOk */ );
                            // loadPropsIntoGlobal( "@"+ EnvironmentParameters.USERCONFIGHOME_CFN +"/"+ EnvironmentParameters.TAGS_ENTERPRISE_MASTER,  Tags, true /* _bMissingIsOk */ );
                            break;
            case UNDEFINED:
            default:        final String es = " Unimplemented command: " + _cmdLA.cmdName;
                            System.err.println( HDR + es );
                            throw new Exception( es );
        }
        if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

        // --------------------
        globalProps.setProperty( "cfnJobTYPE", this.envParams.getCfnJobTYPEString() );
        globalProps.setProperty( "JobSetName", _cmdLA.jobSetName );
        globalProps.setProperty( "ItemNumber", _cmdLA.itemNumber );
        globalProps.setProperty( "PublicOrPrivate", _cmdLA.PublicOrPrivate );
        // final String InitialCapitalStr = Character.toUpperCase( _cmdLA.PublicOrPrivate.charAt(0) ) + _cmdLA.PublicOrPrivate.substring(1);
        // globalProps.setProperty( "PublicOrPrivateStr", InitialCapitalStr );
        if (this.verbose) System.out.println( HDR + "JobSetName=" + _cmdLA.jobSetName + " ItemNumber=" + _cmdLA.itemNumber + " PublicOrPrivate=" + _cmdLA.PublicOrPrivate );

        final String AWSRegion    = Macros.evalThoroughly( this.verbose, "${ASUX::AWSRegion}", this.envParams.getAllPropsRef() );
        final String AWSLocation  = Macros.evalThoroughly( this.verbose, "${ASUX::AWS-${ASUX::AWSRegion}}", this.envParams.getAllPropsRef() );
        this.envParams.setFundamentalGlobalProps( AWSRegion, AWSLocation );
        globalProps.setProperty( "AWSLocation", AWSLocation );

        final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-${ASUX::JobSetName}${ASUX::ItemNumber}", this.envParams.getAllPropsRef() );
        final String MyVPCStackPrefix = Macros.evalThoroughly( this.verbose, "${ASUX::MyOrgName}-${ASUX::MyEnvironment}-${ASUX::AWSLocation}", this.envParams.getAllPropsRef() );
        globalProps.setProperty( "MyStackNamePrefix", MyStackNamePrefix );
        globalProps.setProperty( EnvironmentParameters.MYVPCSTACKPREFIX, MyVPCStackPrefix );
        if (this.verbose) System.out.println( HDR + "MyStackNamePrefix=" + MyStackNamePrefix + " MyVPCStackPrefix=" + MyVPCStackPrefix );
        this.envParams.setFundamentalPrefixes( MyStackNamePrefix, MyVPCStackPrefix );

        final String VPCID = MyVPCStackPrefix + "-VPCID"; // Macros.evalThoroughly( this.verbose, "${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-VPCID", this.envParams.getAllPropsRef()  );
        final String DefaultAZ = MyVPCStackPrefix + "-AZ-ID"; // Macros.evalThoroughly( this.verbose, "${ASUX::"+EnvironmentParameters.MYVPCSTACKPREFIX+"}-AZ-ID", this.envParams.getAllPropsRef() );
        globalProps.setProperty( "VPCID", VPCID ) ;
        globalProps.setProperty( "DefaultAZ", DefaultAZ );
        if (this.verbose) System.out.println( HDR + "VPCID=" + VPCID + " DefaultAZ=" + DefaultAZ );

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
    public static String getCFNJobTypeAsString( final Enums.GenEnum _cmdName ) throws Exception
    {   final String HDR = CLASSNAME + ": getJobType("+ _cmdName +"): ";
        switch (_cmdName) {
            case VPC: // cfnJobTYPEString="vpc"; break;
            case SUBNET: // cfnJobTYPEString="subnets"; break;
            case SGSSH: // cfnJobTYPEString="sg-ssh"; break;
            case SGEFS: // cfnJobTYPEString="sg-efs"; break;
            case EC2PLAIN: // cfnJobTYPEString="ec2plain"; break;
            case VPNCLIENT: // cfnJobTYPEString="vpnclient"; break;
            case FULLSTACK: // cfnJobTYPEString="vpnclient"; break;
                String cfnJobTYPEString = _cmdName.toString();
                cfnJobTYPEString = cfnJobTYPEString.replaceAll("-gen$", "").toLowerCase();
                assertTrue( cfnJobTYPEString != null );
                return cfnJobTYPEString;
                // break;

            case UNDEFINED: // cfnJobTYPEString="vpc"; break;
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
     *  Checks whether a file under a specified folder-path exists or not.  If NOT, that is a problem, and appropriate error messages are put on STDERR and program stops running.
     *  @param _RootFldr NotNull - where to check for the existence of _filename
     *  @param _filename NotNull - can be a simple filename or a __RELATIVE__ path (under _RootFldr)
     *  @param _bMissingIsOk true means, this method does Not throw FileNotFound exception
     *  @return true if file exists, can be read and is Not an empty file
     *  @throws Exception if the file and/or folder do Not exist.
     */
    public boolean fileCheck( final String _RootFldr, final String _filename, final boolean _bMissingIsOk ) throws Exception
    {   final String HDR = CLASSNAME + ": fileCheck("+ _RootFldr +","+ _filename +","+ _bMissingIsOk +"): ";
        final File fileObj = new File ( _RootFldr +"/"+ _filename );
        // if ( ! fileObj.exists() || ! fileObj.canRead() || fileObj.length() <= 0 ) ..
        if ( fileObj.exists() && fileObj.canRead() && fileObj.length() > 0 ) {
            if ( this.verbose ) System.out.println( HDR + " File exists, can be read and is Not empty." );
            return true;
        } else {
            final String es = "ERROR! File "+ _filename +" is either missing/unreadable/empty. Failed to find: '"+ ( _RootFldr +"/"+ _filename ) +"'";
            if ( _bMissingIsOk ) {
                if ( this.verbose ) System.out.println( HDR + " File Not-exists/No-access/isEmpty.  But.. Won't barf and throw exception." );
                return false;
            } else {
                if ( this.verbose ) System.out.println( HDR + " File Not-exists/No-access/isEmpty.  Exiting program & throwing exception." );
                new Exception().printStackTrace( System.err );
                System.err.println( "\n\n"+ es );
                // System.err.println( "This command will not succeed until you correct this problem." );
                System.exit(5);
                throw new Exception( es );
            } // inner IF-ELSE
        } // outer IF-ELSE
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    private void loadPropsIntoGlobal( final String _filepath, final Properties globalProps, final boolean _bMissingIsOk ) throws java.io.FileNotFoundException, Exception
    {   final String HDR = CLASSNAME + ": loadPropsIntoGlobal("+ _filepath +",<globalProps>,"+ _bMissingIsOk +"): ";
        try {
            globalProps.putAll( org.ASUX.common.Utils.parseProperties( _filepath ) );
            if ( this.verbose ) System.out.println( HDR + " Properties-File successfully loaded." );
        } catch (java.io.FileNotFoundException fnfe) {
            if (  !   _bMissingIsOk ) {
                if ( this.verbose ) System.out.println( HDR + " File Not-exist.  But.. Won't barf and throw exception." );
                throw fnfe;
            }
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
