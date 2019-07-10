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

import java.util.ArrayList;

import static org.junit.Assert.*;

//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
/**
 * This enum class is a bit extensive, only because the ENNUMERATED VALUEs are strings.
 * For variations - see https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
 */
public final class Util
{
    public static final String CLASSNAME = Util.class.getName();

    //-----------------------------
    public boolean verbose;

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public Util( final boolean _verbose ) {
        this.verbose = _verbose;
    }

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    public ArrayList<String> genSubnetMasks( final String vpcCidrBlk, final int numOfAZs, final int CIDRBLOCK_Byte3_Delta ) throws Exception
    {
		final String HDR = CLASSNAME + ": genSubnetMasks("+ vpcCidrBlk +"): ";
		final ArrayList<String> retval = new ArrayList<String>();

		final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile( EnvironmentParameters.CIDRBLOCKpattern );
		java.util.regex.Matcher matcher    = pattern.matcher( vpcCidrBlk );
		if ( ! matcher.find()) {
			throw new Exception( "Invalid vpcCidrBlk: '"+ vpcCidrBlk + "' provided within one of the many Properties-files." );
		} else {
			if ( this.verbose ) System.out.println( ": I found the text "+ matcher.group() +" starting at index "+  matcher.start() +" and ending at index "+ matcher.end() );    
			final String s1 = matcher.group(1); // line.substring( matcher.start(), matcher.end() );
			final String s2 = matcher.group(2); // line.substring( matcher.start(), matcher.end() );
			final String s3 = matcher.group(3); // line.substring( matcher.start(), matcher.end() );
			final String s4 = matcher.group(4); // line.substring( matcher.start(), matcher.end() );
			final String s5 = matcher.group(5); // line.substring( matcher.start(), matcher.end() );
			if ( this.verbose ) System.out.println( "\t s1=[" + s1 +"]\t s2=[" + s2 +"]\t s3=[" + s3 +"]\t s4=[" + s4 +"]\t/\ts5=[" + s5 +"]" );

			try {
				final int b1 = Integer.parseInt(s1);
				final int b2 = Integer.parseInt(s2);
				final int b3 = Integer.parseInt(s3);
				final int b4 = Integer.parseInt(s4);
				final int cidrBlockInt = b1*256*256*256 + b2*256*256 + b3*256 + b4;
				if ( this.verbose ) System.out.println( HDR + "CIDRBlockInt = "+ cidrBlockInt );
				final int cidrBlockRange = Integer.parseInt(s5);
				if ( this.verbose ) System.out.println( HDR + "cidrBlockRange = "+ cidrBlockRange );
				int iy = 1;
				while( Math.pow(2,iy)<cidrBlockRange ) {
					if ( this.verbose ) System.out.println( HDR + "Math.pow(2^iy) = "+ Math.pow(2,iy)  + ", cidrBlockRange = "+ cidrBlockRange );
					iy ++;
				}
				final int subnetMask = ( 32 - 8 - iy ); // assumption that last/4th byte of CIDRBlock (a.k.a. right-most 8 bits) Not in subnet-mask.
				if ( this.verbose ) System.out.println( HDR + "iy = "+ iy + ", subnetMask = "+ subnetMask );

				int newb2 = b2;
				int newb3 = b3;

				for ( int ix=1; ix <= numOfAZs; ix ++ ) {
					newb3 += CIDRBLOCK_Byte3_Delta;
					if ( newb3 > 255 ) {
						newb2 += Math.floorDiv( newb3, 256 );
						newb3  = Math.floorMod( newb3, 256 );
					}
					final String subnet = ""+ b1 +"."+ newb2 +"."+ newb3 +"."+ b4 +"/"+ subnetMask;
					if ( this.verbose ) System.out.println( HDR + "subnet-"+ix+" = "+ subnet );

					retval.add( subnet );
				} // for numOfAZs

				return retval;

			} catch( IllegalStateException e ) {
				e.printStackTrace( System.err );
				System.err.println("FAILURE!!! SERIOUS INTERNAL LOGIC FAILURE: Shouldn't be happening for '"+ vpcCidrBlk + "'" );
				throw e;
			} catch( IndexOutOfBoundsException e ) {
				e.printStackTrace( System.err );
				System.err.println("FAILURE!!! SERIOUS INTERNAL LOGIC FAILURE: Shouldn't be happening for '"+ vpcCidrBlk + "'" );
				throw e;
			} catch( NumberFormatException e ) {
				e.printStackTrace( System.err );
				System.err.println("FAILURE!!! SERIOUS INTERNAL LOGIC FAILURE: Shouldn't be happening for '"+ vpcCidrBlk + "'" );
				throw e;
			}
		}
	}


    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

    //=================================================================================
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //=================================================================================

};
