#/bin/false


### This must be included - via '.'
### This will NOT run as a standalone script


###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================




### !!!!!!!!!!!!!!!!!!!!!!!!!! ATTENTION !!!!!!!!!!!!!!!!!!!!!!!
### Make sure _EVERYTHING_ below comes __AFTER__ sourcing 'common.sh' (above)




if [ -d ${JobSetName} ]; then
	cd ${JobSetName}
fi
USERFLDR=`pwd`
if [ "${VERBOSE}" == "1" ]; then echo ${USERFLDR}; fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

JobSetSpecs=jobset-${CFNContext}.properties
. ${JobSetSpecs}
if [ "${VERBOSE}" == "1" ]; then
	echo "[y] parsed file ${JobSetName}/${JobSetSpecs}"
	read -p '(1) enter to see what was loaded' USERRESPONSE

	\grep -v '^#' ${JobSetSpecs} | \grep -v '^$' ###------ | \sed -e 's/^AWS-/set /'
	read -p '(2) enter to continue' USERRESPONSE
fi

###=============================================================
###@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
###=============================================================

#EoF
