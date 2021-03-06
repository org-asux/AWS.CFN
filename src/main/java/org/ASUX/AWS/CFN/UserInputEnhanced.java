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
public class UserInputEnhanced extends UserInput
{
    private static final long serialVersionUID = 438L;

    public static final String CLASSNAME = UserInputEnhanced.class.getName();

    public boolean bExistingVPC = false;
    public boolean bExistingSubnet = false;
    public String  existingVPCID = null;
    public String  existingSubnetID = null;

    //--------------- PRIVATE ---------------
    private String MyStackNamePrefix = "UNDEFINED";

    private String outputFolderPath = "/tmp"; // Default, but the getter-Setter can change this - specifically only for "--fullstack-gen" commands.

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public UserInputEnhanced( final UserInput _uinp ) {
        super( _uinp );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public String getMyStackNamePrefix()    { return this.MyStackNamePrefix; }

    // ==============================================================================

    public void setMyStackNamePrefix( final String _MyStackNamePrefix ) { this.MyStackNamePrefix = _MyStackNamePrefix; }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
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

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * For use by {@link CmdProcessorFullStack}, for creating a StackSet (consisting of multiple CFN-templates in a NEW local folder)
     * @return NotNull String
     */
    public String getOutputFolderPath()    { return this.outputFolderPath; }

    /**
     * See {@link #getOutputFolderPath()}.
     * @param _ofp  Will throw runtime-exception(java.lang.AssertionError) if NULL
     */
    public void setOutputFolderPath( final String _ofp ) {
        this.outputFolderPath = _ofp;
        assertTrue( this.outputFolderPath != null );
    }

    //=================================================================================

    /**
     *  <p>This method allows the code to uniformly determine the "tmp sub-folder" that is specific to the job-set being processed via cmdline.</p>
     *  <p>FYI: The "tmp-folder" is created by {@link CmdProcessorFullStack#genAllCFNs(CmdLineArgs, Environment)}, if it does Not exist.</p>
     *  @return a NotNull Absolute-path string, that represents a subfolder 'tmp' to the folder defined by {@link #setOutputFolderPath(String)}
     *  @see Environment#getJobTmpFolderPath(String)
     *  @see Environment#getJobFolderPath(String)
     */
    public String getTmpFolderPath() {
        return getOutputFolderPath() +"/tmp";
    }

    // ==============================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // ==============================================================================

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

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  Will look for the YAML-Path '/AWS/VPC/Subnet/Public' or '/AWS/VPC/Subnet/Public' -- within Job.yaml file
     *  @param _yamltools a NotNull instance
     *  @param _subnet NotNull YAML rooted at the YAML-Path /AWS/VPC/Subnet -- within Job.yaml file
     *  @return NotNull, either "Public" or "Private".  No other value possible.
     *  @throws Exception if any issues reading/parsing the YAML file 
     */
    public String getPublicOrPrivate( final YAMLTools _yamltools, final Node _subnet ) throws Exception
    {   final String HDR = CLASSNAME + ": getPublicOrPrivate(_subnet): ";

        String strPublicSubnet    = "no";
        String strPrivateSubnet   = "no";

        //-------------------------------------
        try {
            strPublicSubnet    = _yamltools.readStringFromYAML( _subnet, "public" );
        } catch( java.lang.AssertionError ae ) { /* do Nothing */ }

        if ( strPublicSubnet == null )
            strPublicSubnet = "";
        else
            strPublicSubnet = strPublicSubnet.toLowerCase();

        try {
            strPrivateSubnet   = _yamltools.readStringFromYAML( _subnet, "private" );
        } catch( java.lang.AssertionError ae ) { /* do Nothing */ }

        if ( strPrivateSubnet == null )
            strPrivateSubnet = "";
        else
            strPrivateSubnet = strPrivateSubnet.toLowerCase();

        //-------------------------------------
        final boolean isPublicSubnet      = ( strPublicSubnet.equals("yes") );
        final boolean isPublicNATGWSubnet = ( strPublicSubnet.equals( Environment.PUBLIC_WITH_NATGW.toLowerCase() )
                                            || strPublicSubnet.equals( "yes+natgw" ) ); // offer two meaningful alternatives for humans.
        final boolean isPrivateSubnet     = ( strPrivateSubnet.equals("yes") );

        //-------------------------------------
        String PublicOrPrivate = "NeitherPublicNorPrivate"; // by default - in case of existing Subnet ID

        if (   !  isPrivateSubnet ) {
            if ( isPublicSubnet )
               PublicOrPrivate = "Public"; // unless I am 100% sure, I'm _NOT_ making the subnet _PUBLIC_.
            else if ( isPublicNATGWSubnet )
                PublicOrPrivate = Environment.PUBLIC_WITH_NATGW; // unless I am 100% sure, I'm _NOT_ making the subnet _PUBLIC_.
            else
                PublicOrPrivate = "Private";
        } else
            PublicOrPrivate = "Private";

        if ( this.verbose ) System.out.println( HDR +" PublicOrPrivate="+ PublicOrPrivate +" strPublicSubnet="+ strPublicSubnet +" strPrivateSubnet="+ strPrivateSubnet +" isPublicSubnet="+ isPublicSubnet +" isPrivateSubnet="+ isPrivateSubnet );
        //-------------------------------------
        return PublicOrPrivate;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>A simple tool to map the cmd - into a String variable, that is used to load the cmd-specific YAML-BATCH scripts.</p>
     * <p>Example: Enums.GenEnum.VPC("VPC-gen") will return "vpc" - in lowercase.</p>
     *  @param _cmdName  a value of type {@link Enums.GenEnum} - it should come from {@link CmdLineArgs#getCmdName()}
     *  @return a string that is always NOT NULL
     *  @throws Exception on any invalid input or for Incomplete-code scenarios
     */
    public static String getCFNJobTypeAsString( final Enums.GenEnum _cmdName ) throws Exception
    {   final String HDR = CLASSNAME + ": getJobType("+ _cmdName +"): ";
        switch (_cmdName) {
            case VPC: // cfnJobTYPEString="vpc"; break;
            case SUBNET: // cfnJobTYPEString="subnets"; break;
            case SG: // cfnJobTYPEString="sg"; break;
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
     * This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class.
     * 
     * @param _orig what you want to deep-clone
     * @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static UserInputEnhanced deepClone( UserInputEnhanced _orig ) {
        try {
            final UserInputEnhanced newobj = org.ASUX.common.Utils.deepClone(_orig);
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
    protected void deepCloneFix( final UserInputEnhanced _orig ) {
        // Luckily, this class does _NOT_ have _ANY_ TRANSIENT class-variable.. ..
        // Otherwise, this is the place, that we  'restore' that object's transient variable to a 'replica'
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
