import * as cdk from '@aws-cdk/core';
import {Duration} from '@aws-cdk/core';
import * as lambda from '@aws-cdk/aws-lambda';
import * as ec2 from '@aws-cdk/aws-ec2';
import * as iam from '@aws-cdk/aws-iam';
import {ManagedPolicy, PolicyStatement, ServicePrincipal} from '@aws-cdk/aws-iam';
import * as appsync from '@aws-cdk/aws-appsync';
import {MappingTemplate} from "@aws-cdk/aws-appsync";


interface AppsyncStackProps extends cdk.StackProps {
    vpc: ec2.Vpc;
    secretArn: string;
}

export class AppsyncStack extends cdk.Stack {
    constructor(scope: cdk.Construct, id: string, props: AppsyncStackProps) {
        super(scope, id, props);

        const role = new iam.Role(this, "AppSyncLambdaRole", {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
        });
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'));
        role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaVPCAccessExecutionRole'));
        role.addToPolicy(new PolicyStatement({
            resources: [props.secretArn],
            actions: ['secretsmanager:GetSecretValue']
        }))

        const handler = new lambda.Function(this, "GraphqlLambdaResolverFunction", {
            runtime: lambda.Runtime.JAVA_11,
            role: role,
            code: new lambda.AssetCode("../LambdaResolver/target/appsync-lambda-resolver-0.0.1-SNAPSHOT.jar"),
            handler: "com.amazon.example.resolver.LambdaHandler::handleRequest",
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

        const api = new appsync.GraphqlApi(this, 'AppSyncApi', {
            name: 'graphql-lambda-appsync-api',
            schema: appsync.Schema.fromAsset('graphql/schema.graphql'),
            authorizationConfig: {
                defaultAuthorization: {
                    authorizationType: appsync.AuthorizationType.API_KEY,
                    apiKeyConfig: {
                        expires: cdk.Expiration.after(cdk.Duration.days(365))
                    }
                },
            }
        });
        const lambdaDS = api.addLambdaDataSource('lambdaDatasource', handler);

        lambdaDS.createResolver({
            fieldName: 'getPost',
            typeName: 'Query',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "getPost", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'getPostsByAuthor',
            typeName: 'Query',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "getPostByAuthor", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'getCommentsByAuthor',
            typeName: 'Query',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "getCommentsByAuthor", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'getNumberOfCommentsOnPost',
            typeName: 'Query',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "getNumberOfCommentsOnPost", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'getCommentsOnPost',
            typeName: 'Query',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "getCommentsOnPost", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'comments',
            typeName: 'Post',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "commentsByPost", "source":  $utils.toJson($context.source) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'createComment',
            typeName: 'Mutation',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "createComment", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'upvoteComment',
            typeName: 'Mutation',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "upVoteComment", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'downvoteComment',
            typeName: 'Mutation',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "downVoteComment", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'createPost',
            typeName: 'Mutation',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "addPost", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        lambdaDS.createResolver({
            fieldName: 'incrementViewCount',
            typeName: 'Mutation',
            requestMappingTemplate: MappingTemplate.fromString('{ "version" : "2017-02-28", "operation": "Invoke", "payload": { "field": "incrementViewCount", "arguments":  $utils.toJson($context.arguments) }}'),
            responseMappingTemplate: MappingTemplate.fromString('$context.result')
        });

        new cdk.CfnOutput(this, "AppSyncAPIURL", {
            value: api.graphqlUrl
        });

    }

}
