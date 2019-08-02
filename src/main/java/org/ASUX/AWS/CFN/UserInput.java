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
public final class UserInput
{
    public static final String CLASSNAME = UserInput.class.getName();

    public boolean verbose;

    protected final YAMLTools yamltools;
    // protected CmdInvoker cmdinvoker;
    // protected CmdProcessor cmdProcessor;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public UserInput( final boolean _verbose, final YAMLTools _yamltools ) {
        this.verbose = _verbose;
        this.yamltools = _yamltools;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  Will look for the YAML-Path '/AWS/VPC/Subnet/Public' or '/AWS/VPC/Subnet/Public' -- within Job.yaml file
     *  @param _subnet NotNull YAML rooted at the YAML-Path /AWS/VPC/Subnet -- within Job.yaml file
     *  @return NotNull, either "Public" or "Private".  No other value possible.
     *  @throws Exception if any issues reading/parsing the YAML file 
     */
    public String getPublicOrPrivate( final Node _subnet ) throws Exception
    {   final String HDR = CLASSNAME + ": getPublicOrPrivate(_subnet): ";

        String strPublicSubnet    = "no";
        String strPrivateSubnet   = "no";

        //-------------------------------------
        try {
            strPublicSubnet    = yamltools.readStringFromYAML( _subnet, "public" );
        } catch( java.lang.AssertionError ae ) { /* do Nothing */ }

        try {
            strPrivateSubnet   = yamltools.readStringFromYAML( _subnet, "private" );
        } catch( java.lang.AssertionError ae ) { /* do Nothing */ }

        final boolean isPublicSubnet    = ( strPublicSubnet != null && strPublicSubnet.toLowerCase().equals("yes") );
        final boolean isPrivateSubnet   = ( strPrivateSubnet != null && strPrivateSubnet.toLowerCase().equals("yes") );

        //-------------------------------------
        String PublicOrPrivate = "NeitherPublicNorPrivate"; // by default - in case of existing Subnet ID

        if ( isPublicSubnet && ! isPrivateSubnet )
            PublicOrPrivate = "Public"; // unless I am 100% sure, I'm _NOT_ making the subnet _PUBLIC_.
        else
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

};
