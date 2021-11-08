import * as cdk from '@aws-cdk/core';
import {Duration} from '@aws-cdk/core';
import * as lambda from '@aws-cdk/aws-lambda';
import * as ec2 from '@aws-cdk/aws-ec2';
import * as iam from '@aws-cdk/aws-iam';
import {ManagedPolicy, PolicyStatement, ServicePrincipal} from '@aws-cdk/aws-iam';
import {CorsHttpMethod, HttpApi, HttpMethod} from '@aws-cdk/aws-apigatewayv2';
import {LambdaProxyIntegration} from "@aws-cdk/aws-apigatewayv2-integrations";


interface ApiGWStackProps extends cdk.StackProps {
    vpc: ec2.Vpc;
    secretArn: string;
}

export class ApiGatewayStack extends cdk.Stack {
    constructor(scope: cdk.Construct, id: string, props: ApiGWStackProps) {
        super(scope, id, props);

        const role = new iam.Role(this, "LambdaRole", {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
        });
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'));
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaVPCAccessExecutionRole'));
        role.addToPolicy(new PolicyStatement({
            resources: [props.secretArn],
            actions: ['secretsmanager:GetSecretValue']
        }))

        const handler = new lambda.Function(this, "GraphqlOnLambdaFunction", {
            runtime: lambda.Runtime.JAVA_11,
            role: role,
            code: new lambda.AssetCode("../GraphqlLambda/target/graphql-lambda-0.0.1-SNAPSHOT.jar"),
            handler: "com.amazon.example.graphqllambda.GraphqlLambdaHandler::handleRequest",
            memorySize: 512,
            timeout: Duration.seconds(20),
            vpc: props.vpc,
            environment: {
                "RDS_SECRET": props.secretArn
            },
            vpcSubnets:
                {
                    subnetType: ec2.SubnetType.PRIVATE_WITH_NAT
                }
        });

        const httpApi = new HttpApi(this,"GraphQLApi", {
            description: 'HTTP API for Graph QL',
            corsPreflight: {
                allowHeaders:['Content-Type'],
                allowMethods:[CorsHttpMethod.OPTIONS, CorsHttpMethod.POST],
                allowOrigins: ['*']
            }
        });

        httpApi.addRoutes({
            path:'/graphql',
            methods:[HttpMethod.POST],
            integration: new LambdaProxyIntegration({
                handler: handler
            })
        })

        new cdk.CfnOutput(this, "GraphQLAPIURL", {
            value: httpApi.url || '/graphql'
        });

    }

}
