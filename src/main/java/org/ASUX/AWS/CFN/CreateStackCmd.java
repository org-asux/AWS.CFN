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
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * This class exists to represent the "aws cloudformation create-stack .. --parameters .." command in a structured manner, so that the same command can be re-represented as a NESTED-Stack.
 */
public final class CreateStackCmd
{
    public static final String CLASSNAME = CreateStackCmd.class.getName();

    public boolean verbose;

    private final String AWSRegion;
    private final String CFNTemplateFile;

    private String stackName;
    private final LinkedHashMap<String,String> parameters = new LinkedHashMap<>();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public CreateStackCmd( final boolean _verbose, final String _awsregion, final String _cfntemplatefile ) {
        this.verbose = _verbose;
        this.AWSRegion = _awsregion;
        this.CFNTemplateFile = _cfntemplatefile;
        assertTrue( this.AWSRegion != null );
        assertTrue( this.CFNTemplateFile != null );
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

    public String getStackName()                    { return this.stackName; }
    public void   setStackName( final String _sn )  { this.stackName = _sn;  }

    public String getCFNTemplateFile()              { return this.CFNTemplateFile; }

    /**
     *  @return get back the content passed in via multiple invocations of {@link #addParameter(String, String)}, as a HashMap of pairs-of-Strings.
     */
    public LinkedHashMap<String,String> getParams() { return this.parameters; }

    /**
     *  A string to use As-Is as the parameters within the "aws cloudformation create-stack .. --parameters .." command.
     *  @return a String of the form " ParameterKey=Key1,ParameterValue=Val1 ParameterKey=Key2,ParameterValue=Val2 ParameterKey=Key3,ParameterValue=Val3".  Note the leading blank, but no trailing blank.
     */
    public String getParamsAsString()       {
        final StringBuffer buf = new StringBuffer();
        for( String key: this.parameters.keySet() ) {
            final String val = this.parameters.get( key );
            buf.append( " ParameterKey=" ).append( key ).append( ",ParameterValue=" ).append( val );
        }
        return buf.toString();
    }
    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public String toString() {
        return "aws cloudformation create-stack --stack-name "+ this.stackName
            +" --region "+ this.AWSRegion
            +" --parameters "+this.getParamsAsString()
            +" --template-body file://"+ this.getCFNTemplateFile()
            +" --profile ${AWSprofile} ";
    }

/**
 *  <p>Use this to put the stack (represented by this instance) as a Nested-Stack within another.</p>
 *  <p>The invoking-code _must_ have "uploaded" the file represented by {@link #CFNTemplateFile} into S3 and must provide that URL-to-S3, as the only argument to this method.</p>
 *  @param _s3ObjectURL must be a valid URL to an object containing the CFN-Template for one specific stack
 *  @return returns the NotNull, with 1st elem as the YAML-Key and the 2nd as the YAML-as-MultiLine-String that can be embedded AS-IS (as a Nested-Stack) within another Cloudformation YAML.
 */
    public Tuple<String,String> getCFNYAMLString( final String _s3ObjectURL, final String _dependsOn ) {
        final StringBuffer buf = new StringBuffer();
        final String sn = this.getStackName().replaceAll("-","").replaceAll("_","").replaceAll("\\.",""); // Resource-Names within a CFN Template can ONLY be AlphaNumeric
        // System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! replaceAllHyphens = '"+ this.getStackName().replaceAll("-","") +"'\n" );
        // System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! replaceAllAll = '"+ sn +"'\n" );
        // Note since this YAML is embedded inside another YAML, there at least 1 tab-char at the beginning of _EACH_ line.
        buf.append( "   " ).append( sn ).append( ":     ### create-stack --stack-name ").append( this.getStackName() ).append( "\n" );
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
            // buf.append( "            MyVPCStackPrefix: org-ASUX-Playground-Ohio\n" );
            for( String key: this.parameters.keySet() ) {
                final String val = this.parameters.get( key );
                buf.append( "            " ).append( key ).append( ": " ).append( val ).append( "\n" );
            }
        }
                // # Tags: 
                // #   - Key: __
                // #     Value: ___
                // # TimeoutInMinutes: '3'   ### When CloudFormation detects that the nested-stack has reached the CREATE_COMPLETE state, it marks the nested-stack resource as CREATE_COMPLETE in the parent-stack and resumes creating the parent-stack.

        return new Tuple<String,String>( sn, buf.toString() );
    }

};
