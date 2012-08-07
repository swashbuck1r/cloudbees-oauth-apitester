
CloudBees OAuth Client App Example  
============================================

Setup Instructions
-------------------
Create a CloudBees application to host the application.  OAuth apps must be served over SSL, so you will need to attach SSL to your application.

Register for a CloudBees OAuth client key/secret (as defined in the CloudBees OAuth docs).

Attach your OAuth client key and secret to your application ID as config variables
    bees config:set -a YOUR_APP_ID oauth.key=YOUR_KEY oauth.secret=YOUR_SECRET oauth.callback=YOUR_APP_CALLBACK_URL

Deploying the app
-----------------
Deploy the app to RUN@cloud using the bees:deploy maven command.

    mvn bees:deploy -Dbees.appid=YOUR_APP_ID


Using the app
--------------
Once properly deployed, navigating to the application's URL will trigger the OAuth permission prompt.  The oauth.prompt.scopes variable in cloudbees-web.xml is used to tell the app to ask for permission to access the CloudBees API on behalf of the user (OAuth scope:  https://api.cloudbees.com/oauth/api_all).  

After the user allows the API permission, the application will be granted an oauth token that can be used to send API calls on behalf of the user.  The application provides a form where the user can modify the query string that will be sent to the API.  Clicking the execute button will send the API message and print out the XML result at the bottom of the page.


Understanding the code
----------------------
* OAuthFilter.java - ServletFilter that negotiates getting an OAuth token saves it into the HttpSession
* AppServlet.java - Servlet with the application logic for using the OAuth token from the HttpSession to send API requests
* CloudBeesClient.java - a trivially simple implementation of a CloudBees API client that can send OAuth-authenticated requests using the Bearer authentication scheme. Note: the standard CloudBees API client does not yet support authentication using Bearer tokens.
* CloudBeesOAuthDriver.java - an OAuth client implementation for the popular Scribe OAuth library (https://github.com/fernandezpablo85/scribe-java)
