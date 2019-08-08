aws cloudformation create-stack --stack-name org-ASUX-Playground-Ohio-2layer-EC2-MyPrivASUXLinux2 --region us-east-2 --parameters  ParameterKey=MySSHSecurityGroup,ParameterValue=org-ASUX-Playground-Ohio-2layer-SG-SSH ParameterKey=MyIamInstanceProfiles,ParameterValue=MyIAM-roles ParameterKey=AWSAMIID,ParameterValue=ami-0d8f6eb4f641ef691 ParameterKey=EC2InstanceType,ParameterValue=t2.micro ParameterKey=MySSHKeyName,ParameterValue=Ohio-org-ASUX-Playground-LinuxSSH.pem ParameterKey=MyPrivateSubnet1,ParameterValue=org-ASUX-Playground-Ohio-2layer-Subnet-Private1-ID --template-body file:///Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN/myjobs/2layer/fullstack-ec2plain-MyPrivASUXLinux2.yaml --profile ${AWSprofile} 