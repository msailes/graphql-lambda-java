#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from '@aws-cdk/core';
import {BackendStack} from '../lib/backend-stack';
import {ApiGatewayStack} from "../lib/api-gateway-stack";
import {AppsyncStack} from "../lib/appsync-stack";

const app = new cdk.App();
const backend = new BackendStack(app, 'GraphqlLambda-BackendStack', {
    env: {account: process.env.CDK_DEFAULT_ACCOUNT, region: 'us-west-2'},
});

new ApiGatewayStack(app, 'GraphqlLambda-ApiStack', {
    env: {account: process.env.CDK_DEFAULT_ACCOUNT, region: 'us-west-2'},
    vpc: backend.vpc,
    secretArn: backend.secretArn
});

new AppsyncStack(app, 'GraphqlLambda-AppsyncStack', {
    env: {account: process.env.CDK_DEFAULT_ACCOUNT, region: 'us-west-2'},
    vpc: backend.vpc,
    secretArn: backend.secretArn
});

