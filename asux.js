#!/usr/bin/env node



//decison made: Each subfolder or org.ASUX (like this one org.ASUX.cmdline) will be a standalone project ..
// .. as in: this asux.js is EXPECTING to see cmdline-arguments **as if** it were entered by user on shell-prompt



//--------------------------
var fs = require("fs");     // https://nodejs.org/api/fs.html#fs_fs_accesssync_path_mode 

if ( ! process.env.ORGASUXHOME ) {
  console.error("ERROR: You must define the ENVIRONMENT variable 'ORGASUXHOME' accurately.  A simple way is to run asux.js from the __ROOT__ (org.ASUX) project of the org.ASUX hierarchy-of-projects at GitHub.com." );
  process.exit(99);
}
// file-included - Not a 'require'
eval( fs.readFileSync( process.env.ORGASUXHOME +'/bin/asux-common.js' ) + '' );

//==========================================================
var CMDGRP="aws.cfn"; // this entire file is about this CMDGRP.   !!! This value is needed within processJavaCmd() - that function is defined within ${ORGASUXFLDR}/bin/asux-common.js
var COMMAND = "unknown"; // will be set based on what the user enters on the commandline.

//==========================================================
/* attach options to a command */
/* if a command does NOT define an action (see .action invocation), then the options are NOT validated */
/* For Git-like submodule commands.. ..
 *	When .command() is invoked with a description argument, no .action(callback) should be called to handle sub-commands.
 *	Otherwise there will be an error.
 *	By avoiding .action(), you tell commander that you're going to use separate executables for sub-commands, much like git(1) and other popular tools.
 *	The commander will try to search the executables in the directory of the entry script (if this file is TopCmd.js) for names like:- TopCmd-install.js TopCmd-search.js
 *	Specifying true for opts.noHelp (see noHelp)  will remove the subcommand from the generated help output.
*/

CmdLine
	.version('1.0', '-v, --version')
	.usage('[options] <commands ...>')
	.option('--verbose', 'A value that can be increased by repeating', 0)
	.option('--offline', 'whether to assume No internet and use cached responses (previously saved)', 0)
.command('vpc-gen ...', 'create a new VPC in a specific region', { isDefault: false, noHelp: false } )
.command('subnets-gen ...', 'create a new subnet (must also specify public or private)', { isDefault: false, noHelp: false } )
.command('sg-ssh-gen ...', 'create a new Security-Group for allowing SSH access to EC2 instances', { isDefault: false, noHelp: false } )
.command('ec2plain-gen ...', 'create a new simple EC2 AWS-Linux2 instance (with packages optionally specified to install)', { isDefault: false, noHelp: false } )
// .command('ec2efs-gen ...', 'create a new simple AWS-Linux2 EC2 instance with an EFS mounted under /mnt', { isDefault: false, noHelp: true } )
.command('ecs-gen ...', 'create a new Elastic Container Service instance', { isDefault: false, noHelp: true } )
// .command('dns-gen ...', 'create DNS entries as appropriate', { isDefault: false, noHelp: true } )
.command('fullstack-gen ...', 'create an entire stack from a new VPC, one or more subnets, one or more SGs and one or more EC2 instances', { isDefault: false, noHelp: false } )
	;

//==========================
// Custom HELP output .. must be before .parse() since node's emit() is immediate

CmdLine.on('--help', function(){
  console.log('')
  console.log('Examples:');
  console.log('  $ %s --help', __filename);
  console.log('  $ %s --version', __filename);
  console.log('  $ %s --offline aws.cfn .. ..', __filename);
});

//==========================
/* execute custom actions by listening to command and option events.
 */

CmdLine.on('option:verbose', function () {
	console.log("Yeah.  Going verbose" + this.verbose);
  process.env.VERBOSE = this.verbose;
});

CmdLine.on('command:vpc-gen', function () {
  COMMAND="vpc-gen";
  processCFNCmd(COMMAND);
});

CmdLine.on('command:subnets-gen', function () {
  COMMAND="subnets-gen";
  processCFNCmd(COMMAND);
});

CmdLine.on('command:sg-ssh-gen', function () {
  COMMAND="sg-ssh-gen";
  processCFNCmd(COMMAND);
});

CmdLine.on('command:ec2plain-gen', function () {
  COMMAND="ec2plain-gen";
  processCFNCmd(COMMAND);
});

CmdLine.on('command:fullstack-gen', function () {
  COMMAND="fullstack-gen";
  processCFNCmd(COMMAND);
});

// CmdLine.on('command:vpnclient-gen', function () {
//   COMMAND="vpnclient-gen";
//   processCFNCmd(COMMAND);
// });

// Like the 'default' in a switch statement.. .. After all of the above "on" callbacks **FAIL** to trigger, we'll end up here.
// If we end up here, then .. Show error about unknown command
CmdLine.on('command:*', function () {
  console.error( __filename +':\nInvalid command: %s\nSee --help for a list of available commands.', CmdLine.args.join(' '));
	console.error( 'FULL command-line: ', process.argv.join(' ') );
  process.exit(21);
});

//==========================
CmdLine.parse(process.argv);

//============================================================
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//============================================================

function processCFNCmd( _CMD) {

  if (process.env.VERBOSE) console.log( "Environment variables (As-Is): AWSHOME=" + process.env.AWSHOME +", AWSCFNHOME=" + process.env.AWSCFNHOME +"\n" );

  // whether or not process.env.AWSHOME is already set already.. reset it based on the location of this file (./asux.js)
    // if ( !  process.env.AWSHOME ) {
      var parentDir = ""+__dirname;
      parentDirArr = parentDir.split(PATH.sep);
      parentDirArr.pop();
      if (process.env.VERBOSE) console.log( "REGULAR variable: parentDirArr='" + parentDirArr.join('/') +"'." );
      const afolder = ""+parentDirArr.join('/'); // for use by all scripts under process.env.ORGASUXHOME/AWS/CFN .. so it know where this asux.js is.
      if ( (afolder != process.env.AWSHOME) && EXECUTESHELLCMD.checkIfExists( process.env.AWSHOME ) ) {
        console.error( __filename +"\nThe parent-folder "+ afolder + " that contains this asux.js script conflicts with the Environment-variable AWSHOME="+ process.env.AWSHOME +".  Please unset the environment variable AWSHOME or remove the folder "+ afolder );
        process.exitCode = 9;
        return;
      }
      process.env.AWSHOME = afolder;
    // } // if

    // whether or not process.env.AWSCFNHOME is already set already.. reset it based on the location of this file (./asux.js)
    // if ( ! process.env.AWSCFNHOME ) {
      if ( (__dirname != process.env.AWSCFNHOME) && EXECUTESHELLCMD.checkIfExists( process.env.AWSCFNHOME ) ) {
        console.error( __filename +"\nThe folder "+ __dirname + " that contains this asux.js script conflicts with the Environment-variable AWSCFNHOME="+ process.env.AWSCFNHOME +".  Please unset the environment variable AWSCFNHOME or remove the folder "+ __dirname );
        process.exitCode = 9;
        return;
      }
      process.env.AWSCFNHOME=__dirname; // for use by all scripts under process.env.ORGASUXHOME/AWS/CFN .. so it know where this asux.js is.
    // } // if

    if (process.env.VERBOSE) console.log( "Environment variables (final): AWSHOME=" + process.env.AWSHOME +", AWSCFNHOME=" + process.env.AWSCFNHOME +"\n" );

    processJavaCmd( _CMD );

  } // end function processCFNCmd

//============================================================
//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//============================================================

// The Node.js process will exit on its own if there is no additional work pending in the event loop.
// The process.exitCode property can be set to tell the process which exit code to use when the process exits gracefully.
process.exitCode = 0;

//EoScript