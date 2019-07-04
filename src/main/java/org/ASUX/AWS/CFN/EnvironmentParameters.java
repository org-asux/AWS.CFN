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

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * 
 */
public final class EnvironmentParameters implements Serializable {

    private static final long serialVersionUID = 439L;

    public static final String CLASSNAME = EnvironmentParameters.class.getName();

    public static final String ORGASUXHOME = "ORGASUXHOME";
    public static final String AWSHOME = "AWSHOME";
    public static final String AWSCFNHOME = "AWSCFNHOME";

    public static final String PROPERTIES_FOR_CFN = "cfn.properties"; // one of the many Properties objects within this.allPropsRef (see go())
    public static final String PROPERTIES_FOR_JOB = "job.properties"; // one of the many Properties objects within this.allPropsRef (see go())

    public static final String AWSREGIONSLOCATIONS = "config/AWSRegionsLocations.properties";
    public static final String JOB_DEFAULTS = "/config/DEFAULTS/job-DEFAULTS.properties"; // under AWSCFNHOME
    public static final String JOBSET_MASTER = "jobset-Master.properties"; // under '.' folder

    // =================================================================================
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // =================================================================================

    public boolean verbose;

    public String orgasuxhome   = "UNDEFINED";
    public String awshome       = "UNDEFINED";
    public String awscfnhome    = "UNDEFINED";

    public String AWSRegion     = "UNDEFINED";
    public String AWSLocation   = "UNDEFINED";
    public String MyStackNamePrefix = "UNDEFINED";
    public String MyVPCStackPrefix  = "UNDEFINED";

    public String cfnJobTYPE    = "UNDEFINED";

    //---------------- PRIVATE ----------------
    private transient LinkedHashMap<String, Properties> allPropsRef;   // this could have been 'final' too, but for the fact that this.deepClone() needs to reset it.

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     * The only constructor
     * @param _verbose  Whether you want deluge of debug-output onto System.out.
     * @param _allProps a (NotNull) reference provided by CmdInvoker().memoryAndContext.getAllPropsRef().. or other source
     */
    public EnvironmentParameters( final boolean _verbose, final LinkedHashMap<String, Properties> _allProps  ) {
        this.verbose = _verbose;
        this.allPropsRef = _allProps;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /**
     * The getter-method to the ONLY private instance-variable of this class.
     * @return NotNull instance, unless of course, logic-errors led to the constructor being called to set this.allPropsRef to null.
     */
    public LinkedHashMap<String, Properties> getAllPropsRef() {
        return this.allPropsRef;
    }

    //==============================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //==============================================================================

    /** This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class.
     *  @param _orig what you want to deep-clone
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static EnvironmentParameters deepClone( EnvironmentParameters _orig ) {
        try {
            final EnvironmentParameters newobj = org.ASUX.common.Utils.deepClone( _orig );
            newobj.deepCloneFix( _orig );
            return newobj;
        } catch (Exception e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping this on the user.
            return null;
        }
    }

    /**
     * In order to allow deepClone() to work seamlessly up and down the class-hierarchy.. I should allow subclasses to EXTEND (Not semantically override) this method.
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
