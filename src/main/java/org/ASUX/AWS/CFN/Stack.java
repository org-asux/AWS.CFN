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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 *  <p>This class represents a single 'stack' associated with a single CloudFormation(CFN)-Template file.</p>
 *  <p>A Stack - per AWS definition - is a combination of a CFN-Template file, AWSRegion and Parameter-values.</p>
 *  <p>This class can also be seen as representing the CLI-command "aws cloudformation create-stack .. --parameters .."  in a structured manner, so that the same command can be re-represented as a NESTED-Stack.</p>
 */
public final class Stack implements Serializable
{
    private static final long serialVersionUID = 467L;

    public static final String CLASSNAME = Stack.class.getName();

    public boolean verbose;

    public String AWSRegion;
    public String AWSLocation;

    // ----------- PRIVATE ----------
    private String stackName;
    // private String stackFileName;
    private String CFNTemplateFile;

    private final LinkedHashMap<String,String> parameters = new LinkedHashMap<>();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public Stack( final boolean _verbose, final String _awsregion, final String _awslocation ) {
        this.verbose = _verbose;
        this.AWSRegion = _awsregion;
        this.AWSLocation = _awslocation;
        assertTrue( this.AWSRegion != null );
        assertTrue( this.AWSLocation != null );
        // this.CFNTemplateFile = _cfntemplatefile; // , final String _cfntemplatefile
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public void addParameter( final String _paramName, final String _paramValue )
    {   final String HDR = CLASSNAME + ": addParameter("+ _paramName +","+ _paramValue +"): ";
        final String oldval = this.parameters.get( _paramName );
        if ( oldval != null &&   !   oldval.equals(_paramValue)   && this.verbose ) System.out.println( HDR + "overwriting old value '"+ oldval +"' with '"+ _paramValue +"." );
        this.parameters.put( _paramName, _paramValue );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public String getStackName()                        { return this.stackName; }
    public void   setStackName( final String _sn )      { this.stackName = _sn;  }

    public String getAWSRegion()                        { return this.AWSRegion; }
    public void   setAWSRegion( final String _sn )      { this.AWSRegion = _sn;  }

    public String getAWSLocation()                        { return this.AWSLocation; }
    public void   setAWSLocation( final String _sn )      { this.AWSLocation = _sn;  }

    /** 
     *  <p>In contrast with getStackName(), which can return a string containing '-', '_', etc.. (pretty much any Java-String that can be a valid file-name)..<br>
     *      this method will return a String __DEVOID__ of '-', '_', '.', .. (leaving only AlphaNumerics, per the AWS-CFN-YAML Resource-Naming specifications)</p>
     * @return a NotNull Alphanumeric String
     */
    public String getStackId()                          { return this.getStackName().replaceAll("-","").replaceAll("_","").replaceAll("\\.",""); }

    public String getCFNTemplateFile()                  { return this.CFNTemplateFile; }
    public void   setCFNTemplateFile( final String _cf )  { this.CFNTemplateFile = _cf; }

    public String toString() {
        return this.getStackName() +", "+ this.AWSRegion +", "+ this.getParamsAsString() +", "+ this.getCFNTemplateFile() ;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  @return get back the content passed in via multiple invocations of {@link #addParameter(String, String)}, as a HashMap of pairs-of-Strings.
     */
    public LinkedHashMap<String,String> getParams() { return this.parameters; }

    /**
     *  A string to use As-Is as the parameters within the "aws cloudformation create-stack .. --parameters .." command.
     *  @return a String of the form " ParameterKey=Key1,ParameterValue=Val1 ParameterKey=Key2,ParameterValue=Val2 ParameterKey=Key3,ParameterValue=Val3".  Note the leading blank, but no trailing blank.
     */
    public String getParamsAsString()       {
        return Stack.getParamsAsString( this.parameters );
    }

    /**
     *  A string to use As-Is as the parameters within the "aws cloudformation create-stack .. --parameters .." command.
     *  @param _parameters a NotNull instance (expected to be Stack().parameters instance-variable or StackSet().parameters instance-variable)
     *  @return a String of the form " ParameterKey=Key1,ParameterValue=Val1 ParameterKey=Key2,ParameterValue=Val2 ParameterKey=Key3,ParameterValue=Val3".  Note the leading blank, but no trailing blank.
     */
    public static String getParamsAsString( final LinkedHashMap<String,String> _parameters ) {
        final StringBuffer buf = new StringBuffer();
        for( String key: _parameters.keySet() ) {
            final String val = _parameters.get( key );
            buf.append( " ParameterKey=" ).append( key ).append( ",ParameterValue=" ).append( val );
        }
        return buf.toString();
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public String genCLICmd( final String _folderPath ) {
        return Stack.genCLICmd( this.getStackName(), this.AWSRegion, this.getParamsAsString(), this.getCFNTemplateFile(), _folderPath );
    }

    /**
     *  Reusable code for use by this class and by {@link StackSet}.
     *  @param _stackName since this implementation of toString(), can be null
     *  @param _AWSRegion since this implementation of toString(), can be null
     *  @param _params since this implementation of toString(), can be null
     *  @param _cfnfile since this implementation of toString(), can be null
     *  @param _folderPath the folder in which the CFN-Template-File is location (the file represented by {@link #getCFNTemplateFile()}
     *  @return a NotNull String
     */
    public static String genCLICmd( final String _stackName, final String _AWSRegion, final String _params, final String _cfnfile, final String _folderPath ) {
        return "aws cloudformation create-stack --stack-name "+ _stackName
            +" --region "+ _AWSRegion
            +" --parameters "+ _params
            +" --template-body file://"+ _folderPath +"/"+_cfnfile
            +" --profile ${AWSprofile} ";
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>A utility method that allows you to incorporate the CFN-Template (represented by this instance) as a Nested-Stack within another.</p>
     *  <p>The invoking-code _must_ have "uploaded" the file represented by {@link #CFNTemplateFile} into S3 and must provide that URL-to-S3, as the only argument to this method.</p>
     *  @param _s3ObjectURL must be a valid URL to an object containing the CFN-Template for one specific stack
     *  @param _dependsOn can be Null.  This will show up as "DependsOn:\n - ..." in the YAML generated.  Very important to help "SEQUENCE" the AWS components within a _STACKSET_ (Note: SET)
     *  @return returns the NotNull, with 1st elem as the YAML-Key and the 2nd as the YAML-as-MultiLine-String that can be embedded AS-IS (as a Nested-Stack) within another Cloudformation YAML.
     */
    public Tuple<String,String> getCFNYAMLString( final String _s3ObjectURL, final String _dependsOn )
    {   final String HDR = CLASSNAME + ": getCFNYAMLString(_s3ObjectURL,"+ _dependsOn +"): ";

        if ( this.verbose ) System.out.println( HDR + "this="+ this.toString() );
        final StringBuffer buf = new StringBuffer();
        // Note: Resource-Names within a CFN Template can ONLY be AlphaNumeric
        // Note since this YAML is embedded inside another YAML, there at least 1 tab-char at the beginning of _EACH_ line.
        buf.append( "   " ).append( this.getStackId() ).append( ":     ### create-stack --stack-name ").append( this.getStackName() ).append( "\n" );
        buf.append( "      Type: AWS::CloudFormation::Stack\n" );
        if ( _dependsOn != null ) {
            buf.append( "      DependsOn:\n");
            buf.append( "      - ").append( _dependsOn ).append("\n");
        }
        buf.append( "      Properties:\n" );
        buf.append( "         TemplateURL: "+ _s3ObjectURL +"\n" );
                // # NotificationARNs  ### OPTIONAL: SNS-topic ARNs to publish the stack-related-events.
                // #   - ARN1          ### Maximum 5 ARNs\n
        if ( this.parameters.size() > 0 ) {
            buf.append( "         Parameters:\n" );
            for( String key: this.parameters.keySet() ) {
                final String val = this.parameters.get( key );
                buf.append( "            " ).append( key ).append( ": " ).append( val ).append( "\n" );
            }
        }
                // # Tags: 
                // #   - Key: __
                // #     Value: ___
                // # TimeoutInMinutes: '3'   ### When CloudFormation detects that the nested-stack has reached the CREATE_COMPLETE state, it marks the nested-stack resource as CREATE_COMPLETE in the parent-stack and resumes creating the parent-stack.

        return new Tuple<String,String>( this.getStackId(), buf.toString() );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Supporting method to the 4 utility functions to help STANDARDIZE the naming of STACKS - whether for VPC, SUBNET, SG OR EC2.. ..</p>
     *  <p>This specific method is actually invoked by {@link #genSubnetStackName(CmdLineArgs)} and {@link #genSGStackName(CmdLineArgs)}, to help appropriately incorporate the value of the &lt;itemNumber&gt; cmdline arguments for 'subnet-gen' amd 'sg-gen' commands</p>
     *  @param _cmdLA_itemNumber a Nullable String
     *  @return NotNull String (can be empty-string)
     */
    public static final String getItemNumberSuffix( final String _cmdLA_itemNumber ) {
        // final String itemSuffix = ( _cmdLA_itemNumber == null || "".equals(_cmdLA_itemNumber.trim()) ) ? "" : "-"+ _cmdLA_itemNumber;
        final String itemSuffixWWOHyphen = ( _cmdLA_itemNumber == null || "".equals(_cmdLA_itemNumber.trim()) ) ? "" : _cmdLA_itemNumber;
        // WWO === With or With-OUT
        final String itemSuffix = ( itemSuffixWWOHyphen == null || "".equals(itemSuffixWWOHyphen) || itemSuffixWWOHyphen.startsWith("-") ) ? itemSuffixWWOHyphen : "-"+ itemSuffixWWOHyphen;
        return itemSuffix;
    }

    //=================================================================================
    /**
     *  <p>One of the set of 4 utility functions to help STANDARDIZE the naming of STACKS - whether for VPC, SUBNET, SG OR EC2.. ..</p>
     *  <p>{@link #genVPCStackName(CmdLineArgs)}, {@link #genSubnetStackName(CmdLineArgs)}, {@link #genSGStackName(CmdLineArgs)} and {@link #genEC2StackName(CmdLineArgs)}</p>
     *  @param _cmdLA a NotNull instance
     *  @return NotNull String
     */
    public static final String genVPCStackName( final CmdLineArgs _cmdLA ) {
        return "${ASUX::"+ Environment.MYVPCSTACKPREFIX +"}-VPC";
    }

    //=================================================================================
    /**
     *  <p>One of the set of 4 utility functions to help STANDARDIZE the naming of STACKS - whether for VPC, SUBNET, SG OR EC2.. ..</p>
     *  <p>{@link #genVPCStackName(CmdLineArgs)}, {@link #genSubnetStackName(CmdLineArgs)}, {@link #genSGStackName(CmdLineArgs)} and {@link #genEC2StackName(CmdLineArgs)}</p>
     *  @param _cmdLA a NotNull instance
     *  @return NotNull String
     */
    public static final String genSubnetStackName( final CmdLineArgs _cmdLA ) {
        return "${ASUX::"+ Environment.MYVPCSTACKPREFIX +"}-"+ _cmdLA.PublicOrPrivate +"-"+ _cmdLA.jobSetName + Stack.getItemNumberSuffix(_cmdLA.itemNumber) +"-subnet";
    }

    //=================================================================================
    /**
     *  <p>One of the set of 4 utility functions to help STANDARDIZE the naming of STACKS - whether for VPC, SUBNET, SG OR EC2.. ..</p>
     *  <p>{@link #genVPCStackName(CmdLineArgs)}, {@link #genSubnetStackName(CmdLineArgs)}, {@link #genSGStackName(CmdLineArgs)} and {@link #genEC2StackName(CmdLineArgs)}</p>
     *  @param _cmdLA a NotNull instance
     *  @return NotNull String
     */
    public static final String genSGStackName( final CmdLineArgs _cmdLA ) {
        return "${ASUX::"+Environment.MYVPCSTACKPREFIX+"}-"+_cmdLA.PublicOrPrivate +"-"+  _cmdLA.jobSetName + Stack.getItemNumberSuffix(_cmdLA.itemNumber) +"-SG";
                                                        // we're re-purposing '_cmdLA.PublicOrPrivate' for passing/storing the SG-PORT# (ssh/https/..) as provided by user on commandline.
    }

    //=================================================================================
    /**
     *  <p>One of the set of 4 utility functions to help STANDARDIZE the naming of STACKS - whether for VPC, SUBNET, SG OR EC2.. ..</p>
     *  <p>{@link #genVPCStackName(CmdLineArgs)}, {@link #genSubnetStackName(CmdLineArgs)}, {@link #genSGStackName(CmdLineArgs)} and {@link #genEC2StackName(CmdLineArgs)}</p>
     *  @param _cmdLA a NotNull instance
     *  @return NotNull String
     */
    public static final String genEC2StackName( final CmdLineArgs _cmdLA ) {
        return "${ASUX::"+ Environment.MYVPCSTACKPREFIX +"}-"+ _cmdLA.jobSetName +"-EC2-${ASUX::"+ Environment.MYEC2INSTANCENAME +"}"+ _cmdLA.itemNumber;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Yet-another utility-function - to help STANDARDIZE the naming of FILE-NAMES for CFN-Stacks</p>
     *  @param _cmd see {@link Enums.GenEnum}
     *  @param _cmdLA a NotNull instance
     *  @param _myEnv a NotNull instance
     *  @return a NotNull String representing JUST the file-name ONLY.  !!ATTENTION!! Prepend it with a Folder-path, to exactly place the file in the right location in the file-system.
     *  @throws Exception for unimplemented commands (code-safety checks)
     */
    public static final String genStackCFNFileName( final Enums.GenEnum _cmd, final CmdLineArgs _cmdLA, final Environment _myEnv ) throws Exception {
        return genStackCFNFileName(_cmd, _cmdLA, _myEnv, ".yaml");
    }

    /**
     *  <p>Yet-another utility-function - to help STANDARDIZE the naming of FILE-NAMES for script that _EXECUTE_ 'aws cloudformation' commands for CFN-Stacks</p>
     *  @param _cmd see {@link Enums.GenEnum}
     *  @param _cmdLA a NotNull instance
     *  @param _myEnv a NotNull instance
     *  @return a NotNull String representing JUST the file-name ONLY.  !!ATTENTION!! Prepend it with a Folder-path, to exactly place the file in the right location in the file-system.
     *  @throws Exception for unimplemented commands (code-safety checks)
     */
    public static final String genStackScriptFileName( final Enums.GenEnum _cmd, final CmdLineArgs _cmdLA, final Environment _myEnv ) throws Exception {
        return genStackCFNFileName(_cmd, _cmdLA, _myEnv, ".sh");
    }


    private static final String genStackCFNFileName( final Enums.GenEnum _cmd, final CmdLineArgs _cmdLA, final Environment _myEnv, final String _suffix ) throws Exception
    {   final String HDR = CLASSNAME + ": getStackCFNFileName("+ _cmd +"_cmdLA,_myEnv,"+ _suffix +"): ";

        switch ( _cmd ) {
            case VPC:       return _myEnv.getCfnJobTYPEString() + Stack.getItemNumberSuffix(_cmdLA.itemNumber) +_suffix; // ".yaml";
            case SUBNET:    return _myEnv.getCfnJobTYPEString() +"-"+ _cmdLA.PublicOrPrivate + Stack.getItemNumberSuffix(_cmdLA.itemNumber) +_suffix; // ".yaml";
            case SG:        return _myEnv.getCfnJobTYPEString() +"-"+ _cmdLA.PublicOrPrivate + Stack.getItemNumberSuffix(_cmdLA.itemNumber) +_suffix; // ".yaml";
                            // we're re-purposing '_cmdLA.PublicOrPrivate' for passing/storing the SG-PORT# (ssh/https/..) as provided by user on commandline.
            case EC2PLAIN:
                            final Properties globalProps = _myEnv.getAllPropsRef().get( org.ASUX.common.ScriptFileScanner.GLOBALVARIABLES );
                            return _myEnv.getCfnJobTYPEString() +"-"+ globalProps.getProperty( Environment.MYEC2INSTANCENAME ) +_suffix; // ".yaml";
            case FULLSTACK:
                            return null;    // <<------- <<--------
            case VPNCLIENT:
            case UNDEFINED:
            default:        final String es = HDR +" Unimplemented command: " + _cmd;
                            System.err.println( es );
                            throw new Exception( es );
        } // switch
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
