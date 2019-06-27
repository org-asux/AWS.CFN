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

    public static final String CLASSNAME = CmdLineArgs.class.getName();

    protected static final String VPCGEN = "vpc-gen";
    protected static final String SUBNETSGEN = "subnets-gen";
    protected static final String VPNCLIENTGEN = "vpnclient-gen";
    protected static final String SGSSHGEN = "sg-ssh-gen";
    protected static final String SGEFSGEN = "sg-efs-gen";
    protected static final String EC2PLAINGEN = "ec2plain-gen";

    protected static final String ITEMNUMBER = "itemNumber";

    protected static final String YAMLLIB = "yamllibrary";

    //------------------------------------
    // public boolean verbose;
    protected Enums.GenEnum cmdName = Enums.GenEnum.UNDEFINED;

    protected String jobSetName = "undefined-JobSetName";
    protected String itemNumber = "undefined-ItemNumber";

    public YAML_Libraries YAMLLibrary = YAML_Libraries.NodeImpl_Library; // some default value for now

    //------------------------------------
    protected final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** <p>1 of 2 constructors.  This one is to be used in functional code.</p>
     */
    public CmdLineArgs() {
        this( false );
    }

    /** <p>2 of 2 constructors.  This one is most useful for unit-testing in debug-mode</p>
     *  @param _verbose Whether you want deluge of debug-output onto System.out.
     */
    public CmdLineArgs( final boolean _verbose ) {
        super.verbose = _verbose;
        this.defineCommonOptions( this.options );
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public Enums.GenEnum getCmdName()   { return this.cmdName; }
    public String getJobSetName()       { return this.jobSetName; }
    public String getItemNumber()       { return this.itemNumber; }

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
        ;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>This method completely __OVERRIDES__ the super/parent-class' implementation of this method.</p>
     *  <p>Add cmd-line argument definitions (using apache.commons.cli.Options) for the instance-variables defined in this class.</p>
     *  @param options a Non-Null instance of org.apache.commons.cli.Options
     */
    @Override
    protected void defineCommonOptions( final Options options ) {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // This method completely OVERWRITES the super/parent-class' implementation of this method.
        Option opt;

        opt= new Option("v", "verbose", false, "Show debug output");
        opt.setRequired(false);
        this.options.addOption(opt);

        //----------------------------------
        final OptionGroup grp = new OptionGroup();
        final Option vpcgen = genOption( "vpc", VPCGEN, false, "create a new VPC", 1 );
        final Option vpcclientgen = genOption( "vn", VPNCLIENTGEN, false, "create a new VPN-Client connection for a specific VPC", 1 );
        final Option subnetsgen = genOption( "s", SUBNETSGEN, false, "create private or public subnets within a VPC", 2 );
            subnetsgen.setArgName("JobSetName> <public|private"); // overwrite what was set within genOption()
        final Option sgsshgen = genOption( "gs", SGSSHGEN, false, "create a SecurityGroup just to allow SSH access to a AMZN2-Linux EC2-instance", 1 );
        final Option sgefsgen = genOption( "ge", SGEFSGEN, false, "create a SecurityGroup just to MOUNT an EFS onto a AMZN2-Linux EC2-instance", 1 );
        final Option ec2plaingen = genOption( "e", EC2PLAINGEN, false, "create a new plain EC2-instance of AMZN2-Linux", 1 );
        grp.addOption(vpcgen);
        grp.addOption(vpcclientgen);
        grp.addOption(subnetsgen);
        grp.addOption(sgsshgen);
        grp.addOption(sgefsgen);
        grp.addOption(ec2plaingen);
        grp.setRequired( true );

        this.options.addOptionGroup( grp );

        //----------------------------------
        OptionGroup grp2 = new OptionGroup();
        Option noQuoteOpt = new Option("qn", NOQUOTE, false, "do Not use Quotes in YAML output");
        Option singleQuoteOpt = new Option("qs", SINGLEQUOTE, false, "use ONLY Single-quote when generating YAML output");
        Option doubleQuoteOpt = new Option("qd", DOUBLEQUOTE, false, "Use ONLY Double-quote when generating YAML output");
        grp2.addOption(noQuoteOpt);
        grp2.addOption(singleQuoteOpt);
        grp2.addOption(doubleQuoteOpt);
        grp2.setRequired(false);

        this.options.addOptionGroup( grp2 );

        //----------------------------------
        opt = new Option("n", ITEMNUMBER, false, "if multiple copies, give this a unique ID-suffix like 1 or A ..etc..");
        opt.setRequired(false);
        opt.setArgs(1);
        opt.setOptionalArg(false);
        opt.setArgName("itemNumber");
        this.options.addOption(opt);

        //----------------------------------
        opt = new Option("zy", YAMLLIB, false, "only valid values are: "+ YAML_Libraries.list("\t") );
        opt.setRequired(false);
        opt.setArgs(1);
        opt.setOptionalArg(false);
        opt.setArgName("yamllibparam");
        opt.setType(YAML_Libraries.class);
        this.options.addOption(opt);

    }

    //=================================================================================
    private static Option genOption( final String _short, final String _long, final boolean _bool, final String description, final int _numArgs ) {
        final Option opt = new Option( _short, _long, _bool, description );
            opt.setArgs( _numArgs );
            opt.setOptionalArg(false);
            opt.setArgName( "JobSetName" );
        return opt;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>This method completely __OVERRIDES__ the super/parent-class' implementation of this method.</p>
     *  <p>This method is to be used by all Sub-classes for the common instance-variables defined in this claass</p>
     *  <p>After setting up to parse commandline via {@link #defineCommonOptions(Options)}, this method allows to detect what exactly was entered by the user on the command line. </p>
     *  @param _apacheCmdProcessor a Non-Null instance of org.apache.commons.cli.CommandLine
     */
    @Override
    protected void parseCommonOptions( final org.apache.commons.cli.CommandLine _apacheCmdProcessor )
    {   final String HDR = CLASSNAME + ": parse(args[]):";
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // This method completely OVERWRITES the super/parent-class' implementation of this method.
        this.verbose = this.verbose || _apacheCmdProcessor.hasOption("verbose");
        // System.out.println( HDR + "Verbose="+this.verbose );

        //-------------------------------------------
        if ( _apacheCmdProcessor.hasOption( NOQUOTE     ) ) super.quoteType = org.ASUX.yaml.Enums.ScalarStyle.PLAIN;
        if ( _apacheCmdProcessor.hasOption( SINGLEQUOTE ) ) super.quoteType = org.ASUX.yaml.Enums.ScalarStyle.SINGLE_QUOTED;
        if ( _apacheCmdProcessor.hasOption( DOUBLEQUOTE ) ) super.quoteType = org.ASUX.yaml.Enums.ScalarStyle.DOUBLE_QUOTED;
        if ( this.verbose ) System.out.println( HDR +"super.quoteType = "+super.quoteType.toString());
        // DO NOT do this --> assertTrue( super.quoteType != org.ASUX.yaml.Enums.ScalarStyle.UNDEFINED );
        // We now __actually use__ UNDEFINED to represent the fact that the end-user did NOT provide anything on the commandline (whether no-quote, single or double)
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /**
     *  <p>Subclasses to override this method - - to parse for additional options.</p>
     *  <p>FYI: This method does nothing in this parent class, as it's a placeholder for any subclasses.</p>
     *  @param _args command line argument array - as received as-is from main().
     *  @param _apacheCmdProcessor a Non-Null instance of org.apache.commons.cli.CommandLine
     *  @throws Exception like ClassNotFoundException while trying to serialize and deserialize the input-parameter
     */
    protected void moreParsing( String[] _args, final org.apache.commons.cli.CommandLine _apacheCmdProcessor ) throws Exception {
        // Do nothing in this class.  Allow child classes to override (actually insert code into the parsing logic)
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    /** Constructor.
     *  @param _args command line argument array - as received as-is from main().
     *  @throws Exception like ClassNotFoundException while trying to serialize and deserialize the input-parameter
     */
    public final void parse( String[] _args ) throws Exception
    {
        final String HDR = CLASSNAME + ": parse(args[]):";

        super.args.addAll( java.util.Arrays.asList(_args) );
        // System.out.println( HDR + super.argsAsIs.toString() );

        org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
        org.apache.commons.cli.HelpFormatter formatter = new HelpFormatter();
        // formatter.printOptions( new java.io.PrintWriter(System.out), 120, this.options, 0, 1);
        formatter.setWidth(120);
        org.apache.commons.cli.CommandLine apacheCmdProcessor = null;

        try {
            // if ( ???.verbose ) ..
            // what if the parse() statement below has issues.. ?  We can't expect to use apacheCmdProcessor.hasOption("verbose") 
            apacheCmdProcessor = parser.parse( this.options, _args, true ); //3rd param: boolean stopAtNonOption

            //-------------------------------------------
            this.parseCommonOptions( apacheCmdProcessor );  // this.verbose is set .. based on whether --verbose present on commandline

            if ( this.verbose ) System.out.println( HDR + this.toString() );

            //-------------------------------------------
            if ( apacheCmdProcessor.hasOption( VPCGEN ) ) {
                this.cmdName = Enums.GenEnum.VPC;
                this.jobSetName = apacheCmdProcessor.getOptionValue( VPCGEN );
            }
            if ( apacheCmdProcessor.hasOption( SUBNETSGEN ) ) {
                this.cmdName = Enums.GenEnum.SUBNET;
                this.jobSetName = apacheCmdProcessor.getOptionValue( SUBNETSGEN );
            }
            if ( apacheCmdProcessor.hasOption( SGSSHGEN ) ) {
                this.cmdName = Enums.GenEnum.SGSSH;
                this.jobSetName = apacheCmdProcessor.getOptionValue( SGSSHGEN );
            }
            if ( apacheCmdProcessor.hasOption( SGEFSGEN ) ) {
                this.cmdName = Enums.GenEnum.SGEFS;
                this.jobSetName = apacheCmdProcessor.getOptionValue( SGEFSGEN );
            }
            if ( apacheCmdProcessor.hasOption( EC2PLAINGEN ) ) {
                this.cmdName = Enums.GenEnum.EC2PLAIN;
                this.jobSetName = apacheCmdProcessor.getOptionValue( EC2PLAINGEN );
            }
            if ( apacheCmdProcessor.hasOption( VPNCLIENTGEN ) ) {
                this.cmdName = Enums.GenEnum.VPNCLIENT;
                this.jobSetName = apacheCmdProcessor.getOptionValue( VPNCLIENTGEN );
            }

            if ( this.verbose ) System.out.println( HDR + this.toString() );

            assert( this.cmdName == Enums.GenEnum.UNDEFINED ); // sanity check

            //-------------------------------------------
            this.itemNumber = apacheCmdProcessor.getOptionValue( ITEMNUMBER );

            //-------------------------------------------
            if ( apacheCmdProcessor.getOptionValue(YAMLLIB) != null )
                this.YAMLLibrary = YAML_Libraries.fromString( apacheCmdProcessor.getOptionValue(YAMLLIB) );
            else
                this.YAMLLibrary = YAML_Libraries.SNAKEYAML_Library; // default.

            //-------------------------------------------
            this.moreParsing( _args, apacheCmdProcessor );

            // System.err.println( CLASSNAME +": "+this.toString());

        } catch( MissingOptionException moe) {
            if ( this.verbose ) moe.printStackTrace(System.err); // Too Serious an Error.  We do NOT have the benefit of '--verbose',as this implies a FAILURE to parse command line.
            System.err.println( "\n\nERROR: @ "+ HDR +" Cmd-line options detected were:-\n"+ super.args );
            formatter.printHelp( "\n\njava <jar> "+CLASSNAME, this.options );
            System.err.println( "\n\nERROR: "+ moe.getMessage() );
            throw new ParseException( moe.getMessage() );  // Specifically for use by Cmd.main()
        } catch (ParseException pe) {
            if ( this.verbose ) pe.printStackTrace(System.err); // Too Serious an Error.  We do NOT have the benefit of '--verbose',as this implies a FAILURE to parse command line.
            System.err.println( "\n\nERROR: @ "+ HDR +" Cmd-line options detected were:-\n"+ super.args );
            formatter.printHelp( "\n\njava <jar> "+CLASSNAME, this.options );
            System.err.println( "\n\nERROR: failed to parse the command-line: "+ pe.getMessage() );
            throw pe;
        } catch( Exception e) {
            if ( this.verbose ) e.printStackTrace(System.err); // Too Serious an Error.  We do NOT have the benefit of '--verbose',as this implies a FAILURE to parse command line.
            System.err.println( "\n\nERROR: @ "+ HDR +" Cmd-line options detected were:-\n"+ super.args );
            formatter.printHelp( "\n\njava <jar> "+CLASSNAME, this.options );
            System.err.println( "\n\nERROR: failed to parse the command-line (see error-details above) " );
            throw new ParseException( e.getMessage() );  // Specifically for use by Cmd.main()
        }

    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public static void main(String[] args) {
        // class Test extends CmdLineArgs {
        //     private static final long serialVersionUID = 11111L;
        //     public Test (boolean _verbose) {
        //         super(_verbose);
        //     }
        //     protected void moreParsing( String[] _args, final org.apache.commons.cli.CommandLine _apacheCmdProcessor ) throws Exception {}
        // };
        try{
            final CmdLineArgs o = new CmdLineArgs( false );
            // o.define CommonOptions();
            o.parse(args);
        } catch( Exception e) {
            e.printStackTrace(System.err); // main() for unit testing
            System.exit(1);
        }
    }

}
