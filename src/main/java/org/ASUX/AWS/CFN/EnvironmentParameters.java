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
import java.io.Serializable;
import java.nio.file.FileSystems;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * 
 */
public final class EnvironmentParameters implements Serializable {

    private static final long serialVersionUID = 439L;

    public static final String CLASSNAME = EnvironmentParameters.class.getName();

    public static final String ORGASUXHOME  = "ORGASUXHOME";
    public static final String AWSHOME      = "AWSHOME";
    public static final String AWSCFNHOME   = "AWSCFNHOME";
    public static final String USERHOMEDIR          = System.getProperty("user.home");
    public static final String USERCONFIGHOME       = USERHOMEDIR + "/.ASUX.org";
    public static final String USERCONFIGHOME_CFN   = USERCONFIGHOME + "/AWS.CFN";

    public static final String PROPERTIES_FOR_CFN = "cfn.properties"; // one of the many Properties objects within this.allPropsRef (see go())
    public static final String PROPERTIES_FOR_JOB = "job.properties"; // one of the many Properties objects within this.allPropsRef (see go())

    public static final String ALONE = "Alone";
    public static final String DEPT = "Dept";
    public static final String ENTERPRISE = "Enterprise";
    public static final String TAGS_ALONE_MASTER = "Tags-Alone-Master.properties"; // one of the many Properties objects within this.allPropsRef (see go())
    public static final String TAGS_DEPT_MASTER = "Tags-Dept-Master.properties"; // one of the many Properties objects within this.allPropsRef (see go())
    public static final String TAGS_ENTERPRISE_MASTER = "Tags-Enterprise-Master.properties"; // one of the many Properties objects within this.allPropsRef (see go())

    public static final String AWSREGIONSLOCATIONS  = "config/AWSRegionsLocations.properties";
    public static final String AWSLOCATIONSREGIONS  = "config/AWSLocationsRegions.properties";

    public static final String MYSTACKNAMEPREFIX    = "MyStackNamePrefix";
    public static final String MYVPCSTACKPREFIX     = "MyVPCStackPrefix";
    public static final String MYEC2INSTANCENAME    = "MyEC2InstanceName";

    public static final String MYORGNAME            = "MyOrgName";
    public static final String MYENVIRONMENT        = "MyEnvironment";
    public static final String MYDOMAINNAME         = "MyDomainName";
    public static final String MYRT53HOSTEDZONEID   = "MyRt53HostedZoneId";

    public static final String VPCCIDRBLOCK         = "VPCCIDRBLOCK";

    public static final String CFNINIT_PACKAGES = "AWS-CFNInit-Standup";
    public static final String CFNINIT_SERVICES = "AWS-CFNInit-Services";
    public static final String EC2INSTANCETYPE = "EC2InstanceType";
    public static final String EC2IAMROLES = "MyIAM-roles";

    // ------ private ------
    private static final String JOB_DEFAULTS = "/config/DEFAULTS/job-DEFAULTS.properties"; // under AWSCFNHOME
    private static final String FULLSTACKJOB_DEFAULTS = "/config/DEFAULTS/FullStackJob-DEFAULTS.properties"; // under AWSCFNHOME
    private static final String JOBSET_MASTER = "jobset-Master.properties"; // under '.' folder

    private static final String prefixFULLSTACK = "fullstack";

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    // The following .. once they are set by BootCheckAndConfig.config(), they are untouched.
    public boolean verbose;

    private String orgasuxhome = "UNDEFINED";
    private String awshome = "UNDEFINED";
    private String awscfnhome = "UNDEFINED";

    private String AWSRegion = "UNDEFINED";
    private String AWSLocation = "UNDEFINED";
    private String MyStackNamePrefix = "UNDEFINED";
    private String MyVPCStackPrefix = "UNDEFINED";

    private Enums.GenEnum cfnJobTypEnum = Enums.GenEnum.UNDEFINED;
    private String cfnJobTYPEString = "UNDEFINED";

    private boolean bExistingVPC = false;
    private boolean bExistingSubnet = false;
    private String  existingVPCID = null;
    private String  existingSubnetID = null;

    // ---------------- PRIVATE ----------------
    private transient LinkedHashMap<String, Properties> allPropsRef; // this could have been 'final' too, but for the fact that this.deepClone() needs to reset it.

    // --------- following are redefined by fullstack-gen -----------
    public boolean bInRecursionByFullStack = false;
    public String outputFolderPath = "/tmp";

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    /**
     * The only constructor
     * 
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _allProps a (NotNull) reference provided by CmdInvoker().memoryAndContext.getAllPropsRef().. or other source
     */
    public EnvironmentParameters( final boolean _verbose, final LinkedHashMap<String, Properties> _allProps ) {
        this.verbose = _verbose;
        this.allPropsRef = _allProps;
        // this.cfnJobTypEnum = _cfnJobTypEnum;
        // this.cfnJobTYPEString = BootCheckAndConfig.getCFNJobTypeAsString(
        // this.cfnJobTypEnum );
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    public void setHomeFolders( final String _orgasuxhome, final String _awshome, final String _awscfnhome ) {
        this.orgasuxhome = _orgasuxhome;
        this.awshome = _awshome;
        this.awscfnhome = _awscfnhome;
    }

    public void setFundamentalGlobalProps( final String _AWSRegion, final String _AWSLocation ) {
        this.AWSRegion = _AWSRegion;
        this.AWSLocation = _AWSLocation;
    }

    public void setFundamentalPrefixes( final String _MyStackNamePrefix, final String _MyVPCStackPrefix ) {
        this.MyStackNamePrefix = _MyStackNamePrefix;
        this.MyVPCStackPrefix = _MyVPCStackPrefix;
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    public void setCmd( final Enums.GenEnum _cfnJobTypEnum ) throws Exception {
        this.cfnJobTypEnum = _cfnJobTypEnum;
        this.cfnJobTYPEString = BootCheckAndConfig.getCFNJobTypeAsString( this.cfnJobTypEnum );
    }

    /**
     * Use this method to set the flags whether user has specified an existing VPC (+ optionally an existing subnet also).  Based on these flags, the appropriate '-gen.ASUX.batch.txt' scripts are executed.
     * @param _existingVPCID a Nullable string. A non-null value represents the AWS VPC-ID of an existing VPC.
     * @param _existingSubnetID a Nullable string. A non-null value represents the AWS SUBNET-ID of an existing Subnet (whether public or private).
     */
    public void setExisting( final String _existingVPCID, final String _existingSubnetID ) {
        this.bExistingVPC = _existingVPCID != null;
        this.bExistingSubnet = _existingSubnetID != null;
        this.existingVPCID = _existingVPCID;
        this.existingSubnetID = _existingSubnetID;
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * The getter-method to the ONLY private instance-variable of this class.
     * 
     * @return NotNull instance, unless of course, logic-errors led to the
     *         constructor being called to set this.allPropsRef to null.
     */
    public LinkedHashMap<String, Properties> getAllPropsRef() {
        return this.allPropsRef;
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    public Enums.GenEnum getCmdEnum() {
        return this.cfnJobTypEnum;
    }

    public String getCfnJobTYPEString() {
        if (this.bInRecursionByFullStack) {
            if ( this.bExistingSubnet )
                return this.prefixFULLSTACK +"-"+ this.cfnJobTYPEString + "ExistingSubnet";
            else
            return this.prefixFULLSTACK +"-"+ this.cfnJobTYPEString;
        } else {
            return this.cfnJobTYPEString;
        }
    }

    // ==============================================================================
    public String get_orgasuxhome()         { return this.orgasuxhome; }

    public String get_awshome()             { return this.awshome; }

    public String get_awssdkhome()          { return this.awshome + "/AWS-SDK"; }

    public String get_awscfnhome()          { return this.awscfnhome; }

    public static String get_cwd()                 { return FileSystems.getDefault().getPath( System.getProperty("user.dir") ).toString(); } // throws RunTimeExceptions only.

    // ==============================================================================
    public String getAWSRegion()            { return this.AWSRegion; }

    public String getAWSLocation()          { return this.AWSLocation; }

    // ==============================================================================
    public String getMyStackNamePrefix()    { return this.MyStackNamePrefix; }

    public String getMyVPCStackPrefix()     { return this.MyVPCStackPrefix; }

    // ==============================================================================
    public boolean isExistingVPC()          { return this.bExistingVPC; }
    public boolean isExistingSubnet()       { return this.bExistingSubnet; }

    /**
     * @return can be Null. Depending on whether user has specified an existing VPC (in the full-stack job's YAML)
     */
    public String getExistingVPCID()        { return this.existingVPCID; }

    /**
     * @return can be Null. Depending on whether user has specified an existing subnet (in the full-stack job's YAML).
     */
    public String getExistingSubnetID()     { return this.existingSubnetID; }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    public String getJOB_DEFAULTS()
                throws Exception
    {   final String HDR = CLASSNAME + ": getJOB_DEFAULTS(): ";
        switch (this.cfnJobTypEnum) {
            case FULLSTACK:
                            return FULLSTACKJOB_DEFAULTS; // prefixFULLSTACK +"/"+ JOB_DEFAULTS;
            case SUBNET:
            case EC2PLAIN:
            case VPC:
            case SGSSH:
                        if ( this.bInRecursionByFullStack )
                            return FULLSTACKJOB_DEFAULTS;
                        else
                            return JOB_DEFAULTS;
            case VPNCLIENT:
            case SGEFS:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + this.cfnJobTypEnum;
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

    // =================================================================================
    public String getJOBSET_MASTER()
                throws Exception
    {   final String HDR = CLASSNAME + ": getJOBSET_MASTER(): ";
        switch ( this.cfnJobTypEnum ) {
            case FULLSTACK:     return JOBSET_MASTER;
            case SUBNET:
            case EC2PLAIN:
            case VPC:
            case SGSSH:         return JOBSET_MASTER;
            case VPNCLIENT:
            case SGEFS:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + this.cfnJobTypEnum;
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

    /**
     * This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class.
     * 
     * @param _orig what you want to deep-clone
     * @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static EnvironmentParameters deepClone( EnvironmentParameters _orig ) {
        try {
            final EnvironmentParameters newobj = org.ASUX.common.Utils.deepClone(_orig);
            newobj.deepCloneFix(_orig);
            return newobj;
        } catch (Exception e) {
            e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    /**
     * In order to allow deepClone() to work seamlessly up and down the class-hierarchy.. I should allow subclasses to EXTEND (Not semantically override) this method.
     * 
     * @param _orig the original NON-Null object
     */
    protected void deepCloneFix( final EnvironmentParameters _orig ) {
        // because this class has at least one TRANSIENT class-variable.. ..
        // we need to 'restore' that object's transient variable to a 'replica'
        this.allPropsRef = _orig.allPropsRef;
    }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

};
