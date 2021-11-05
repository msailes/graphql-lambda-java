import * as cdk from '@aws-cdk/core';
import * as ec2 from '@aws-cdk/aws-ec2';
import * as rds from '@aws-cdk/aws-rds';
import * as secretsmanager from '@aws-cdk/aws-secretsmanager'
import {SecurityGroup} from "@aws-cdk/aws-ec2";

export class BackendStack extends cdk.Stack {

  public readonly vpc: ec2.Vpc;
  public readonly secretArn: string;

  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const vpc = new ec2.Vpc(this, 'TheVPC', {
      maxAzs: 2
    })
    this.vpc = vpc;

    const rdsSecret = new secretsmanager.Secret(this, 'rds-password', {
      secretName:'graphqlrds-credentials',
      generateSecretString: {
        excludePunctuation: true,
        passwordLength: 16,
        generateStringKey: 'password',
        secretStringTemplate: JSON.stringify({username: 'graphqladmin'})
      }
    });

    const rdsSG = new SecurityGroup(this,"RDSSG", {
      allowAllOutbound:true,
      securityGroupName: 'RdsSecurityGroup',
      vpc: vpc
    });
    rdsSG.addIngressRule(ec2.Peer.anyIpv4(), ec2.Port.allTcp(),"Allow all TCP traffic")

    const rdsCluster = new rds.DatabaseCluster(this, 'MySQLDatabase', {
      engine: rds.DatabaseClusterEngine.auroraMysql({ version: rds.AuroraMysqlEngineVersion.VER_2_10_0}),
      credentials: rds.Credentials.fromSecret(rdsSecret),
      instanceProps: {
        instanceType: ec2.InstanceType.of(ec2.InstanceClass.BURSTABLE2, ec2.InstanceSize.SMALL),
        vpcSubnets: {
          subnetType: ec2.SubnetType.PRIVATE_WITH_NAT,
        },
        securityGroups: [rdsSG],
        allowMajorVersionUpgrade: true,
        vpc,
      },
      defaultDatabaseName: 'GraphQLRDSDB',
      instances: 1
    });

    this.secretArn = rdsSecret.secretArn;
  }
}
