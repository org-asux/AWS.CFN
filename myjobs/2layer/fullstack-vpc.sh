aws cloudformation create-stack --stack-name org-ASUX-Playground-Ohio-VPC --region us-east-2 --parameters  ParameterKey=MyVPCStackPrefix,ParameterValue=org-ASUX-Playground-Ohio --template-body file:///Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN/myjobs/2layer/fullstack-vpc.yaml --profile ${AWSprofile} 