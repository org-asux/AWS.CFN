aws cloudformation create-stack --stack-name org-ASUX-Playground-Ohio-ssh-2layer-0-SG --region us-east-2 --parameters  ParameterKey=MyVPC,ParameterValue=org-ASUX-Playground-Ohio-VPCID --template-body file:///Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN/myjobs/2layer/fullstack-sg-ssh-0.yaml --profile ${AWSprofile} 