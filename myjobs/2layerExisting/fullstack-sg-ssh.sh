aws cloudformation create-stack --stack-name org-ASUX-Playground-Canada-2layerExisting-SG-SSH  --region ca-central-1 --profile ${AWSprofile} --parameters ParameterKey=MyVPC,ParameterValue=org-ASUX-Playground-Canada-VPCID --template-body file:///Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN/myjobs/2layerExisting/fullstack-sg-ssh.yaml