(defproject contact-us-lambda-demo "0.1.0-SNAPSHOT"
  :description "Lambda function to accept Contact Us form submissions"
  :url "https://github.com/epxlabs/contact-us-lambda-demo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [com.amazonaws/aws-java-sdk-ses "1.11.0"]
                 [com.amazonaws/aws-lambda-java-core "1.1.0"]]
  :aot :all)
