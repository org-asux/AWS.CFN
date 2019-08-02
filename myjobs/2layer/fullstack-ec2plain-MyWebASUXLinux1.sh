aws cloudformation create-stack --stack-name org-ASUX-Playground-Ohio-2layer-EC2-MyWebASUXLinux1  --region us-east-2 --profile ${AWSprofile} --parameters  ParameterKey=MyPublicSubnet1,ParameterValue=org-ASUX-Playground-Ohio-2layer-Subnet-Public1-ID ParameterKey=MySSHSecurityGroup,ParameterValue=org-ASUX-Playground-Ohio-2layer-SG-SSH ParameterKey=MyIamInstanceProfiles,ParameterValue=EC2-ReadWrite-to-S3 ParameterKey=AWSAMIID,ParameterValue=ami-0d8f6eb4f641ef691  ParameterKey=EC2InstanceType,ParameterValue=t2.micro  ParameterKey=MySSHKeyName,ParameterValue=Ohio-org-ASUX-Playground-LinuxSSH.pem --template-body file:///Users/Sarma/Documents/Development/src/org.ASUX/AWS/CFN/myjobs/2layer/fullstack-ec2plain-MyWebASUXLinux1.yaml