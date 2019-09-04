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

import org.ASUX.yaml.CmdLineArgsCommon;
import org.ASUX.yaml.YAML_Libraries;

import java.util.ArrayList;

import org.apache.commons.cli.*;

import static org.junit.Assert.*;

/** <p>This class is a typical use of the org.apache.commons.cli package.</p>
 *  <p>This class has No other function - other than to parse the commandline arguments and handle user's input errors.</p>
 *  <p>For making it easy to have simple code generate debugging-output, added a toString() method to this class.</p>
 *  <p>Typical use of this class is: </p>
 *<pre>
 public static void main(String[] _args) {
 cmdLineArgs = new CmdLineArgsIntf(_args);
 .. ..
 *</pre>
 *
 *  <p>See full details of how to use this, in {@link org.ASUX.yaml.Cmd} as well as the <a href="https://github.com/org-asux/org.ASUX.cmdline">org.ASUX.cmdline</a> GitHub.com project.</p>
 * @see org.ASUX.yaml.Cmd
 */
public class CmdLineArgs extends org.ASUX.yaml.CmdLineArgsCommon {

    private static final long serialVersionUID = 441L;

    public static final String CLASSNAME = CmdLineArgs.class.getName();

    protected static final String VPCGEN = "vpc-gen";
    protected static final String SUBNETSGEN = "subnet-gen";
    protected static final String SGGEN = "sg-gen";
    // protected static final String SGEFSGEN = "sg-efs-gen";
    protected static final String EC2PLAINGEN = "ec2plain-gen";

    // protected static final String VPNCLIENTGEN = "vpnclient-gen";
    protected static final String FULLSTACKGEN = "fullstack-gen";

    protected static final String ITEMNUMBER = "itemNumber";
    protected static final String S3BUCKETNAME = "s3bucketname";

    //------------------------------------
    // public boolean verbose;
    protected Enums.GenEnum cmdName = Enums.GenEnum.UNDEFINED;

    protected String jobSetName = "undefined-JobSetName";
    protected String itemNumber = "undefined-ItemNumber";
    protected String scope = "NeitherPublicNorPrivate-UNINITIALIZEDJavaInstanceVariable";
    
    private String s3bucketname = "undefined-S3-BucketName";

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public Enums.GenEnum getCmdName()   { return this.cmdName; }
    public String getJobSetName()       { return this.jobSetName; }
    public String getItemNumber()       { return this.itemNumber; }
    public String getScope()            { return this.scope; }

    public String getS3bucketname()            { return this.s3bucketname; }

    //------------------------------------
    /** 
     *  <p>This method completely __OVERRIDES__ the super/parent-class' implementation of this method.</p>
     *  <p>For making it easy to have simple code generate debugging-output, added this toString() method to this class.</p>
     */
    @Override
    public String toString() {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // This method completely OVERWRITES the super/parent-class' implementation of this method.
        return
        " --verbose="+verbose+" cmdName="+cmdName
        +" jobSetName="+jobSetName+" itemNumber="+itemNumber
        + super.toString()
        ;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Add cmd-line argument definitions (using apache.commons.cli.Options) for the instance-variables defined in this class.</p>
     */
    @Override
    protected void defineAdditionalOptions()
    {   final String HDR = CLASSNAME + ": defineAdditionalOptions(): ";
        Option opt;

        //----------------------------------
        final OptionGroup grp = new OptionGroup();

        final Option vpcgen       = CmdLineArgsCommon.genOption( "vpc", VPCGEN, "create a new VPC", 1, "jobname" );
        final Option subnetsgen   = CmdLineArgsCommon.genOption( "s", SUBNETSGEN, "create private or public subnets within a VPC", 2, "jobname" );
            subnetsgen.setArgName("JobSetName> <public|private"); // overwrite what was set within genOption() above
        final Option sggen = CmdLineArgsCommon.genOption( "gs", SGGEN, "create a SecurityGroup just to allow specific protocol-access to a AMZN2-Linux EC2-instance", 2, "jobname" );
            sggen.setArgName("JobSetName> <ssh|https"); // overwrite what was set within genOption() above
        final Option ec2plaingen  = CmdLineArgsCommon.genOption( "e", EC2PLAINGEN, "create a new plain EC2-instance of AMZN2-Linux", 2, "jobname" );
            ec2plaingen.setArgName("JobSetName> <public|private"); // overwrite what was set within genOption()

        final Option fullstackgen = CmdLineArgsCommon.genOption( "fg", FULLSTACKGEN, "create a new stack that includes a VPC, subnets, SGs and an EC2 instance", 1, "job.yaml" );
        // final Option vpnclientgen = CmdLineArgsCommon.genOption( "vn", VPNCLIENTGEN, "create a new VPN-Client connection for a specific VPC", 1, "jobname" );

        grp.addOption(vpcgen);
        grp.addOption(subnetsgen);
        grp.addOption(sggen);
        // grp.addOption(sgefsgen);
        grp.addOption(ec2plaingen);

        // grp.addOption(vpnclientgen);
        grp.addOption(fullstackgen);

        grp.setRequired( true );

        this.options.addOptionGroup( grp );

        //----------------------------------
        opt = new Option("n", ITEMNUMBER, false, "if multiple copies, give this a unique ID-suffix like 1 or A ..etc..");
        opt.setRequired(false);
        opt.setArgs(1);
        opt.setOptionalArg(false);
        opt.setArgName("itemNumber");
        this.options.addOption(opt);

        //----------------------------------
        opt = new Option("s3", S3BUCKETNAME, false, "Name of the S3 bucket into which individual Stack-CFN-Templates will be saved, so that a Stack-SET can be created. Example: bucketname@eu-west-1");
        opt.setRequired(false);
        opt.setArgs(1);
        opt.setOptionalArg(false);
        opt.setArgName("S3-Bucket-Name");
        this.options.addOption(opt);

// System.out.println( HDR +"function completed." );
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

    /**
     *  @see org.ASUX.yaml.CmdLineArgsCommon#parseAdditionalOptions
     */
    @Override
    protected void parseAdditionalOptions( String[] _args, final org.apache.commons.cli.CommandLine _apacheCmdProcessor )
                    throws MissingOptionException, ParseException, Exception
    {   final String HDR = CLASSNAME + ": parseAdditionalOptions([]],..): ";

        //-------------------------------------------
        class ReusableCode {
            public final void setInstanceVariables( final String _cmd, final String[] subnetsArgs, final CmdLineArgs  _THIS ) throws MissingOptionException {
                // because we set .setArgs(2) above.. you can get the values for:- subnetsArgs[0] and subnetsArgs[1].
                _THIS.jobSetName = subnetsArgs[0]; // 1st of the 2 arguments for INSERT cmd.
                if ( _THIS.jobSetName.endsWith(".yaml") )
                    _THIS.jobSetName = _THIS.jobSetName.replaceAll( ".yaml$", "" );
                if ( verbose ) System.err.println( "_THIS.jobSetName for Cmd="+ _cmd +" is "+ _THIS.jobSetName );
                _THIS.scope = subnetsArgs[1];
                if ( _THIS.scope == null || ( ! _THIS.scope.toLowerCase().matches("public|private|public+natgw") ) )
                    throw new MissingOptionException("Command "+ _cmd +" requires 2nd argument to be precisely 'public' or 'private' or 'public+natgw'");
                else
                    _THIS.scope = _THIS.scope.toLowerCase();
                    _THIS.scope = Character.toUpperCase( _THIS.scope.charAt(0) ) + _THIS.scope.substring(1);
                    if ( _THIS.scope.equals( Environment.PUBLIC_PLUS_NATGW ) )
                        _THIS.scope = Environment.PUBLIC_WITH_NATGW; // make 'natgw' into ALL upper-case
                }
        }

        //-------------------------------------------
        if ( _apacheCmdProcessor.hasOption( VPCGEN ) ) {
            this.cmdName = Enums.GenEnum.VPC;
            this.jobSetName = _apacheCmdProcessor.getOptionValue( VPCGEN );
        }
        if ( _apacheCmdProcessor.hasOption( SUBNETSGEN ) ) {
            this.cmdName = Enums.GenEnum.SUBNET;
            new ReusableCode().setInstanceVariables( SUBNETSGEN, _apacheCmdProcessor.getOptionValues( SUBNETSGEN ), this );
        }
        if ( _apacheCmdProcessor.hasOption( SGGEN ) ) {
            this.cmdName = Enums.GenEnum.SG;
            final String[] sgArgs = _apacheCmdProcessor.getOptionValues( SGGEN );
            this.jobSetName = sgArgs[0];
            this.scope = sgArgs[1];
        }

        if ( _apacheCmdProcessor.hasOption( EC2PLAINGEN ) ) {
            this.cmdName = Enums.GenEnum.EC2PLAIN;
            new ReusableCode().setInstanceVariables( EC2PLAINGEN, _apacheCmdProcessor.getOptionValues( EC2PLAINGEN ), this );
        }

        if ( _apacheCmdProcessor.hasOption( FULLSTACKGEN ) ) {
            this.cmdName = Enums.GenEnum.FULLSTACK;
            this.jobSetName = _apacheCmdProcessor.getOptionValue( FULLSTACKGEN );
        }
        // if ( _apacheCmdProcessor.hasOption( VPNCLIENTGEN ) ) {
        //     this.cmdName = Enums.GenEnum.VPNCLIENT;
        //     this.jobSetName = _apacheCmdProcessor.getOptionValue( VPNCLIENTGEN );
        // }

        // let's do this check once more.. in case something slipped-thru the above complexity.
        if ( this.jobSetName.endsWith(".yaml") )
            this.jobSetName = this.jobSetName.replaceAll( ".yaml$", "" );
        if ( this.verbose ) System.out.println( "this.jobSetName="+ this.jobSetName );
        if ( this.verbose ) System.out.println( HDR + this.toString() );

        assert( this.cmdName == Enums.GenEnum.UNDEFINED ); // sanity check

        //-------------------------------------------
        this.itemNumber = _apacheCmdProcessor.getOptionValue( ITEMNUMBER );
        if ( this.itemNumber == null )
            this.itemNumber = "";
        // else {
        //     if ( ! this.itemNumber.startsWith("-") )
        //     this.itemNumber = "-"+ this.itemNumber; // add a '-' hyphen prefix to the 'this.itemNumber'
        // }

        //-------------------------------------------
        this.s3bucketname = _apacheCmdProcessor.getOptionValue( S3BUCKETNAME );
        // if ( this.s3bucketname == null )
        //     this.s3bucketname = "";
    }


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** 
     *  <p>This method exists to allow CmdProcessor to invoke commands 'on behalf of' the user.  This is needed specifially for the 'fullStack' command.</p>
     *  <p>This deepClone function is VERY MUCH necessary, as No cloning-code can handle 'transient' variables in this class.</p>
     *  @param _orig what you want to deep-clone
     *  @param _newCmdName after cloning change the {@link #cmdName} to this-value
     *  @param _newItemNumber after cloning change the {@link #itemNumber} to this-value
     *  @param _scope make sure to pass in either "public" or "private" (case sensitive) _ONLY_
     *  @return a deep-cloned copy, created by serializing into a ByteArrayOutputStream and reading it back (leveraging ObjectOutputStream)
     */
    public static CmdLineArgs deepCloneWithChanges( final CmdLineArgs _orig, final Enums.GenEnum _newCmdName, final String _newItemNumber, final String _scope ) {
        try {
            final CmdLineArgs newobj = org.ASUX.common.Utils.deepClone( _orig );
            newobj.deepCloneFix( _orig );
            // after full cloning.. let's make some changes (per arguments provided.)
            newobj.cmdName = _newCmdName;
            if ( _newItemNumber != null )
                newobj.itemNumber = _newItemNumber;
            if ( _scope != null )
                newobj.scope = _scope;
            return newobj;
        } catch (Exception e) {
			e.printStackTrace(System.err); // Static Method. So.. can't avoid dumping it on the user.
            return null;
        }
    }

    /**
     * In order to allow deepClone() to work seamlessly up and down the class-hierarchy.. I should allow subclasses to EXTEND (Not semantically override) this method.
     * @param _orig the original NON-Null object
     */
    protected void deepCloneFix( final CmdLineArgs _orig ) {
        // because this class has at least one TRANSIENT class-variable.. ..
        // we need to 'restore' that object's transient variable to a 'replica'
        // this.options = _orig.options;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public static void main(String[] args) {
        try{
            final CmdLineArgs o = new CmdLineArgs();
            o.define();
            o.parse(args);
        } catch( Exception e) {
            e.printStackTrace(System.err); // main() for unit testing
            System.exit(1);
        }
    }

}
