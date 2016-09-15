(ns contact-us-lambda-demo.core
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler])
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io])
  (:import (com.amazonaws.services.simpleemail AmazonSimpleEmailServiceClient)
           (com.amazonaws.services.simpleemail.model SendEmailRequest Content Message Body Destination)))

;; This Lambda function accepts a json representation of Contact Us form data and sends an email
;;  from a specific email address to the desired receipients.

;; The email address the email submission will be sent from
(def from-address "epxcontactlambdafrom@gmail.com")
;; The email addresses the email submission will be sent to
(def to-addresses ["epxcontactlambdato1@gmail.com"])

;; Set up SES client - will default to us-east-1
(def ses-client ^AmazonSimpleEmailServiceClient (AmazonSimpleEmailServiceClient.))

(defn compose-body [details]
  (str "No servers were used in the production of this message:\n"
       ;; We like Clojure so just send us the map literal!
       (with-out-str
         (clojure.pprint/pprint details))))

(defn send-email [details]
  (let [destinations (.withToAddresses (Destination.) to-addresses)
        body (Body. (Content. (compose-body details)))
        message (Message. (Content. "Contact from the Serverless Frontier") body)]
    (.sendEmail
     ses-client
     (SendEmailRequest. (format "Contact Us <%s>" from-address) destinations message))
    {:success true}))

(defn -handleRequest [this is os context]
  (with-open [w (io/writer os)]
    (-> (json/read (io/reader is) :key-fn keyword)
        (send-email)
        ;; Write JSON to the writer
        (json/write w))
    (.flush w)))
