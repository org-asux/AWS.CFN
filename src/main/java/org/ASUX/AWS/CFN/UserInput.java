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

import java.io.Serializable;


//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

/**
 *  <p>This class COMPLEMENTS {@link org.ASUX.AWS.CFN.CmdLineArgs}.. .. so that user can provide input via Command-line, but also .. those arguments passed via Cmd-line refer to YAML/other files that contain the rest of the user's input</p>
 */
public class UserInput implements Serializable
{
    private static final long serialVersionUID = 439L;

    public static final String CLASSNAME = UserInput.class.getName();

    public boolean verbose;

    private String AWSRegion = "UNDEFINED";
    private String AWSLocation = "UNDEFINED";

    private Enums.GenEnum cfnJobTypEnum = Enums.GenEnum.UNDEFINED;
    private String cfnJobTYPEString = "UNDEFINED";

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public UserInput( final boolean _verbose, final String _AWSRegion, final String _AWSLocation ) {
        this.verbose = _verbose;
        this.AWSRegion = _AWSRegion;
        this.AWSLocation = _AWSLocation;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public UserInput( final UserInput _copy ) {
        this.verbose = _copy.verbose;

        this.AWSRegion = _copy.AWSRegion;
        this.AWSLocation = _copy.AWSLocation;

        this.cfnJobTypEnum = _copy.cfnJobTypEnum;
        this.cfnJobTYPEString = _copy.cfnJobTYPEString;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public String getAWSRegion()            { return this.AWSRegion; }
    
    public String getAWSLocation()          { return this.AWSLocation; }

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    public Enums.GenEnum getCmd() {
        return this.cfnJobTypEnum;
    }

    public String getCmdAsString()     {return this.cfnJobTYPEString; }

    // ==============================================================================

    /**
     * To prevent inconsistent values for this.cfnJobTypEnum and this.cfnJobTYPEString instance-variables (which are accessible as {@link #getCmd()} and {@link #getCmdAsString()})
     * @param _cfnJobTypEnum a value of type {@link Enums.GenEnum} - it should come from {@link CmdLineArgs#getCmdName()}
     * @param _cmdAsString NotNull String equivalent of _cfnJobTypEnum
     * @throws Exception None for this class, but subclasses may.
     */
    public void setCmd( final Enums.GenEnum _cfnJobTypEnum, final String _cmdAsString ) throws Exception {
        this.cfnJobTypEnum = _cfnJobTypEnum;
        this.cfnJobTYPEString = _cmdAsString; // was:- Boot Check And Config.get CFNJob Type As String( this.cfnJobTypEnum );
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
    public static UserInput deepClone( UserInput _orig ) {
        try {
            final UserInput newobj = org.ASUX.common.Utils.deepClone(_orig);
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
    protected void deepCloneFix( final UserInput _orig ) {
        // Luckily, this class does _NOT_ have _ANY_ TRANSIENT class-variable.. ..
        // Otherwise, this is the place, that we  'restore' that object's transient variable to a 'replica'
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Supporting method to the 4 utility functions to help STANDARDIZE the naming of STACKS - whether for VPC, SUBNET, SG OR EC2.. ..</p>
     *  <p>This specific method is actually invoked by {@link Stack#genSubnetStackName(CmdLineArgs)} and {@link Stack#genSGStackName(CmdLineArgs)}, to help appropriately incorporate the value of the &lt;itemNumber&gt; cmdline arguments for 'subnet-gen' amd 'sg-gen' commands</p>
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
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
