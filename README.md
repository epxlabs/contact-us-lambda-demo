# contact-us-lambda-demo

A Clojure Lambda designed to receive a JSON message from the Contact Us form on the site and send an email through SES.

Requirements for deployment:

1. AWS Account
2. At least 1 email address
3. [Java](https://java.com/en/download/)
4. [Leiningen](http://leiningen.org/)
5. [AWS Command Line tools](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-set-up.html)

High level steps to deploy a fully working serverless Contact Us form:

1. Set up code
2. Create IAM roles and policies for Lambda function
3. Create new Lambda function
4. Set up SES
5. Set up API Gateway
6. Set up and deploy frontend
7. Test!

## Set up code

Pull down the repository, pull in the dependencies, and modify the from and to addresses for SES.

### Get Repo

```
git clone git@github.com:epxlabs/contact-us-lambda-demo.git
```

### Pull in dependencies

```
cd contact-us-lambda-demo
lein deps
```

### Modify to and from addresses

In `src/contact_us_lambda_demo/core.clj` modify the `from-address` and `to-addresses`.

In `resources/aws/contact-us-lambda-policy.json` change the `"ses:FromAddress"`.


## Create IAM roles and policies for Lambda function

Create a new IAM policy named `contact-us-lambda-demo`. For the policy body use the policy in `resources/aws/contact-us-lambda-policy.json`.

Create a new IAM role named `contact-us-lambda-demo`. Add the `contact-us-lambda-demo` policy and the `AWSLambdaBasicExecutionRole` to the IAM role.


## Create new Lambda function

### Uberjar Creation

To create the JAR

```
lein uberjar
```

Verify that contact-us-lambda-demo-0.1.0-SNAPSHOT-standalone.jar exists in `target/`

### Lambda Function Upload

To create the lambda run (REMEMBER TO REPLACE {AWS ACCOUNT ID}):

```
aws lambda create-function \
--function-name contact-us-lambda-demo \
--handler 'contact_us_lambda_demo.core' \
--runtime java8 \
--memory 1024 \
--timeout 20 \
--role 'arn:aws:iam::{AWS ACCOUNT ID}:role/contact-us-lambda-demo' \
--zip-file 'fileb://./target/contact-us-lambda-demo-0.1.0-SNAPSHOT-standalone.jar'
```

This will return something like:

```json
{
    "CodeSha256": "aUDD2xOT2VvCE4/y49ntBK7uNIi9gP5HZzB87ZA0jko=", 
    "FunctionName": "contact-us-lambda-demo", 
    "CodeSize": 8414199, 
    "MemorySize": 1024, 
    "FunctionArn": "arn:aws:lambda:us-east-1:{AWS ACCOUNT ID}:function:contact-us-lambda-demo", 
    "Version": "$LATEST", 
    "Role": "arn:aws:iam::{AWS ACCOUNT ID}:role/contact-us-lambda-demo", 
    "Timeout": 10, 
    "LastModified": "2016-05-15T00:22:05.873+0000", 
    "Handler": "contact_us_lambda_demo.core", 
    "Runtime": "java8", 
    "Description": ""
}
```


## Set up SES

Verify your email addresses in the SES console so mail can be sent and received by your account.


## Set up API Gateway

API Gateway is very picky about what you send it. It seems to strongly prefer application/json. So in order to get it to accept form data we must use a mapping.

The Velocity mapping template we use is heavily influenced by: https://forums.aws.amazon.com/thread.jspa?messageID=673012&tstart=0#673012

### Set up new API in API Gateway console

1. Create a new API
2. Add a resource named Contact Us with url `/contact-us`
3. Create POST Method
4. Connect POST Method to `contact-us-lambda-demo` Lambda function
5. In `Integration Request` -> 200 Response -> `Body Mapping Templates` -> `application/x-www-form-urlencoded` paste in the mapping template from `resources/aws/api_gateway_mapping_template`
6. Enable CORS
7. Deploy the API as `prod`
8. Note the Invoke URL, you will need it for the frontend

## Set up and deploy frontend

### Add API Gateway endpoint

Update the API Gateway Invoke URL in `resources/public/js/view.contact.js`.

Should be like `https://t0nlhdyll3.execute-api.us-east-1.amazonaws.com/prod/contact-us`.

### Create S3 Website Bucket

1. Create a new S3 bucket named `www.contactuslambda.com` (or other URL if taken).
2. Update the bucket name in `resources/aws/public-s3-bucket-policy.json` with your bucket name.
3. Add the Bucket Policy to your S3 bucket.
4. Enable website hosting and make the Index Document index.html.

### Upload frontend to S3 Bucket

In the root of the repo upload `resources/public/*` into `www.contactuslambda.com` with:

```
aws s3 sync ./resources/public/ s3://www.contactuslambda.com --acl=public-read
```


## Testing

### Lambda Testing

To test the contact us email sending we must use the test data in `resources/test/input/sample_contact_data.json`.

To invoke the function we run:

```shell
aws lambda invoke \
 --invocation-type RequestResponse \
 --function-name contact-us-lambda-demo \
 --payload 'file://./resources/test/input/sample_contact_data.json' \
 resources/test/output/contact_output.json
```

### Test API Gateway using cURL

On your local machine execute:

```shell
curl -X POST -v -d 'name=EPX+Labs&email=hello%40epxlabs.com&phone=1234567890&message=Look+Ma%21+No+servers%21' https://{API GATEWAY DOMAIN}/prod/contact-us --header "Content-Type:application/x-www-form-urlencoded; charset=UTF-8"
```

### Test with form

Visit `https://{S3 Bucket URL}` to see the Contact Us form. Fill out and send!

## Deploying New Lambda Code

Don't forget to run `lein uberjar` before deploying!

To deploy new code run:

```shell
aws lambda update-function-code \
 --function-name contact-us-lambda-demo \
 --zip-file 'fileb://./target/contact-us-lambda-demo-0.1.0-SNAPSHOT-standalone.jar'
```

## Update Lambda Configuration

To update configuration as opposed to code run:

```shell
aws lambda update-function-configuration \
 --function-name contact-us-lambda-demo \
 --memory 1024 \
 --timeout 20
```
