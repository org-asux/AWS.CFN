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

import org.ASUX.common.Tuple;
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
    public Environment myEnv;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _env a NotNull instance
     */
    public BootCheckAndConfig( final boolean _verbose, Environment _env  ) {
        this.verbose = _verbose;
        this.myEnv = _env;
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    // private Tuple<String,String>   capture User Input( final String AWSRegion, final String AWSLocation ) throws Exception
    // {   final String HDR = CLASSNAME + ": capture User Input("+ AWSRegion +","+ AWSLocation + "): "; 
    // }

    private Tuple<String,String>  evalAWSRegionLocation( final Enums.GenEnum _cmd ) throws Exception
    {   final String HDR = CLASSNAME + ": evalAWSRegionLocation(): ";

        // --------------------
        final String AWSRegion    = Macros.evalThoroughly( this.verbose, "${ASUX::AWSRegion}", this.myEnv.getAllPropsRef() );
        final String AWSLocation  = Macros.evalThoroughly( this.verbose, "${ASUX::AWS-${ASUX::AWSRegion}}", this.myEnv.getAllPropsRef() );

        if ( this.verbose ) System.out.println( HDR +"AWSRegion="+ AWSRegion +" AWSLocation="+ AWSLocation + "." );

        // --------------------
        if ( this.myEnv.enhancedUserInput == null || this.myEnv.enhancedUserInput.getAWSLocation().contains("ASUX::") ) {
            // this.myEnv.enhancedUserInput can be != null, if configure() is invoked repeatedly for '--fullstack-gen' (CmdProcessorFullStack.java)
            final UserInput userInput = new UserInput( this.verbose, AWSRegion, AWSLocation );
            this.myEnv.enhancedUserInput = new UserInputEnhanced( userInput );
        }
        final String cfnJobTYPEString = UserInputEnhanced.getCFNJobTypeAsString( _cmd );
        this.myEnv.enhancedUserInput.setCmd( _cmd, cfnJobTYPEString );

        // --------------------
        return new Tuple<String,String>( AWSRegion, AWSLocation );
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    private void  init( final CmdLineArgs _cmdLA ) throws Exception
    {   final String HDR = CLASSNAME + ": init(" + _cmdLA.cmdName + "): ";

        // Now check and obtain ALL the important Property Objects.
        if( this.myEnv.getAllPropsRef().get( "Tags" ) == null ) {
            this.myEnv.getAllPropsRef().put( "Tags", new Properties() );
        }
        final Properties globalProps = this.myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
        assertTrue( globalProps != null );

        final Properties AWSRegionsLocations = org.ASUX.common.Utils.parseProperties( "@"+ this.myEnv.get_awssdkhome()  +"/"+ Environment.AWSREGIONSLOCATIONS );
        final Properties AWSLocationsRegions = org.ASUX.common.Utils.parseProperties( "@"+ this.myEnv.get_awssdkhome()  +"/"+ Environment.AWSLOCATIONSREGIONS );
        this.myEnv.getAllPropsRef().put( "AWSRegionsLocations", AWSRegionsLocations );
        this.myEnv.getAllPropsRef().put( "AWSLocationsRegions", AWSLocationsRegions );
        // We need this specific '{AWSCFNHOME}/config/AWSRegionsLocations.properties' because we need to CONVERT a AWSRegion into an AWSLocation
        // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ awscfnhome  +"/"+ AWSREGIONSLOCATIONS ) );

        // --------------------
        // The following will be ineffective for '--fullstack' (a.k.a.  cmdName == Enums.GenEnum.FULLSTACK )
        // The following will have to be 're-calculated' within CmdProcessorFullStack.java
        final Tuple<String,String> tuple = this.evalAWSRegionLocation( _cmdLA.cmdName );
        final String AWSRegion   = tuple.key;
        final String AWSLocation = tuple.val;
        assertTrue( this.myEnv.enhancedUserInput != null );

        globalProps.setProperty( "AWSLocation", AWSLocation );

        // return tuple;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  Checks for critical environment variables and for critical cmdline parameters like AWSRegion. Then loads all Propertyfiles for the job.
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @throws Exception on any missing variables or parameters
     */
    public void check( final CmdLineArgs _cmdLA ) throws Exception
    {   final String HDR = CLASSNAME + ": check(" + _cmdLA.cmdName + "): ";

        final Properties sysprops     = this.myEnv.getAllPropsRef().get( org.ASUX.common.OSScriptFileScanner.SYSTEM_ENV );
        final String orgasuxhome      = sysprops.getProperty("ORGASUXHOME");
        final String awshome          = sysprops.getProperty("AWSHOME");
        final String awscfnhome       = sysprops.getProperty("AWSCFNHOME");

        // --------------------
        final String cfnJobTYPEString = UserInputEnhanced.getCFNJobTypeAsString( _cmdLA.cmdName );
        if (this.verbose) System.out.println( HDR + "cfnJobTYPEString=" + cfnJobTYPEString );

        // --------------------
        if (orgasuxhome == null) {
            throw new Exception("ERROR! " + Environment.ORGASUXHOME + " is NOT defined.");
        }
        if (awshome == null) {
            throw new Exception("ERROR! " + Environment.AWSHOME + " is NOT defined.");
        }
        if (awscfnhome == null) {
            throw new Exception("ERROR! " + Environment.AWSCFNHOME + " is NOT defined.");
        }

        this.myEnv.setHomeFolders( orgasuxhome, awshome, awscfnhome );
        if (this.verbose) System.out.println(HDR + "ORGASUXHOME=" + this.myEnv.get_orgasuxhome() + " AWSHOME=" + this.myEnv.get_awshome() + " AWSCFNHOME=" + this.myEnv.get_awscfnhome()  + " jobSetName=" + _cmdLA.jobSetName );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        fileCheck( this.myEnv.get_orgasuxhome(), "/asux.js", false /* _bMissingIsOk */ );
        // final File asux_js = new File( orgasuxhome +"/asux.js" );
        // if ( ! asux_js.exists() ) {
        //      final String es = "Please ensure the correct value of 'ORGASUXHOME' as thatfolder is missing the file/script 'asux.js', which is currently set to "+orgasuxhome;
        //      System.err.println( es );
        //      System.err.println( "This command will fail until you correct the current value of ORGASUXHOME );
        //      System.exit(5);
        //      throw new Exception( es );
        // }

        fileCheck( this.myEnv.get_awssdkhome(), Environment.AWSREGIONSLOCATIONS, false /* _bMissingIsOk */ );
        fileCheck( this.myEnv.get_awssdkhome(), Environment.AWSLOCATIONSREGIONS, false /* _bMissingIsOk */ );
        // Don't need checks for 'AWSprofile' (a.k.a. ~/.aws/.profile) as this check is done within org.ASUX.AWS-SDK project's code, prior to interacting with AWS-SDK.

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        // Important: The above filechecks will ensure the following init() will Not cause incomprehensible errors (to end-user)

        this.init( _cmdLA );
        // final Tuple<String,String> tuple = this.evalAWSRegionLocation( _cmdLA.cmdName );
        // final String AWSRegion   = tuple.key;
        // final String AWSLocation = tuple.val;

        // --------------------
        final Properties Tags        = this.myEnv.getAllPropsRef().get( "Tags" );
        assertTrue( Tags != null );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        switch ( _cmdLA.cmdName ) {
            case FULLSTACK: // use checkFullStack() instead.
                            break;
            case SUBNET:
            case EC2PLAIN:
            case VPC:
            case SG:
                            fileCheck( this.myEnv.get_awscfnhome(), this.myEnv.getJOB_DEFAULTS_FILEPATH(), false /* _bMissingIsOk */ );
                            fileCheck( _cmdLA.jobSetName, this.myEnv.getJOBSET_MASTER_FILEPATH(), this.myEnv.bInRecursionByFullStack );
                            // fileCheck( _cmdLA.jobSetName, "jobset-" + this.myEnv.cfnJobTYPEString + ".properties" ); // we can't do this for all cfnJob-TYPEs
                            break;
            case VPNCLIENT:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmdLA.cmdName;
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  Checks for critical environment variables and for critical cmdline parameters like AWSRegion. Then loads all Propertyfiles for the job.
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @throws Exception on any missing variables or parameters
     */
    public void checkForFullStack( final CmdLineArgs _cmdLA ) throws Exception
    {   final String HDR = CLASSNAME + ": checkFullStack(" + _cmdLA.cmdName + "): ";

        // --------------------
        switch ( _cmdLA.cmdName ) {
            case FULLSTACK:
                            // Note: We have 'fileCheck()' invocations for these 3 'exclusive-or'  Tags-props-files in this.check() method.
                            // Objective: To determine that at least one of these exists.
                            // So, let's make sure that these below fileCheck() calls .. do _NOT_ throw a FileNotFoundException.
                            // For that the last parameter _MUST_ be true
                            final boolean exists1 = fileCheck( Environment.USERCONFIGHOME_CFN, Environment.TAGS_ALONE_MASTER, true /* _bMissingIsOk */ );
                            if (this.verbose) System.out.println(HDR + "exists1="+ exists1 +" for "+ Environment.USERCONFIGHOME_CFN +"/"+ Environment.TAGS_ALONE_MASTER );
                            final boolean exists2 = fileCheck( Environment.USERCONFIGHOME_CFN, Environment.TAGS_DEPT_MASTER, true /* _bMissingIsOk */ );
                            if (this.verbose) System.out.println(HDR + "exists2="+ exists1 +" for "+ Environment.USERCONFIGHOME_CFN +"/"+ Environment.TAGS_DEPT_MASTER );
                            final boolean exists3 = fileCheck( Environment.USERCONFIGHOME_CFN, Environment.TAGS_ENTERPRISE_MASTER, true /* _bMissingIsOk */ );
                            if (this.verbose) System.out.println(HDR + "exists3="+ exists1 +" for "+ Environment.USERCONFIGHOME_CFN +"/"+ Environment.TAGS_ENTERPRISE_MASTER );
                            if (  !  ( exists1 || exists2 || exists3 ) )
                                throw new Exception( "Missing defaults file named "+ Environment.TAGS_ALONE_MASTER +" or "+ Environment.TAGS_DEPT_MASTER +" or "+ Environment.TAGS_ENTERPRISE_MASTER +" within folder: "+ Environment.USERCONFIGHOME_CFN );
                            break;
            case SUBNET:
            case EC2PLAIN:
            case VPC:
            case SG:
            case VPNCLIENT:
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
    // * @param _jobTYPE a string representing the context (example: vpn, sg, ec2plain).. which is then used to load the appropriate Config-file for User's SPECs.
    /**
     *  Checks for critical environment variables and for critical cmdline parameters like AWSRegion. Then loads all Propertyfiles for the job.
     *  @param _cmdLA a NotNull instance (created within {@link CmdInvoker#processCommand})
     *  @throws Exception on any missing variables or parameters
     */
    public void configure( final CmdLineArgs _cmdLA ) throws Exception
    {   final String HDR = CLASSNAME + ": configure(" + _cmdLA.cmdName + "): ";


        final Properties globalProps = this.myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );

        // --------------------
        final String cfnJobTYPEString = UserInputEnhanced.getCFNJobTypeAsString( _cmdLA.cmdName );
        globalProps.setProperty( "cfnJobTYPE", cfnJobTYPEString ); // this.myEnv.getCfnJobTYPEString()
        globalProps.setProperty( "JobSetName", _cmdLA.jobSetName );
        globalProps.setProperty( "ItemNumber", _cmdLA.itemNumber );
        globalProps.setProperty( "Scope", _cmdLA.scope );
        globalProps.setProperty( "PublicOrPrivate", _cmdLA.scope );
        if ( _cmdLA.scope.startsWith("Public"))
            globalProps.setProperty( "PublicOrPrivate", "Public" );
        // final String InitialCapitalStr = Character.toUpperCase( _cmdLA.scope.charAt(0) ) + _cmdLA.scope.substring(1);
        // globalProps.setProperty( "PublicOrPrivateStr", InitialCapitalStr );
        if (this.verbose) System.out.println( HDR + "JobSetName=" + _cmdLA.jobSetName + " ItemNumber=" + _cmdLA.itemNumber + " Scope=" + _cmdLA.scope );

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

        switch ( _cmdLA.cmdName ) {
            case EC2PLAIN:
            case VPC:
            case SG:
            case SUBNET:
                            globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ this.myEnv.get_awscfnhome()  +"/"+ this.myEnv.getJOB_DEFAULTS_FILEPATH() ) );
                            // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _cmdLA.jobSetName +"/"+ Environment.JOBSET_MASTER ) );
                            // globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ _cmdLA.jobSetName +"/jobset-" + this.myEnv.cfnJobTYPEString + ".properties" ) );
                            final boolean isItOkIfFileIsMissing = this.myEnv.bInRecursionByFullStack;
                            loadPropsIntoGlobal( "@"+ _cmdLA.jobSetName +"/"+ this.myEnv.getJOBSET_MASTER_FILEPATH(),    globalProps, isItOkIfFileIsMissing );
                            loadPropsIntoGlobal( "@"+ _cmdLA.jobSetName +"/jobset-" + this.myEnv.getCfnJobTYPEString() + ".properties",   globalProps, isItOkIfFileIsMissing );
                            final Tuple<String,String> tuple = this.evalAWSRegionLocation( _cmdLA.cmdName );
                            final String AWSRegion   = tuple.key;
                            final String AWSLocation = tuple.val;
                            // --------------------
                            if (  !  this.myEnv.bInRecursionByFullStack ) { // if boot.configure() is being recursively called within CmdProcessorFullStack.java
                                final Stack stack = new Stack( this.verbose, AWSRegion, AWSLocation, Enums.StackComponentType.fromCmdType(_cmdLA.cmdName,_cmdLA.scope) );
                                this.myEnv.setStack( stack );
                                break;
                            }
            case FULLSTACK: // get defaults from ~/.ASUX.org/.
                            // Note: We have 'fileCheck()' invocations for these 3 Tags-props-files in this.check() method.
                            // So, we know __at-least__ one of these exists.
                            // So, let's make sure that these below loadPropsIntoGlobal() calls .. do _NOT_ throw a FileNotFoundException.
                            // For that the last ARGUMENT __MUST__ be true
// ATTENTION!!! Loading these (below) property files is TOO early.
// REASON: It gets OVERWRITTEN by the config/DEFAULTS/Tags_Defaults.properties - within the ASUX-BATCH scripts.
// So, the ASUX-BATCH scripts should __RE-LOAD__ these.
                            final Properties Tags        = this.myEnv.getAllPropsRef().get( "Tags" );
                            loadPropsIntoGlobal( "@"+ Environment.USERCONFIGHOME_CFN +"/"+ Environment.TAGS_ALONE_MASTER,       Tags, true /* _bMissingIsOk */ );
                            loadPropsIntoGlobal( "@"+ Environment.USERCONFIGHOME_CFN +"/"+ Environment.TAGS_DEPT_MASTER,        Tags, true /* _bMissingIsOk */ );
                            loadPropsIntoGlobal( "@"+ Environment.USERCONFIGHOME_CFN +"/"+ Environment.TAGS_ENTERPRISE_MASTER,  Tags, true /* _bMissingIsOk */ );
                            globalProps.putAll( org.ASUX.common.Utils.parseProperties( "@"+ this.myEnv.get_awscfnhome()  +"/"+ this.myEnv.getJOB_DEFAULTS_FILEPATH() ) );
                            final Tuple<String,String> tuple2 = this.evalAWSRegionLocation( _cmdLA.cmdName );
                            final String AWSRegion2   = tuple2.key;
                            final String AWSLocation2 = tuple2.val;
                            // --------------------
                            if ( this.myEnv.getStackSet() == null ) { // StackSET .. NOT Stack.
                                // ..getStackSet() can be != null, if configure() is invoked repeatedly for '--fullstack-gen' (CmdProcessorFullStack.java)
                                final StackSet stackSet = new StackSet( this.verbose, AWSRegion2, AWSLocation2 );
                                this.myEnv.setStackSet( stackSet );
                            }
                            break;
            case UNDEFINED:
            default:        final String es = " Unimplemented command: " + _cmdLA.cmdName;
                            System.err.println( HDR + es );
                            throw new Exception( es );
        }
        if (this.verbose) System.out.println( HDR + "Currently " + globalProps.size() + " entries into globalProps." );

        // --------------------
        final String MyVPCStackPrefix = Macros.evalThoroughly( this.verbose, "${ASUX::MyOrgName}-${ASUX::MyEnvironment}-${ASUX::AWSLocation}", this.myEnv.getAllPropsRef() );
        final String MyStackNamePrefix = Macros.evalThoroughly( this.verbose, "${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-${ASUX::JobSetName}${ASUX::ItemNumber}", this.myEnv.getAllPropsRef() );
        globalProps.setProperty( "MyStackNamePrefix", MyStackNamePrefix );
        globalProps.setProperty( Environment.MYVPCSTACKPREFIX, MyVPCStackPrefix );
        this.myEnv.enhancedUserInput.setMyStackNamePrefix( MyStackNamePrefix );
        // this.myEnv.enhancedUserInput.setMyVPCStackPrefix ( MyVPCStackPrefix  );
        if (this.verbose) System.out.println( HDR + "MyStackNamePrefix=" + MyStackNamePrefix + " MyVPCStackPrefix=" + MyVPCStackPrefix );

        final String VPCID = MyVPCStackPrefix + "-VPCID"; // Macros.evalThoroughly( this.verbose, "${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-VPCID", this.myEnv.getAllPropsRef()  );
        final String DefaultAZ = MyVPCStackPrefix + "-AZ-ID"; // Macros.evalThoroughly( this.verbose, "${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-AZ-ID", this.myEnv.getAllPropsRef() );
        globalProps.setProperty( "VPCID", VPCID ) ;
        globalProps.setProperty( "DefaultAZ", DefaultAZ );

        if (this.verbose) System.out.println( HDR + "VPCID=" + VPCID + " DefaultAZ=" + DefaultAZ );
        if (this.verbose) System.out.println( HDR + "globalProps: Total # of entries = " + globalProps.size() + "." );

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
