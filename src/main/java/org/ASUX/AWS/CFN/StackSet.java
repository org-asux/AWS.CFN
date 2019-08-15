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
import org.ASUX.common.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 *  <p>This class exists to represent the "aws cloudformation create-stack .. --parameters .." command in a structured manner, so that the same command can be re-represented as a NESTED-Stack.</p>
 *  <p>Note: This class <em><code>extends java.util.ArrayList&lt;Stack&gt;</code></em></p>
 */
public final class StackSet implements Serializable
{
    private static final long serialVersionUID = 468L;
    public static final String CLASSNAME = StackSet.class.getName();

    public boolean verbose;

    public final String AWSRegion;
    public final String AWSLocation;

    // /**
    //  *  <p>For each Stack (represented by an index in Super-class/ArrayList), it has dependency on another-stack in the corresponding index.</p>
    //  *  <p>Note: a null in the corresponding index in this, represents No dependency.  For example: by design <code>stackHasDependencyOn.get(0)</code> is always NULL</p>
    //  */
    // protected final ArrayList<String> stackHasDependencyOn = new ArrayList<>();

    // ----------- PRIVATE ----------
    private String stackSetName;
    private String CFNTemplateFile;

    private final ArrayList<Stack> stackHeirarchy = new ArrayList<Stack>();
    private final ArrayList<Stack> stacksCreated = new ArrayList<Stack>();
    private final ArrayList<Stack> stackDependencyIs = new ArrayList<Stack>(); // <<-- Note: Here, the ArrayElements are REFERENCES to Stacks in 'stacksCreated'

    private final LinkedHashMap<String,String> parameters = new LinkedHashMap<>();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public StackSet( final boolean _verbose, final String _awsregion, final String _awslocation ) {
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

    public String getStackSetName()                       { return this.stackSetName; }
    public void   setStackSetName( final String _sn )     { this.stackSetName = _sn;  }

    /** 
     *  <p>In contrast with getStackSetName(), which can return a string containing '-', '_', etc.. (pretty much any Java-String that can be a valid file-name)..<br>
     *      this method will return a String __DEVOID__ of '-', '_', '.', .. (leaving only AlphaNumerics, per the AWS-CFN-YAML Resource-Naming specifications)</p>
     * @return a NotNull purely-Alphanumeric String
     */
    public String getStackSetId()                         { return this.getStackSetName().replaceAll("-","").replaceAll("_","").replaceAll("\\.",""); }

    public String getCFNTemplateFileName()                    { return this.CFNTemplateFile; }
    public void   setCFNTemplateFileName( final String _cf )  { this.CFNTemplateFile = _cf; }

    public String toString() {
        return this.getStackSetName() +", "+ this.AWSRegion +", "+ this.getParamsAsString() +", "+ this.getCFNTemplateFileName() ;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**  @return get back the content passed in via multiple invocations of {@link #addParameter(String, String)}, as a HashMap of pairs-of-Strings. */
    public LinkedHashMap<String,String> getParams() { return this.parameters; }

    //--------------------------
    /** A string to use As-Is as the parameters within the "aws cloudformation create-stack .. --parameters .." command.
     *  @return a String of the form " ParameterKey=Key1,ParameterValue=Val1 ParameterKey=Key2,ParameterValue=Val2 ParameterKey=Key3,ParameterValue=Val3".  Note the leading blank, but no trailing blank.
     */
    public String getParamsAsString()       {
        return Stack.getParamsAsString( this.parameters ); // common implementation
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public String genCLICmd( final String _folderPath ) {
        return Stack.genCLICmd( this.getStackSetName(), this.AWSRegion, this.getParamsAsString(), this.getCFNTemplateFileName(), _folderPath );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public void add( final Stack _stk ) {
        if ( _stk == null ) return;

        this.stacksCreated.add( _stk );

        if ( this.stackHeirarchy.size() >= 1 ) {
            final Stack previous = this.stackHeirarchy.get( this.stackHeirarchy.size() - 1 );
            this.stackDependencyIs.add( previous );
        } else {
            this.stackDependencyIs.add( null ); // null on purpose (so that.. the sizes of 'stackDependencyIs' and 'stacksCreated 'are identical)
        }
        assertTrue( this.stacksCreated.size() == this.stackDependencyIs.size() );

        this.stackHeirarchy.add( _stk );
    }

    //--------------------------
    public void popDependencyHeirarchy() {
        if ( this.stackHeirarchy.size() <= 0 )
            return;
        this.stackHeirarchy.remove( this.stackHeirarchy.size() - 1 );
    }
    
    //--------------------------
    public ArrayList<Stack> getAllStacksCreated() { return this.stacksCreated; }
    // public ArrayList<Stack> getAllStacksCreated() { return Utils.deepClone(this.stacksCreated); }

    // public ArrayList<Stack> getDepStacksHierarchy() { return this.stackHeirarchy; }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>For each Stack (represented by an index in Super-class/ArrayList), it _MAY_ have a dependency on another-stack</p>
     *  <p>Note: a null in the corresponding index in this, represents No dependency.  For example: by design <code>getStacksHasDependency(0)</code> is always NULL</p>
     *  @param ix corresponding to the element-index of this class (which extends java.util.ArrayList).  Will internally check if 'ix' is within the range.
     *  @return a Nullable String, representing the purely-AlphaNumeric StackId (Not the stackName).  Null, if 'ix' is invalid.
     */
    public final Stack getDependencyFor( final int ix ) {
        // return this.stackHasDependencyOn.get(ix);
        return ( ix <= 0 || this.stackDependencyIs.size() <= ix )   ?   null : this.stackDependencyIs.get( ix );
        // return ( ix <= 0 || this.stackDependencyIs.size() <= ix || this.stackDependencyIs.size() <= 1 )   ?   null : this.stackDependencyIs.get( ix - 1 );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Use this to put the stack (represented by this instance) as a Nested-Stack within another.</p>
     *  <p>The invoking-code _must_ have "uploaded" the file represented by {@link #CFNTemplateFile} into S3 and must provide that URL-to-S3, as the only argument to this method.</p>
     *  @param _s3ObjectURL must be a valid URL to an object containing the CFN-Template for one specific stack
     *  @param _dependsOn can be Null.  This will show up as "DependsOn:\n - ..." in the YAML generated.  Very important to help "SEQUENCE" the AWS components within a _STACKSET_ (Note: SET)
     *  @return returns the NotNull, with 1st elem as the YAML-Key and the 2nd as the YAML-as-MultiLine-String that can be embedded AS-IS (as a Nested-Stack) within another Cloudformation YAML.
     */
    public Tuple<String,String> getCFNYAMLString( final String _s3ObjectURL, final String _dependsOn ) {
        final StringBuffer buf = new StringBuffer();
        // Resource-Names within a CFN Template can ONLY be AlphaNumeric
        // Note since this YAML is embedded inside another YAML, there at least 1 tab-char at the beginning of _EACH_ line.
        buf.append( "   " ).append( this.getStackSetId() ).append( ":     ### create-stack --stack-name ").append( this.getStackSetName() ).append( "\n" );
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

        return new Tuple<String,String>( this.getStackSetId(), buf.toString() );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
