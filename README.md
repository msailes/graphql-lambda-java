#### GraphQL on AWS lambda with Java

##### What's inside:

<table>
    <th>Folder</th>
    <th>Purpose</th>
    <tr>
        <td>GraphqlLambda</td>
        <td>Maven application with code for Lambda function that runs GraphQL API using graphql-java library</td>        
    </tr>
    <tr>
        <td>LambdaResolver</td>
        <td>Maven application with code for AppSync Lambda Resolver</td>
    </tr>
    <tr>
            <td>cdk-app</td>
            <td>CDK application for creating network resources, Aurora RDS database , API Gateway endpoints, AppSync API and Lambda functions</td>
        </tr>
</table>

<br>

##### Deployment Instructions:

Navigate to **_GraphqlLambda_** folder inside the local Git repository and execute the following to package the Lambda function.<br>
`mvn clean package`           


Navigate to **_LambdaResolver_** folder in the local git repository and repeat the same to build the Lambda function used in the resolver. <br>
`mvn clean package`           


Navigate to _**cdk-app**_ folder in the local git repository and run the following CDK commands. <br>
`cdk synth` <br>
`cdk deploy --all`

 
 
 